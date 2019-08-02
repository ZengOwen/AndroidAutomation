package com.owen.zeng.comsrv.activity;


import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.owen.zeng.comsrv.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragBd01run extends Fragment {
    private View rootView;
    private Bd3201 aBd32;
    private MainPresenter mPresenter;

    public FragBd01run() {
        // Required empty public constructor
    }

    //public FragBd01run(MainPresenter aPresenter) {
    //    // Required empty public constructor
    //    aBd32 = new Bd3201(1);
    //    mPresenter = aPresenter;
    //}

    public static FragBd01run newInstance(MainPresenter aPresenter) {
        FragBd01run myFragment = new FragBd01run();
        Bundle args = new Bundle();
        args.putSerializable("mPresenter",aPresenter);  //testing enable
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (aBd32 == null) aBd32 = new Bd3201(1);
        mPresenter = (MainPresenter) getArguments().getSerializable("mPresenter"); //testing enable
        rootView = inflater.inflate(R.layout.frag_bd01run, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initListener();
    }

    private void initListener() {
        Switch sw = rootView.findViewById(R.id.swch01);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch02);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch03);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch04);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch05);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch06);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch07);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);

        sw = rootView.findViewById(R.id.swch08);
        sw.setOnCheckedChangeListener(this::onCheckedChanged);
    }

    private void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        int sid = compoundButton.getId();
        int sno = getSno(sid);
        ViewParent vw = rootView.getParent().getParent().getParent();
        EditText editText = ((ConstraintLayout)vw).findViewById(R.id.main_edit);
        String contain = b ? aBd32.OnCmd(sno) : aBd32.OffCmd(sno);
        editText.setText(contain);
        if (!TextUtils.isEmpty(contain)) {
            mPresenter.SendAndRcv(contain,"Local");
        }
    }

    private int getSno(int sid){
        int Result = 0;
        switch (sid)
        {
            case R.id.swch01: Result = 1;
                break;
            case R.id.swch02: Result = 2;
                break;
            case R.id.swch03: Result = 3;
                break;
            case R.id.swch04: Result = 4;
                break;
            case R.id.swch05: Result = 5;
                break;
            case R.id.swch06: Result = 6;
                break;
            case R.id.swch07: Result = 7;
                break;
            case R.id.swch08: Result = 8;
                break;

        }
        return Result;
    }
}
