package com.huobi.cy.marketdemo.model;

import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * @author chenyan@huobi.com
 * @date 2019/8/21 14:10
 * @desp
 */
public class TickerBean implements Comparable<TickerBean> {

    /**
     * id : 1489464480
     * amount : 0.0
     * count : 0
     * open : 7962.62
     * close : 7962.62
     * low : 7962.62
     * high : 7962.62
     * vol : 0.0
     */

    private int id;
    private double amount;
    private int count;
    private double open;
    private double close;
    private double low;
    private double high;
    private double vol;
    private String leftCoin;
    private String rightCoin;
    private String symbol;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public String getLeftCoin() {
        return leftCoin;
    }

    public void setLeftCoin(String leftCoin) {
        this.leftCoin = leftCoin;
    }

    public String getRightCoin() {
        return rightCoin;
    }

    public void setRightCoin(String rightCoin) {
        this.rightCoin = rightCoin;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public int compareTo(@NonNull TickerBean o) {
        return leftCoin.compareTo(o.leftCoin);
    }
}
