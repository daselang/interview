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
@Alias("Supply")
@TableName("supply")
public class Supply implements Serializable {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    private String site;
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

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
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
        return "Supply{" +
                "id='" + id + '\'' +
                ", site='" + site + '\'' +
                ", product='" + product + '\'' +
                ", date=" + date +
                ", quantity=" + quantity +
                ", uploadId='" + uploadId + '\'' +
                '}';
    }
}
