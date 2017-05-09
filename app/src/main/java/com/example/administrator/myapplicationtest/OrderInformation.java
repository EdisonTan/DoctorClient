package com.example.administrator.myapplicationtest;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/30 0030.
 */

public class OrderInformation {
    private String name; //
    private String orderNumber; //
    private String date; //
    private String moneyAmount; //
    private String mediaName; //----------------
    private String description; //----------------
    private String gender; //
    private String regularCheck; //---------------
    private String auscultateResult;
    private boolean acceptState;
    private boolean handledState;
    private boolean auscultateState;
    private boolean paymentState;


    OrderInformation(String orderNumber,
                     String name,
                     String date,
                     String moneyAmount,
                     String mediaName,
                     String description,
                     String gender,
                     String regularCheck,
                     String auscultateState,
                     String paymentState,
                     String result){
        this.orderNumber = orderNumber;
        this.name = name;
        this.date = date;
        this.moneyAmount = moneyAmount;
        this.mediaName = mediaName;
        this.description = description;
        this.gender = gender;
        if ("1".equals(regularCheck))
            this.regularCheck = "是";
        else
            this.regularCheck = "否";
        this.auscultateResult = result;
        this.handledState = false;
        this.acceptState = false;

        if ("1".equals(auscultateState)){
            this.auscultateState = true;
            this.handledState = true;
            this.acceptState = true;
        }else if ("0".equals(auscultateState)){
            this.auscultateState = false;
            this.handledState = false;
            this.acceptState = false;
        }else if ("2".equals(auscultateState)){
            this.auscultateState = false;
            this.handledState = true;
            this.acceptState = false;
        }else if ("3".equals(auscultateState)){
            this.auscultateState = false;
            this.handledState = true;
            this.acceptState = true;
        }



        if ("1".equals(paymentState))
            this.paymentState = true;
        else
            this.paymentState = false;
    }
    public Map<String,Object> getAllInformation(){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("orderNumber",orderNumber);
        map.put("date",date);
        map.put("moneyAmount",moneyAmount);
        map.put("mediaName",mediaName);
        map.put("acceptState",acceptState);
        map.put("handledState",handledState);
        map.put("auscultateState",auscultateState);
        map.put("paymentState",paymentState);
        return map;
    }
    public boolean setAcceptState(boolean state){
        acceptState = state;
        return true;
    }
    public boolean setHandledState(boolean state){
        handledState = state;
        return true;
    }
    public boolean setAuscultateState(boolean state){
        auscultateState = state;
        return true;
    }
    public boolean setPaymentState(boolean state){
        paymentState = state;
        return true;
    }

    public boolean setAuscultateResult(String result){
        this.auscultateResult = result;
        return true;
    }

    public boolean getAcceptState(){
        return acceptState;
    }
    public boolean getHandledState(){
        return handledState;
    }
    public boolean getAuscultateState(){
        return auscultateState;
    }
    public boolean getPaymentState(){
        return paymentState;
    }
    public String getName(){
        return name;
    }
    public String getOrderNumber(){
        return orderNumber;
    }
    public String getDate(){
        return date;
    }
    public String getMoneyAmount(){
        return moneyAmount;
    }
    public String getMediaName() { return mediaName; }
    public String getGender() {return gender;}
    public String getDescription() {return description;}
    public String getRegularCheck() {return regularCheck;}
    public String getAuscultateResult() {return auscultateResult;}
}
