package com.apple.interview.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.apple.interview.dao.auto.DemandOrderDao;
import com.apple.interview.dao.auto.OrderExecutionPlanDao;
import com.apple.interview.dao.auto.SourcingRuleDao;
import com.apple.interview.dao.auto.SupplyDao;
import com.apple.interview.entity.auto.DemandOrder;
import com.apple.interview.entity.auto.OrderExecutionPlan;
import com.apple.interview.entity.auto.SourcingRule;
import com.apple.interview.entity.auto.Supply;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuzhongyu
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResultService {
    private static final Logger logger = LoggerFactory.getLogger(ResultService.class);
    @Autowired
    private DemandOrderDao demandOrderDao;
    @Autowired
    private SupplyDao supplyDao;
    @Autowired
    private SourcingRuleDao sourcingRuleDao;
    @Autowired
    private OrderExecutionPlanDao orderExecutionPlanDao;

    private Ordering<Supply> supplyOrdering = new Ordering<Supply>() {

        @Override
        public int compare(@Nullable Supply left, @Nullable Supply right) {
            if (left == null || right == null) {
                return 0;
            }
            boolean dateCompare = DateUtil.compare(left.getDate(), right.getDate()) < 0;
            boolean siteCompare = StrUtil.compare(left.getSite(), right.getSite(), true) < 0;
            boolean productCompare = StrUtil.compare(left.getProduct(), right.getProduct(), true) < 0;
            //larger is in front
            boolean quantityCompare = NumberUtil.compare(left.getQuantity(), right.getQuantity()) < 0;
            if (dateCompare) {
                return -1;
            } else if (siteCompare) {
                return -1;
            } else if (productCompare) {
                return -1;
            } else if (quantityCompare) {
                //larger is in front
                return 1;
            }
            return 0;
        }
    };

    private Ordering<DemandOrder> demandOrderOrdering = new Ordering<DemandOrder>() {

        @Override
        public int compare(@Nullable DemandOrder left, @Nullable DemandOrder right) {
            if (left == null || right == null) {
                return 0;
            }
            boolean dateCompare = DateUtil.compare(left.getDate(), right.getDate()) < 0;
            //larger is in front
            boolean quantityCompare = NumberUtil.compare(left.getQuantity(), right.getQuantity()) < 0;
            if (dateCompare) {
                return -1;
            } else if (quantityCompare) {
                //larger is in front
                return 1;
            }
            return 0;
        }
    };

    /**
     * generate the order of execution plan by per uploadId
     *
     * @param uploadId
     * @return
     */
    public boolean generateOrderExecutionPlan(String uploadId) {
        //just clean the old data for the pure processing
        ChainWrappers.lambdaUpdateChain(orderExecutionPlanDao)
                .eq(OrderExecutionPlan::getUploadId, uploadId).remove();

        List<Supply> allOrderedSupplies = ChainWrappers.lambdaQueryChain(supplyDao)
                .eq(Supply::getUploadId, uploadId)
                //earlier date in first-come-first-serve manner
                .orderByAsc(Supply::getDate, Supply::getSite, Supply::getProduct)
                //for suppliers,the more quantity info locates in front to process for a higher priority
                .orderByDesc(Supply::getQuantity)
                .list();
        List<DemandOrder> allDemandOrders = ChainWrappers.lambdaQueryChain(demandOrderDao)
                .eq(DemandOrder::getUploadId, uploadId)
                .orderByAsc(DemandOrder::getDate)
                //for demand orders,the larger quantity info locates in front to process for a higher priority
                .orderByDesc(DemandOrder::getQuantity)
                .list();

        List<OrderExecutionPlan> executionPlans =
                generatePlants(uploadId, allOrderedSupplies, allDemandOrders);
        executionPlans.forEach(it -> orderExecutionPlanDao.insert(it));
        return true;
    }

    /**
     * generate the execution plants by first-come-first-serve manner
     * ,and the add the plants that are not matched the manner by left data.
     *
     * @param uploadId
     * @param allOrderedSupplies
     * @param allDemandOrders
     * @return
     */
    private List<OrderExecutionPlan> generatePlants(String uploadId, List<Supply> allOrderedSupplies, List<DemandOrder> allDemandOrders) {
        List<OrderExecutionPlan> executionPlans = new ArrayList<>();
        if (CollUtil.isEmpty(allOrderedSupplies) || CollUtil.isEmpty(allDemandOrders)) {
            return executionPlans;
        }
        //force to be ordered
        allOrderedSupplies = supplyOrdering.sortedCopy(allOrderedSupplies);
        for (Supply orderedSupply : allOrderedSupplies) {
            List<SourcingRule> matchedSourcingRules
                    = ChainWrappers.lambdaQueryChain(sourcingRuleDao)
                    .eq(SourcingRule::getUploadId, uploadId)
                    .eq(SourcingRule::getSite, orderedSupply.getSite())
                    .eq(SourcingRule::getProduct, orderedSupply.getProduct()).list();
            for (SourcingRule matchedSourcingRule : matchedSourcingRules) {
                List<DemandOrder> matchedDemandOrders = allDemandOrders
                        .stream().filter(eachDemandOrder ->
                                StrUtil.equalsAnyIgnoreCase(orderedSupply.getUploadId(), eachDemandOrder.getUploadId())
                                        && StrUtil.equalsAnyIgnoreCase(matchedSourcingRule.getCustomer(), eachDemandOrder.getCustomer())
                                        && StrUtil.equalsAnyIgnoreCase(matchedSourcingRule.getProduct(), eachDemandOrder.getProduct())
                                        && DateUtil.isSameDay(orderedSupply.getDate(), eachDemandOrder.getDate()))
                        .collect(Collectors.toList());
                if (CollUtil.isEmpty(matchedDemandOrders)) {
                    continue;
                }
                //force to be ordered
                matchedDemandOrders = demandOrderOrdering.sortedCopy(matchedDemandOrders);
                for (DemandOrder matchedDemandOrder : matchedDemandOrders) {
                    Long supplyQuantity = Optional.ofNullable(orderedSupply.getQuantity()).orElse(0L);
                    Long demandOrderQuantity = Optional.ofNullable(matchedDemandOrder.getQuantity()).orElse(0L);
                    // if the quantity is <=0,it makes no sense
                    if (NumberUtil.isLessOrEqual(BigDecimal.valueOf(supplyQuantity), BigDecimal.ZERO)
                            || NumberUtil.isLessOrEqual(BigDecimal.valueOf(demandOrderQuantity), BigDecimal.ZERO)) {
                        continue;
                    }
                    //create the plan entity to held the data
                    OrderExecutionPlan executionPlan = new OrderExecutionPlan();
                    executionPlan.setId(IdWorker.getIdStr());
                    executionPlan.setSite(orderedSupply.getSite());
                    executionPlan.setCustomer(matchedDemandOrder.getCustomer());
                    executionPlan.setProduct(matchedDemandOrder.getProduct());
                    executionPlan.setExecuteDate(orderedSupply.getDate());
                    executionPlan.setUploadId(uploadId);

                    //if supply quantity and the demand quantity is equal,just add to the list to save then.
                    if (NumberUtil.equals(supplyQuantity, demandOrderQuantity)) {
                        executionPlan.setQuantity(supplyQuantity);
                        //reduce the allocated quantity,it is 0.
                        orderedSupply.setQuantity(0L);
                        matchedDemandOrder.setQuantity(0L);
                    } else if (NumberUtil.compare(supplyQuantity, demandOrderQuantity) > 0) {
                        // demand order quantity is less then the demand quantity
                        // and we should record the left supply quantity info to allocate to other demand orders
                        executionPlan.setQuantity(demandOrderQuantity);
                        orderedSupply.setQuantity(NumberUtil.sub(supplyQuantity, demandOrderQuantity).longValue());
                        matchedDemandOrder.setQuantity(0L);
                    } else {
                        // demand order quantity is greater than the demand quantity
                        // and we should record the left demand order quantity info to allocate to other supply lines
                        executionPlan.setQuantity(supplyQuantity);
                        orderedSupply.setQuantity(0L);
                        matchedDemandOrder.setQuantity(NumberUtil.sub(demandOrderQuantity, supplyQuantity).longValue());
                    }
                    executionPlans.add(executionPlan);
                }
            }
        }
        List<Supply> leftOrderedSupplies = allOrderedSupplies.stream()
                .filter(it -> NumberUtil.isGreater(BigDecimal.valueOf(it.getQuantity()), BigDecimal.ZERO))
                .collect(Collectors.toList());
        List<DemandOrder> leftDemandOrders
                = allDemandOrders.stream()
                .filter(it -> NumberUtil.isGreater(BigDecimal.valueOf(it.getQuantity()), BigDecimal.ZERO))
                .collect(Collectors.toList());
        executionPlans.addAll(generateLeftPlants(uploadId, leftOrderedSupplies, leftDemandOrders));
        return executionPlans;
    }

    /**
     * generate the execution plants that are not by first-come-first-serve manner<br/>
     * the method is quite same as the above one.
     *
     * @param uploadId
     * @param allOrderedSupplies
     * @param allDemandOrders
     * @return
     */
    private List<OrderExecutionPlan> generateLeftPlants(String uploadId, List<Supply> allOrderedSupplies, List<DemandOrder> allDemandOrders) {
        List<OrderExecutionPlan> executionPlans = new ArrayList<>();
        if (CollUtil.isEmpty(allOrderedSupplies) || CollUtil.isEmpty(allDemandOrders)) {
            return executionPlans;
        }
        //force to be ordered
        allOrderedSupplies = supplyOrdering.sortedCopy(allOrderedSupplies);
        for (Supply orderedSupply : allOrderedSupplies) {
            List<SourcingRule> matchedSourcingRules
                    = ChainWrappers.lambdaQueryChain(sourcingRuleDao)
                    .eq(SourcingRule::getUploadId, uploadId)
                    .eq(SourcingRule::getSite, orderedSupply.getSite())
                    .eq(SourcingRule::getProduct, orderedSupply.getProduct()).list();
            for (SourcingRule matchedSourcingRule : matchedSourcingRules) {
                List<DemandOrder> matchedDemandOrders = allDemandOrders
                        .stream().filter(eachDemandOrder -> {
                            return StrUtil.equalsAnyIgnoreCase(orderedSupply.getUploadId(), eachDemandOrder.getUploadId())
                                    && StrUtil.equalsAnyIgnoreCase(matchedSourcingRule.getCustomer(), eachDemandOrder.getCustomer())
                                    && StrUtil.equalsAnyIgnoreCase(matchedSourcingRule.getProduct(), eachDemandOrder.getProduct());
//                                    && DateUtil.isSameDay(orderedSupply.getDate(), eachDemandOrder.getDate());
                        }).collect(Collectors.toList());
                if (CollUtil.isEmpty(matchedDemandOrders)) {
                    continue;
                }
                //force to be ordered
                matchedDemandOrders = demandOrderOrdering.sortedCopy(matchedDemandOrders);
                for (DemandOrder matchedDemandOrder : matchedDemandOrders) {
                    Long supplyQuantity = Optional.ofNullable(orderedSupply.getQuantity()).orElse(0L);
                    Long demandOrderQuantity = Optional.ofNullable(matchedDemandOrder.getQuantity()).orElse(0L);
                    // if the quantity is <=0,it makes no sense
                    if (NumberUtil.isLessOrEqual(BigDecimal.valueOf(supplyQuantity), BigDecimal.ZERO)
                            || NumberUtil.isLessOrEqual(BigDecimal.valueOf(demandOrderQuantity), BigDecimal.ZERO)) {
                        continue;
                    }
                    //create the plan entity to held the data
                    OrderExecutionPlan executionPlan = new OrderExecutionPlan();
                    executionPlan.setId(IdWorker.getIdStr());
                    executionPlan.setSite(orderedSupply.getSite());
                    executionPlan.setCustomer(matchedDemandOrder.getCustomer());
                    executionPlan.setProduct(matchedDemandOrder.getProduct());
                    executionPlan.setExecuteDate(orderedSupply.getDate());
                    executionPlan.setUploadId(uploadId);

                    //if supply quantity and the demand quantity is equal,just add to the list to save then.
                    if (NumberUtil.equals(supplyQuantity, demandOrderQuantity)) {
                        executionPlan.setQuantity(supplyQuantity);
                        //reduce the allocated quantity,it is 0.
                        orderedSupply.setQuantity(0L);
                        matchedDemandOrder.setQuantity(0L);
                    } else if (NumberUtil.compare(supplyQuantity, demandOrderQuantity) > 0) {
                        // demand order quantity is less then the demand quantity
                        // and we should record the left supply quantity info to allocate to other demand orders
                        executionPlan.setQuantity(demandOrderQuantity);
                        orderedSupply.setQuantity(NumberUtil.sub(supplyQuantity, demandOrderQuantity).longValue());
                        matchedDemandOrder.setQuantity(0L);
                    } else {
                        // demand order quantity is greater than the demand quantity
                        // and we should record the left demand order quantity info to allocate to other supply lines
                        executionPlan.setQuantity(supplyQuantity);
                        orderedSupply.setQuantity(0L);
                        matchedDemandOrder.setQuantity(NumberUtil.sub(demandOrderQuantity, supplyQuantity).longValue());
                    }
                    executionPlans.add(executionPlan);
                }
            }
        }
//        List<Supply> leftOrderedSupplies = allOrderedSupplies.stream()
//                .filter(it -> NumberUtil.isGreater(BigDecimal.valueOf(it.getQuantity()), BigDecimal.ZERO))
//                .collect(Collectors.toList());
//        List<DemandOrder> leftDemandOrders
//                = allDemandOrders.stream()
//                .filter(it -> NumberUtil.isGreater(BigDecimal.valueOf(it.getQuantity()), BigDecimal.ZERO))
//                .collect(Collectors.toList());
        return executionPlans;
    }

    public Map<String, Object> generatePageData(String uploadId) {
        Map<String, Object> model = new LinkedHashMap<>();
        List<OrderExecutionPlan> orderExecutionPlans = ChainWrappers.lambdaQueryChain(orderExecutionPlanDao)
                .eq(OrderExecutionPlan::getUploadId, uploadId)
                .orderByAsc(OrderExecutionPlan::getExecuteDate, OrderExecutionPlan::getSite)
                .orderByAsc(OrderExecutionPlan::getProduct, OrderExecutionPlan::getQuantity)
                .list();
        //extract the date as headers
        List<String> dateHeaders = orderExecutionPlans.stream().map(it ->
                DateUtil.format(it.getExecuteDate(), DatePattern.NORM_DATE_PATTERN)).distinct()
                .collect(Collectors.toList());

        Ordering<String> stringOrder = new Ordering<String>() {
            @Override
            public int compare(@Nullable String left, @Nullable String right) {
                if (left == null | right == null) {
                    return 0;
                }
                return StrUtil.compare(left, right, true);
            }
        };
        //sort from earlier to later
        dateHeaders = stringOrder.sortedCopy(dateHeaders);

        //extract the site,customer,product list
        List<String> sites = orderExecutionPlans.stream().map(OrderExecutionPlan::getSite).distinct()
                .collect(Collectors.toList());
        List<String> customers = orderExecutionPlans.stream().map(OrderExecutionPlan::getCustomer).distinct()
                .collect(Collectors.toList());
        List<String> products = orderExecutionPlans.stream().map(OrderExecutionPlan::getProduct).distinct()
                .collect(Collectors.toList());

        List<Map<String, Object>> datas = new ArrayList<>();
        for (String site : sites) {
            for (String customer : customers) {
                for (String product : products) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("site", site);
                    data.put("customer", customer);
                    data.put("product", product);
                    for (String dateHeader : dateHeaders) {
                        long quantitySum = orderExecutionPlans.stream().filter(plan -> {
                            String dateStr = DateUtil.format(plan.getExecuteDate(), DatePattern.NORM_DATE_PATTERN);
                            boolean dateMatch = StrUtil.equalsAnyIgnoreCase(dateHeader, dateStr);
                            boolean productMatch = StrUtil.equalsAnyIgnoreCase(plan.getProduct(), product);
                            boolean customerMatch = StrUtil.equalsAnyIgnoreCase(plan.getCustomer(), customer);
                            boolean siteMatch = StrUtil.equalsAnyIgnoreCase(plan.getSite(), site);
                            return dateMatch && productMatch && customerMatch && siteMatch;
                        }).mapToLong(OrderExecutionPlan::getQuantity).sum();
                        data.put(dateHeader, quantitySum);
                    }
                    datas.add(data);
                }
            }
        }
        List<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("site");
        columnHeaders.add("customer");
        columnHeaders.add("product");
        columnHeaders.addAll(dateHeaders);

        model.put("columnHeaders", columnHeaders);
        model.put("tableData", datas);
        model.put("uploadId", uploadId);
        return model;
    }

    /**
     * input the uploadId to check whether all 3 files are already uploaded or not
     *
     * @param uploadId
     * @return
     */
    public boolean checkUploadIdExists(String uploadId) {
        return ChainWrappers.lambdaQueryChain(demandOrderDao)
                .eq(DemandOrder::getUploadId, uploadId).count() > 0
                && ChainWrappers.lambdaQueryChain(supplyDao)
                .eq(Supply::getUploadId, uploadId).count() > 0
                && ChainWrappers.lambdaQueryChain(sourcingRuleDao)
                .eq(SourcingRule::getUploadId, uploadId).count() > 0;
    }
}
