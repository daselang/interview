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
@Alias("DemandOrder")
@TableName("demand_order")
public class DemandOrder implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String customer;
    private String product;
    private Date date;
    private Long quantity;
    private String uploadId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
        return "DemandOrder{" +
                "id='" + id + '\'' +
                ", customer='" + customer + '\'' +
                ", product='" + product + '\'' +
                ", date=" + date +
                ", quantity=" + quantity +
                ", uploadId='" + uploadId + '\'' +
                '}';
    }
}
