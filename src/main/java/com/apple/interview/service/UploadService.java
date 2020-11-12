package com.apple.interview.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.apple.interview.dao.auto.DemandOrderDao;
import com.apple.interview.dao.auto.SourcingRuleDao;
import com.apple.interview.dao.auto.SupplyDao;
import com.apple.interview.entity.auto.DemandOrder;
import com.apple.interview.entity.auto.SourcingRule;
import com.apple.interview.entity.auto.Supply;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author zhongyu
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    @Autowired
    private DemandOrderDao demandOrderDao;
    @Autowired
    private SupplyDao supplyDao;
    @Autowired
    private SourcingRuleDao sourcingRuleDao;

    public boolean uploadDemandOrder(String uploadId, MultipartFile file) throws Exception {
        List<List<String>> columnContents = processUploadRawFile(file);
        //transform the raw contents to entity list
        List<DemandOrder> demandOrders
                = columnContents.stream().map(columnContent -> {
            //the raw date content contains the english date short format
            //,so we should input the locale parameter
            Date date = DateUtil.parse(columnContent.get(2)
                    , new SimpleDateFormat("dd-MMMM-yy", Locale.ENGLISH)).toJdkDate();
            DemandOrder demandOrder = new DemandOrder();
            demandOrder.setId(IdWorker.getIdStr());
            demandOrder.setCustomer(columnContent.get(0));
            demandOrder.setProduct(columnContent.get(1));
            demandOrder.setDate(date);
            demandOrder.setQuantity(NumberUtil.parseLong(columnContent.get(3)));
            demandOrder.setUploadId(uploadId);
            return demandOrder;
        }).collect(Collectors.toList());
        //persist the uploaded file data to database,can be optimized by batch insert
        demandOrders.forEach(it -> demandOrderDao.insert(it));
        return true;
    }

    public boolean uploadSupply(String uploadId, MultipartFile file) throws Exception {
        List<List<String>> columnContents = processUploadRawFile(file);
        List<Supply> supplies
                = columnContents.stream().map(columnContent -> {
            Date date = DateUtil.parse(columnContent.get(2), "dd/MM/yy").toJdkDate();
            Supply supply = new Supply();
            supply.setId(IdWorker.getIdStr());
            supply.setSite(columnContent.get(0));
            supply.setProduct(columnContent.get(1));
            supply.setDate(date);
            supply.setQuantity(NumberUtil.parseLong(columnContent.get(3)));
            supply.setUploadId(uploadId);
            return supply;
        }).collect(Collectors.toList());
        supplies.forEach(it -> supplyDao.insert(it));
        return true;
    }

    public boolean uploadSourcingRule(String uploadId, MultipartFile file) throws Exception {
        List<List<String>> columnContents = processUploadRawFile(file);
        List<SourcingRule> sourcingRules
                = columnContents.stream().map(columnContent -> {
            SourcingRule sourcingRule = new SourcingRule();
            sourcingRule.setId(IdWorker.getIdStr());
            sourcingRule.setSite(columnContent.get(0));
            sourcingRule.setCustomer(columnContent.get(1));
            sourcingRule.setProduct(columnContent.get(2));
            sourcingRule.setUploadId(uploadId);
            return sourcingRule;
        }).collect(Collectors.toList());
        sourcingRules.forEach(it -> sourcingRuleDao.insert(it));
        return true;
    }


    /**
     * the common processing method to analyze the file to value list
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<List<String>> processUploadRawFile(MultipartFile file) throws Exception {
        logger.info("File name is {}", file.getOriginalFilename());
        List<String> contentsList = new ArrayList<>();
        //read the csv format file-
        IoUtil.readLines(file.getInputStream(), CharsetUtil.CHARSET_UTF_8, contentsList);
        if (CollUtil.isEmpty(contentsList) || contentsList.size() == 1) {
            String errorStr = StrUtil.format("the file named:{} is empty or contains invalid contents", file.getOriginalFilename());
            throw new Exception(errorStr);
        }
        //just remove the first line that indicates the column name,the value content is separated by ','
        contentsList = CollUtil.sub(contentsList, 1, contentsList.size());
        //using the stream api to handle the list string
        return contentsList.stream()
                .map(content -> StrUtil.splitTrim(content, ","))
                .collect(Collectors.toList());
    }
}
