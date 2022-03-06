package com.miaoshaproject.dataobject;

import lombok.Data;

@Data
public class StockLogDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.stock_log_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    private String stockLogId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.item_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.amount
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    private Integer amount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column stock_log.status
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    private Integer status;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.stock_log_id
     *
     * @return the value of stock_log.stock_log_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public String getStockLogId() {
        return stockLogId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.stock_log_id
     *
     * @param stockLogId the value for stock_log.stock_log_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public void setStockLogId(String stockLogId) {
        this.stockLogId = stockLogId == null ? null : stockLogId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.item_id
     *
     * @return the value of stock_log.item_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.item_id
     *
     * @param itemId the value for stock_log.item_id
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.amount
     *
     * @return the value of stock_log.amount
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.amount
     *
     * @param amount the value for stock_log.amount
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column stock_log.status
     *
     * @return the value of stock_log.status
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column stock_log.status
     *
     * @param status the value for stock_log.status
     *
     * @mbg.generated Sat Mar 05 00:43:33 CST 2022
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}