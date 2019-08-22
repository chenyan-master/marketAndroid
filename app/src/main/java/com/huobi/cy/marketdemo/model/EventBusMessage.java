package com.huobi.cy.marketdemo.model;

/**
 * @author chenyan@huobi.com
 * @date 2019/8/22 11:13
 * @desp
 */
public class EventBusMessage<T> {

    public static int EVENT_GET_SYMBOLS = 0x1;
    public static int EVENT_RECV_MESSAGE = EVENT_GET_SYMBOLS ++;

    public int code;
    public T data;

    public EventBusMessage(int code, T data) {
        this.code = code;
        this.data = data;
    }
}
