package com.owen.zeng.comsrv.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.owen.zeng.comsrv.activity.FragBd01run;
import com.owen.zeng.comsrv.activity.FragDebug;
import com.owen.zeng.comsrv.activity.MainPresenter;

import java.util.ArrayList;

public class BttmAdapter extends FragmentPagerAdapter {

    public ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
    public ArrayList<String> titleList = new ArrayList<String>();

    public BttmAdapter(FragmentManager fm, MainPresenter aPresenter) {
        super(fm);

        //fragmentList.add(new FragDebug(aPresenter));
        fragmentList.add(FragDebug.newInstance(aPresenter));
        titleList.add("PortDebug");
        //fragmentList.add(new FragBd01run(aPresenter)); testing rem
        fragmentList.add(FragBd01run.newInstance(aPresenter));
        titleList.add("Bd32");
    }

    @Override
    public Fragment getItem(int position) {
        //返回对应布局
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return titleList.size();//菜单数量
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);//返回对应菜单标题
    }
}
