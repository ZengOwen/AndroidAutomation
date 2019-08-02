package com.owen.zeng.comsrv.webfunc;

import com.google.gson.Gson;
import com.owen.zeng.websrv.NanoHTTPD;
import com.owen.zeng.websrv.annotation.RequestBody;
import com.owen.zeng.websrv.annotation.RequestMapping;
import com.owen.zeng.websrv.annotation.RequestParam;
import com.owen.zeng.websrv.annotation.ResponseBody;
import com.owen.zeng.websrv.utils.FFileUtils;
import com.owen.zeng.websrv.utils.FLogUtils;
import com.owen.zeng.websrv.utils.FFileUploadUtils;

import com.owen.zeng.comsrv.bean.UserBean;

/**
 * @author laijian
 * @version 2017/12/3
 * @Copyright (C)上午12:29 , www.hotapk.cn
 */
public class UserController {

    @RequestMapping("userls")
    public NanoHTTPD.Response getUserLs() {
        return setResponse("user列表");
        //return setResponse("User列表","application/json");
    }

    @ResponseBody
    @RequestMapping("admin/getuser")
    public UserBean getUser() {
        return new UserBean("admin", "admin");
    }

    @RequestMapping("gethtml")
    public String getHtml() {
        return "index2.html";
    }

    @RequestMapping("upload")
    public String upload(NanoHTTPD.IHTTPSession session) {
        FFileUploadUtils.uploadFile(session, FFileUtils.getRootDir(), "file");
        return "上传成功";
    }

    @RequestMapping("adduser")
    public NanoHTTPD.Response addUser(@RequestBody UserBean userBean) {
        FLogUtils.getInstance().e(userBean);
        return setResponse("添加成功");
    }

    @RequestMapping("edituser")
    public NanoHTTPD.Response editUser(@RequestParam("userName") String userName, @RequestParam("id") int id) {
        return setResponse("修改成功");
    }

    @RequestMapping("userparam")
    public NanoHTTPD.Response userparam(@RequestParam("userName") String userName, @RequestParam("id") int id) {
        String bodystr = new Gson().toJson(new UserBean(userName, Integer.toString(id)), UserBean.class);
        return setResponse(bodystr,"application/json");
    }

    public static NanoHTTPD.Response setResponse(String res) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", res);
    }

    public static NanoHTTPD.Response setResponse(String res, String msgType) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, msgType, res);
    }
}
