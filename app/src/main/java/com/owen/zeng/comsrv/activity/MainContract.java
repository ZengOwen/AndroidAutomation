package com.owen.zeng.comsrv.activity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.owen.zeng.comsrv.bean.MessageBean;

import java.util.ArrayList;

/**
 * author： deemons
 * date:    2018/5/26
 * desc:
 */
public interface MainContract {

    interface IView {
        void setOpen(boolean isOpen);

        void setLeftData(ArrayList<MultiItemEntity> list);

        void addData(int msgType,String date, String from, String contain);

        void showPermissionDialog();

        String getEditText();
    }

    interface IPresenter {

    }

    interface Iio{
        // return HexString
        String OnCmd(int PortNo);

        String OnCmd(int[] PortNos);

        String OffCmd(int PortNo);

        String OffCmd(int[] PortNos);

        String getStautsCmd(int PortNo);

        String getStautsCmd(int[] PortNos);

    }

}
