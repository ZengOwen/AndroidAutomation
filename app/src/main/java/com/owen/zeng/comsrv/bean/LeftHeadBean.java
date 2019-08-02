package com.owen.zeng.comsrv.bean;

import android.support.annotation.DrawableRes;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * author： deemons
 * date:    2018/5/26
 * desc:
 */
public class LeftHeadBean extends AbstractExpandableItem<LeftDetailBean> implements
    MultiItemEntity {


    @DrawableRes
    public int imageRes;

    public String title;

    public String value;
    public String spKey;

    private int itemType;

    public  LeftHeadBean()
    {
        itemType = 1;
    }

    public  LeftHeadBean(int ItemType)
    {
        itemType = ItemType;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getItemType() {
        return itemType; //return LeftAdapter.TYPE_HEAD;
    }
}
