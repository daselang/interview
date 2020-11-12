package com.apple.interview.entity.auto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhongyu
 */
@Alias("OrderExecutionPlan")
@TableName("order_execution_plan")
public class OrderExecutionPlan implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String site;
    private String customer;
    private String product;
    private Date executeDate;
    private Long quantity;
    private String uploadId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Date getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    @Override
    public String toString() {
        return "OrderExecutionPlan{" +
                "id='" + id + '\'' +
                ", site='" + site + '\'' +
                ", customer='" + customer + '\'' +
                ", product='" + product + '\'' +
                ", executeDate=" + executeDate +
                ", quantity=" + quantity +
                ", uploadId='" + uploadId + '\'' +
                '}';
    }
}
