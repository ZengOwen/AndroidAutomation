package com.owen.zeng.comsrv.adapter;

import android.text.TextUtils;
import android.widget.Switch;

import com.blankj.utilcode.util.SPUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.owen.zeng.comsrv.R;
import com.owen.zeng.comsrv.bean.LeftDetailBean;
import com.owen.zeng.comsrv.bean.LeftHeadBean;

import java.util.List;

/**
 * author： deemons
 * date:    2018/5/26
 * desc:
 */
public class LeftAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public static final int TYPE_HEAD   = 1;
    public static final int TYPE_HEADCHK   = 2;
    public static final int TYPE_DETAIL = 3;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public LeftAdapter(List<MultiItemEntity> data) {
        super(data);

        addItemType(TYPE_HEAD, R.layout.left_rv_item_head);
        addItemType(TYPE_HEADCHK, R.layout.left_rv_item_head2);
        addItemType(TYPE_DETAIL, R.layout.left_rv_item_detail);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_HEAD:
                if (item instanceof LeftHeadBean) {
                    helper.setImageResource(R.id.left_rv_item_image, ((LeftHeadBean) item).imageRes)
                            .setText(R.id.left_rv_item_title, ((LeftHeadBean) item).title + "...")
                            .setText(R.id.left_rv_item_value,
                                    TextUtils.isEmpty(((LeftHeadBean) item).value) ? ""
                                            : ((LeftHeadBean) item).value);

                    helper.itemView.setOnClickListener(v -> {
                        collapseOrExpand(item);
                    });
                }
                break;
            case TYPE_HEADCHK:
                if (item instanceof LeftHeadBean) {
                    helper.setImageResource(R.id.left_rv_item_image, ((LeftHeadBean) item).imageRes)
                            .setText(R.id.left_rv_item_title, ((LeftHeadBean) item).title)
                            .setChecked(R.id.switch1,((LeftHeadBean) item).value.equals("1"));

                    Switch aSwch = helper.itemView.findViewById(R.id.switch1);
                    aSwch.setOnCheckedChangeListener( (v, b) -> {
                        SPUtils.getInstance().put(((LeftHeadBean)item).spKey,b);
                    });
                }
                break;
            case TYPE_DETAIL:
                if (item instanceof LeftDetailBean) {
                    helper.setText(R.id.left_rv_item_item, ((LeftDetailBean) item).item)
                        .setChecked(R.id.left_rv_item_check, ((LeftDetailBean) item).isCheck);

                    helper.itemView.setOnClickListener(v -> {
                        int parentPosition = getParentPosition(item);
                        LeftHeadBean headBean =
                            (LeftHeadBean) getData().get(parentPosition);


                        List<LeftDetailBean> subItems = headBean.getSubItems();
                        for (int i = 0; i < subItems.size(); i++) {
                            if (subItems.get(i).isCheck) {
                                subItems.get(i).isCheck = false;
                                break;
                            }
                        }

                        int subPosition = helper.getAdapterPosition() - parentPosition - 1;
                        LeftDetailBean bean =
                            subItems.get(subPosition);
                        bean.isCheck = true;
                        headBean.value = String.valueOf(bean.item);
                        SPUtils.getInstance().put(headBean.spKey,headBean.value);
                        notifyDataSetChanged();
                    });
                }
                break;
        }
    }

    public void collapseOrExpand(MultiItemEntity item) {
        int position = getParentPosition(item);
        if (((LeftHeadBean) item).isExpanded()) {
            collapse(position);
        } else {
            expand(position);
        }
    }
}
