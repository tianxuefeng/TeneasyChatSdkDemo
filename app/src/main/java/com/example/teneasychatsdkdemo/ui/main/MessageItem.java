package com.example.teneasychatsdkdemo.ui.main;

import java.io.Serializable;

public class MessageItem implements Serializable {

    private boolean isSend;
    private String msg;
    private int payLoadId;
    private String time;

    public MessageItem(boolean isSend, String msg, int payLoadId, String time) {
        this.isSend = isSend;
        this.msg = msg;
        this.payLoadId = payLoadId;
        this.time = time;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getPayLoadId() {
        return payLoadId;
    }

    public void setPayLoadId(int payLoadId) {
        this.payLoadId = payLoadId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
