package com.owen.zeng.comsrv.bean;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class ResultBean {
    public int Status;
    public String Msg;
    public String Data;

    public ResultBean(){}

    //public ResultBean(int status,String msg, Map<String,Object> data){
    public ResultBean(int status,String msg, Object data){
        Status = status;
        Msg = msg;
        /*
        if (data != null) {
            Data = new HashMap<>();
            Data.putAll(data);
        }
        */
        Data = new Gson().toJson(data);
    }

}
