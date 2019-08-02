package com.owen.zeng.comsrv.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.owen.zeng.comsrv.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragDebug extends Fragment {
    private boolean firstShow = false;
    private View rootView;
    private MainPresenter mPresenter;

    public FragDebug() {
        // Required empty public constructor
    }

    //public FragDebug(MainPresenter aPresenter) {
    //    // Required empty public constructor
   //     mPresenter = aPresenter;
    //}
    public static FragDebug newInstance(MainPresenter aPresenter) {
        FragDebug myFragment = new FragDebug();
        Bundle args = new Bundle();
        args.putSerializable("mPresenter",aPresenter);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mPresenter = (MainPresenter) getArguments().getSerializable("mPresenter"); //testing enable
        rootView = inflater.inflate(R.layout.fragment_frag_debug, container, false);
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        if (!firstShow) {
            firstShow = true;

            ((CheckBox) rootView.findViewById(R.id.receive_show_send)).setChecked(
                    SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_SHOW_SEND, true));

            ((CheckBox) rootView.findViewById(R.id.receive_show_time)).setChecked(
                    SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_SHOW_TIME, true));

            ((RadioButton) rootView.findViewById(
                    SPUtils.getInstance().getBoolean(SPKey.SETTING_SEND_TYPE, true) ? R.id.send_hex
                            : R.id.send_asc)).setChecked(true);

            ((RadioButton) rootView.findViewById(
                    SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_TYPE, true) ? R.id.receive_hex
                            : R.id.receive_asc)).setChecked(true); //owen chg

            ((CheckBox) rootView.findViewById(R.id.send_repeat)).setChecked(false);
            ((TextView) rootView.findViewById(R.id.send_repeat_during)).setText(
                    String.valueOf(SPUtils.getInstance().getInt(SPKey.SETTING_SEND_DURING, 1000)));

            ((CheckBox) rootView.findViewById(R.id.send_crc)).setChecked(
                    SPUtils.getInstance().getBoolean(SPKey.CONF_ADD_CRC, false));

        }

        initListener();
    }

    private void initListener() {

        RadioGroup receiveRg = rootView.findViewById(R.id.receive_rg);
        receiveRg.setOnCheckedChangeListener((group, checkedId) -> {
            mPresenter.refreshReceiveType(checkedId == R.id.receive_hex);
        });

        RadioGroup sendRg = rootView.findViewById(R.id.send_rg);
        sendRg.setOnCheckedChangeListener((group, checkedId) -> {
            mPresenter.refreshSendType(checkedId == R.id.send_hex);
        });


        CheckBox showSend = rootView.findViewById(R.id.receive_show_send);
        showSend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.refreshShowSend(isChecked);
        });

        CheckBox showTime = rootView.findViewById(R.id.receive_show_time);
        showTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.refreshShowTime(isChecked);
        });

        CheckBox sendRepeat = rootView.findViewById(R.id.send_repeat);
        sendRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.refreshSendRepeat(isChecked);
        });

        CheckBox addCrc = rootView.findViewById(R.id.send_crc);
        addCrc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.refreshAddCrc(isChecked);

        });

    }
}
