package com.owen.zeng.websrv;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.lang.annotation.Annotation;
import java.util.Map;

import com.owen.zeng.comsrv.activity.MainPresenter;
import com.owen.zeng.websrv.annotation.RequestBody;
import com.owen.zeng.websrv.annotation.RequestParam;
import com.owen.zeng.websrv.annotation.ResponseBody;
import com.owen.zeng.websrv.utils.FStaticResUtils;
import com.owen.zeng.comsrv.webfunc.AppController;

/**
 * @author laijian
 * @version 2017/11/30
 * @Copyright (C)下午5:03 , www.hotapk.cn
 */
public class FHttpServer extends NanoHTTPD {
    private MainPresenter mPre;

    public FHttpServer(int port,MainPresenter aPre) {
        super(port);
        mPre = aPre;
    }

    @Override
    public Response CustServe(IHTTPSession session) {

        String file_name = session.getUri().substring(1);
        if (TextUtils.isEmpty(file_name)) {
            file_name = FHttpManager.getFHttpManager().getIndexName();
        }
        Response response = getResources(file_name);
        return response == null ? responseData(session, file_name) : response;
    }

    /**
     * 获取静态资源
     *
     * @param file_name
     * @return
     */
    private Response getResources(String file_name) {
        int dot = file_name.lastIndexOf('/');
        file_name = dot >= 0 ? file_name.substring(dot + 1) : file_name;
        String filePath = FHttpManager.getFHttpManager().getFilels().get(file_name);
        if (filePath != null) {
            return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK, getMimeTypeForFile(file_name), FStaticResUtils.getFileInp(filePath));//这代表任意的二进制数据传输。
        }
        return null;
    }


    /**
     * 解析注解文件
     *
     * @param session
     * @param file_name
     * @return
     */
    private Response responseData(IHTTPSession session, String file_name) {
        Response response;
        Object[] objects = null;
        try {
            Map<String, java.lang.reflect.Method> methods = FHttpManager.getFHttpManager().getMethods();
            java.lang.reflect.Method method = methods.get(file_name);
            if (method != null) {
                method.setAccessible(true); //允许修改反射属性
                Class cla = method.getDeclaringClass();//获取该方法所在的类
                Object obj = cla.newInstance();//实例化类

                //希望能有合适的方式传入以解耦 begin
                if (obj instanceof AppController){
                    ((AppController) obj).setdCom(mPre);
                }
                // end

                Class<?>[] parameterTypes = method.getParameterTypes(); //获得方法所有参数的类型
                if (parameterTypes.length > 0) {
                    objects = new Object[parameterTypes.length];
                    Map<String, String> sessionMap = session.getParms();//获取请求参数
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();//获取方法参数里的注解
                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        if (parameterTypes[i] == IHTTPSession.class) {
                            objects[i] = session;
                        } else if (parameterTypes[i] == Map.class) {
                            objects[i] = sessionMap;
                        } else {
                            Annotation parameterAnnotation = parameterAnnotations[i][0];//获取参数中的第一个注解。所以每个参数只能只有一个注解
                            if (parameterAnnotation.annotationType() == RequestBody.class) {//返回对象
                                byte[] buf = new byte[(int) ((HTTPSession) session).getBodySize()];
                                session.getInputStream().read(buf, 0, buf.length);
                                objects[i] = new Gson().fromJson(new String(buf), parameterTypes[i]);
                            } else if (parameterAnnotation.annotationType() == RequestParam.class) {//返回指定param
                                objects[i] = dataConversion(parameterTypes[i], sessionMap, (RequestParam) parameterAnnotation);
                            }
                        }
                    }
                }
                response = responseBody(method.getReturnType(), method.invoke(obj, objects), method.isAnnotationPresent(ResponseBody.class));
            } else {
                //response = newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, file_name + " Not Found");
                response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "ComSrv OK");
            }
        } catch (Exception e) {
            response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
        }

        //下面是跨域的参数（因为一般要和h5联调，所以最好设置一下）
        if (FHttpManager.getFHttpManager().isAllowCross()) {
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept, token, Authorization, " +
                    "X-Auth-Token,X-XSRF-TOKEN,Access-Control-Allow-Headers");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
        }

        return response;
    }

    /**
     * param数据转换
     *
     * @param parameterTypes
     * @param sessionMap
     * @param requestParam
     * @return
     */
    private Object dataConversion(Class parameterTypes, Map<String, String> sessionMap, RequestParam requestParam) {
        Object object;

        String reqParmValue = requestParam.value().toLowerCase();
        switch (parameterTypes.getName()) {
            case "int":
                object = Integer.parseInt(sessionMap.get(reqParmValue));
                break;
            case "double":
                object = Double.parseDouble(sessionMap.get(reqParmValue));
                break;
            case "float":
                object = Float.parseFloat(sessionMap.get(reqParmValue));
                break;
            case "long":
                object = Long.parseLong(sessionMap.get(reqParmValue));
                break;
            case "boolean":
                object = Boolean.parseBoolean(sessionMap.get(reqParmValue));
                break;
            default:
                object = sessionMap.get(reqParmValue);
                break;
        }
        return object;
    }


    /**
     * response 数据处理
     *
     * @param objcls
     * @param responseObj
     * @param hasAnnotation
     * @return
     */
    private Response responseBody(Class objcls, Object responseObj, boolean hasAnnotation) {
        Response response = null;
        String bodystr = "";
        if (objcls == Response.class) {
            response = (Response) responseObj;
        } else {
            if (!hasAnnotation) {
                bodystr = String.valueOf(responseObj);
                if (bodystr.endsWith(".html")) {
                    response = getResources(bodystr);
                } else {
                    response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/octet-stream", bodystr);
                }
            } else {
                bodystr = new Gson().toJson(responseObj, objcls);
                response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", bodystr);
            }
        }
        return response;
    }

}
