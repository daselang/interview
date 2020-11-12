package com.apple.interview.entity.auto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author zhongyu
 */
@Alias("SourcingRule")
@TableName("sourcing_rule")
public class SourcingRule implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String site;
    private String customer;
    private String product;
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

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    @Override
    public String toString() {
        return "SourcingRule{" +
                "id='" + id + '\'' +
                ", site='" + site + '\'' +
                ", customer='" + customer + '\'' +
                ", product='" + product + '\'' +
                ", uploadId='" + uploadId + '\'' +
                '}';
    }
}
