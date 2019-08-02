package com.owen.zeng.comsrv.webfunc;


import com.owen.zeng.comsrv.activity.Bd3201;
import com.owen.zeng.comsrv.activity.MainContract;
import com.owen.zeng.comsrv.activity.MainPresenter;
import com.owen.zeng.comsrv.bean.ResultBean;
import com.owen.zeng.comsrv.bean.ComRcvBean;
import com.owen.zeng.websrv.NanoHTTPD;
import com.owen.zeng.websrv.annotation.RequestMapping;
import com.owen.zeng.websrv.annotation.RequestParam;
import com.owen.zeng.websrv.annotation.ResponseBody;


/**
 * @author laijian
 * @version 2017/12/3
 * @Copyright (C)上午12:29 , www.hotapk.cn
 */
public class AppController {

    private Bd3201 ComBd;
    private MainContract.Iio  IComStr;
    private MainPresenter   dCom = null;

    public AppController(){
        ComBd = new Bd3201(1);
        IComStr = ComBd;
    }

    @RequestMapping("Switch")
    public  NanoHTTPD.Response ClassStatus() {
        return NanoHTTPD.setJsonResp(new ResultBean(200,"Switch Module Ready",null));
    }

    @ResponseBody
    @RequestMapping("Switch/On")
    public ResultBean On(@RequestParam("SNo") String SNo) {
        ResultBean Result = null;
        int status = 0;
        String msg ="Sucess";
        String cmdStr;

        int PortNo = Integer.parseInt(SNo);
        if (PortNo < 1 || PortNo > 64){
            status = 1;
            msg ="开关编号超出范围";
        }

        cmdStr = IComStr.OnCmd(PortNo);
        if (dCom != null) {
            ComRcvBean ComRcv = dCom.SendAndRcv(cmdStr,"Web");
            if (ComRcv.State){
                Result = new ResultBean(0,msg,ComRcv.Msg);
            }else{
                msg = "Error";
                Result = new ResultBean(1,msg,ComRcv.Msg);
            }
            //Result = dCom.sendMsg(cmdStr) ? new ResultBean(0,msg,null) :new ResultBean(1,"串口未连接",null);
        }
        return Result;
    }

    @ResponseBody
    @RequestMapping("Switch/Off")
    public ResultBean Off(@RequestParam("SNo") String SNo) {
        ResultBean Result = null;
        int status = 0;
        String msg ="Sucess";
        String cmdStr;

        int PortNo = Integer.parseInt(SNo);
        if (PortNo < 1 || PortNo > 64){
            status = 1;
            msg ="开关编号超出范围";
            return new ResultBean(status,msg,null);
        }

        cmdStr = IComStr.OffCmd(PortNo);
        if (dCom != null) {
            ComRcvBean ComRcv = dCom.SendAndRcv(cmdStr,"Web");
            if (ComRcv.State){
                Result = new ResultBean(0,msg,ComRcv.Msg);
            }else{
                msg = "Error";
                Result = new ResultBean(1,msg,ComRcv.Msg);
            }
            //Result = dCom.sendMsg(cmdStr) ? new ResultBean(0,msg,null) :new ResultBean(1,"串口未连接",null);
        }
        return Result;
    }

    public void setdCom(MainPresenter aPre){
        dCom = aPre;
    }

    @ResponseBody
    @RequestMapping("Sale")
    public ResultBean Sale(@RequestParam("LNo") String LNo) {
        ResultBean Result = null;

        int PortNo = Integer.parseInt(LNo);
        if (PortNo < 1 || PortNo > 64){
            Result = new ResultBean(404,"货道编号超出范围",null);
            return Result;
        }

        ComRcvBean ComRcv = dCom.Sale(Integer.parseInt(LNo),"Web");
        if (ComRcv.State){
            Result = new ResultBean(200,"OK",ComRcv);
        }else{
            Result = new ResultBean(500,"Error",ComRcv);
        }
        return Result;
    }

}
