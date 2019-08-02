package com.owen.zeng.comsrv.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.owen.zeng.comsrv.R;
import com.owen.zeng.comsrv.adapter.BttmAdapter;
import com.owen.zeng.comsrv.adapter.HistoryAdapter;
import com.owen.zeng.comsrv.adapter.LeftAdapter;
import com.owen.zeng.comsrv.adapter.MsgAdapter;
import com.owen.zeng.comsrv.bean.MessageBean;
import com.deemons.serialportlib.SerialPort;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.oushangfeng.pinnedsectionitemdecoration.callback.OnHeaderClickListener;
import com.owen.zeng.comsrv.webfunc.AppController;
import com.owen.zeng.comsrv.webfunc.UserController;
import com.owen.zeng.websrv.FHttpManager;
import com.owen.zeng.comsrv.bean.ComRcvBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MainActivity extends AppCompatActivity implements MainContract.IView {

    private MainPresenter mPresenter;
    private RecyclerView  mMainRv;
    private EditText      mEditText;
    private MsgAdapter    mMsgAdapter;
    private LeftAdapter   mLeftAdapter;
    private MenuItem      mMenuItem;
    private Toolbar       mToolbar;
    private boolean       isOpen;
    private boolean       isShowBottom;
    private boolean       willShow;

    private boolean isFollow = true;//信息跟随到底部
    private LinearLayoutManager mMainLayoutManager;
    private FHttpManager mHttpManager;

    private BttmAdapter   mBttmAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new MainPresenter(this);

        mHttpManager = FHttpManager.init(this, UserController.class, AppController.class);
        mHttpManager.setPort(9999);
        mHttpManager.setAllowCross(true);
        mHttpManager.startServer(mPresenter);

        initRv();
        initToolbar();

        KeyboardUtils.registerSoftInputChangedListener(this, height -> {
            if (isShowBottom && height != 0) {
                showBottom(false);
            } else if (height == 0 && willShow) {
                showBottom(true);
                willShow = false;
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRv() {
        mMainRv = findViewById(R.id.main_rv);

        mMsgAdapter = new MsgAdapter(new ArrayList<>());
        mMainLayoutManager = new LinearLayoutManager(this);
        mMainRv.setLayoutManager(mMainLayoutManager);
        mMainRv.setAdapter(mMsgAdapter);
        GestureDetector gestureDetector =
            new GestureDetector(this, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (KeyboardUtils.isSoftInputVisible(MainActivity.this)) {
                        KeyboardUtils.hideSoftInput(MainActivity.this);
                    }
                    if (isShowBottom) {
                        showBottom(false);
                    }
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                    float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY) {
                    return false;
                }
            });
        mMainRv.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        mMainRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    //获取最后一个可见view的位置
                    int lastItemPosition = mMainLayoutManager.findLastVisibleItemPosition();
                    isFollow = mMsgAdapter.getItemCount() - lastItemPosition < 2;
                }
            }
        });

        RecyclerView leftRv = findViewById(R.id.left_rv);
        mLeftAdapter = new LeftAdapter(new ArrayList<>());

        leftRv.addItemDecoration(
            new PinnedHeaderItemDecoration.Builder(LeftAdapter.TYPE_HEAD).disableHeaderClick(false)
                .setHeaderClickListener(new OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(View view, int id, int position) {
                        mLeftAdapter.collapseOrExpand(mLeftAdapter.getItem(position));
                    }

                    @Override
                    public void onHeaderLongClick(View view, int id, int position) {

                    }

                    @Override
                    public void onHeaderDoubleClick(View view, int id, int position) {

                    }
                })
                .create());
        leftRv.setLayoutManager(new LinearLayoutManager(this));
        leftRv.setAdapter(mLeftAdapter);
        mPresenter.getLeftData();

        mEditText = findViewById(R.id.main_edit);

        mBttmAdapter = new BttmAdapter(getSupportFragmentManager(),mPresenter);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.removeAllTabs();

        viewPager = findViewById(R.id.vpage);
        viewPager.setAdapter(mBttmAdapter);
        //设置TabLayout的模式
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        //让tablayout和Viewpager关联;
        tabLayout.setupWithViewPager(viewPager);

        //findViewById(R.id.main_history).setOnClickListener(this::onClickHistory);
        //findViewById(R.id.send_repeat).setOnClickListener(this::onClickRepeatDuring);
        //showBottom(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            mMsgAdapter.setNewData(new ArrayList<>());
            return true;
        } else if (id == R.id.action_run) {
            mMenuItem = item;
            if (isOpen) {
                mPresenter.close();
                mToolbar.setTitle(R.string.title_activity_main);
            } else {
                if (mPresenter.open()) mToolbar.setTitle("串口"+mPresenter.getPort());
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            KeyboardUtils.hideSoftInput(this);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setOpen(boolean isOpen) {
        runOnUiThread(() -> {
            if (mMenuItem != null) {
                mMenuItem.setIcon(isOpen ? R.mipmap.ic_start : R.mipmap.ic_close);
            }
            this.isOpen = isOpen;
        });
    }

    @Override
    public void setLeftData(ArrayList<MultiItemEntity> list) {
        mLeftAdapter.setNewData(list);
    }

    @Override
    public void addData(int msgType,String date, String from, String contain) {
        //Log.d("onReceiveSubscribe", "add = "+ messageBean.getContain());
        runOnUiThread(() -> {
        mMsgAdapter.addData(new MessageBean(msgType,date,from,contain));
        //if (isFollow) {
            int position = mMsgAdapter.getItemCount() - 1;
            mMainRv.scrollToPosition(position > 0 ? position : 0);
        //}
        });
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void showPermissionDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_permission, null);
        TextView textView = view.findViewById(R.id.dialog_text);
        String string = getResources().getString(R.string.permission_error);
        EditText editText = view.findViewById(R.id.dialog_path);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                KeyboardUtils.hideSoftInput(this);
            }
            return false;
        });

        textView.setText(String.format(Locale.getDefault(), string, SerialPort.getSuPath()));
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提示")
            .setView(view)
            .setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            })
            .setPositiveButton("确定", (dialog, which) -> {
                if (editText != null && !TextUtils.isEmpty(editText.getText().toString())) {
                    SerialPort.setSuPath(editText.getText().toString());
                    dialog.dismiss();
                }
            })
            .create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public String getEditText() {
        return mEditText.getText().toString();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isOpen) {
            mPresenter.close();
        } else if (isShowBottom) {
            showBottom(false);
        } else {
            super.onBackPressed();
        }
    }

    private void showBottom(boolean isShow) {
        View view = findViewById(R.id.main_bottom_contain);
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
        isShowBottom = isShow;
    }

    public void onClickHistory(View view) {
        View inflateView = getLayoutInflater().inflate(R.layout.dialog_history, null);

        RecyclerView recyclerView = inflateView.findViewById(R.id.dialog_history_rv);
        HistoryAdapter historyAdapter = new HistoryAdapter(mPresenter.getHistory());
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog alertDialog =
            new AlertDialog.Builder(this).setTitle("历史记录").setView(inflateView).create();

        historyAdapter.setOnItemClickListener((adapter1, view1, position) -> {
            mEditText.setText(historyAdapter.getData().get(position));
            mEditText.setSelection(mEditText.getText().length());
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    public void onClickMore(View view) {
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this);
            willShow = true;
        } else {
            showBottom(!isShowBottom);
        }
    }

    public void onClickSend(View view) {
        EditText editText = findViewById(R.id.main_edit);
        String contain = editText.getText().toString();
        if (!TextUtils.isEmpty(contain)) {
            //mPresenter.sendMsg(contain);
            mPresenter.SendAndRcv(contain,"Local");
        }
        //editText.getText().clear();
        KeyboardUtils.hideSoftInput(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHttpManager.stopServer();
        mPresenter.onDestroy();
        KeyboardUtils.fixSoftInputLeaks(this);
    }

    public void onClickRepeatDuring(View view) {
        View inflateView = getLayoutInflater().inflate(R.layout.dialog_during, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("输入时间")
            .setView(inflateView)
            .setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            })
            .setPositiveButton("确定", (dialog, which) -> {
                EditText editText = inflateView.findViewById(R.id.during_edit);
                if (editText != null && !TextUtils.isEmpty(editText.getText().toString())) {
                    TextView textView = findViewById(R.id.send_repeat_during);
                    int result = Integer.parseInt(editText.getText().toString().trim());
                    int hour_12 = 1000 * 60 * 60 * 12;
                    textView.setText(String.valueOf(result > hour_12 ? hour_12 : result));
                    mPresenter.refreshSendDuring(result);
                    dialog.dismiss();
                }
            })
            .create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void jumpToGithub(View view) {
        Uri uri = Uri.parse("http://1.zouwei.hk/zouwei/order/#/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public  MainPresenter getmPresenter() {
        return mPresenter;
    }
}
