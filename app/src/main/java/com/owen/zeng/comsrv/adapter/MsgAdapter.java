package com.owen.zeng.comsrv.adapter;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.owen.zeng.comsrv.R;
import com.owen.zeng.comsrv.bean.MessageBean;

import java.util.List;
import java.util.Locale;

/**
 * author： deemons
 * date:    2018/5/26
 * desc:
 */
public class MsgAdapter extends BaseMultiItemQuickAdapter<MessageBean, BaseViewHolder> {

    private String mFormat;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public MsgAdapter(List<MessageBean> data) {
        super(data);

        addItemType(MessageBean.TYPE_RECEIVE, R.layout.rv_item_receive);
        addItemType(MessageBean.TYPE_SEND, R.layout.rv_item_send);
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageBean item) {
        if (TextUtils.isEmpty(mFormat)) {
            mFormat = mContext.getString(R.string.time_format);
        }
        if (item.getItemType() == MessageBean.TYPE_RECEIVE) {
            helper.setGone(R.id.item_receive_date, !TextUtils.isEmpty(item.getDate()))
                .setText(R.id.item_receive_date, msgFrom(mFormat, item.getDate(),item.getFrom()))
                .setText(R.id.item_receive_contain, item.getContain());
        } else {
            helper.setGone(R.id.item_send_date, !TextUtils.isEmpty(item.getDate()))
                .setText(R.id.item_send_date, msgFrom(mFormat, item.getDate(),item.getFrom()))
                .setText(R.id.item_send_contain, item.getContain());
        }
    }

    private String msgFrom(String dFormat,String aDate,String aFrom){
        return String.format("%s   From: %s",String.format(Locale.getDefault(), dFormat, aDate),aFrom);
    }
}
