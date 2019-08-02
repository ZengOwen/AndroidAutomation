package com.owen.zeng.comsrv.activity;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.owen.zeng.comsrv.R;
import com.owen.zeng.comsrv.adapter.LeftAdapter;
import com.owen.zeng.comsrv.bean.LeftDetailBean;
import com.owen.zeng.comsrv.bean.LeftHeadBean;
import com.owen.zeng.comsrv.bean.MessageBean;
import com.owen.zeng.comsrv.bean.ComRcvBean;
import com.deemons.serialportlib.ByteUtils;
import com.deemons.serialportlib.SerialPort;
import com.deemons.serialportlib.SerialPortFinder;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.owen.zeng.comsrv.activity.Bd3201;

/**
 * author： deemons
 * date:    2018/5/26
 * desc:
 */
public class MainPresenter implements MainContract.IPresenter,Serializable{

    private final String mDateFormat = "HH:mm:ss:SSS";
    MainContract.IView mView;

    private LinkedHashSet<String> mSendHistory;

    private String mPath;
    private int    mBaudRate;
    private int    mCheckDigit;
    private int    mDataBits;
    private int    mStopBit;

    private boolean isHexReceive;
    private boolean isHexSend;
    private boolean isShowSend;
    private boolean isShowTime;
    private int     mRepeatDuring;

    private boolean mOpenPort;
    private boolean mAddCrc;

    private SerialPort                mSerialPort;
    private boolean                   isInterrupted;
    private Disposable                mReceiveDisposable;
    private ObservableEmitter<String> mEmitter;
    private Disposable                mSendDisposable;
    private Disposable                mSendRepeatDisposable;

    private SerialPortAct mSerialPortAct;
    private Bd3201 mBd;

    private boolean checkReady;
    private StringBuilder checkResult = new StringBuilder();
    private int chkneed = 500;
    private int chkTimeOut = 1000;


    public MainPresenter(MainContract.IView view) {
        mView = view;

        mOpenPort = SPUtils.getInstance().getBoolean(SPKey.CONF_DIRCOPEN_PORT,false);
        mAddCrc = SPUtils.getInstance().getBoolean(SPKey.CONF_ADD_CRC,false);

        isHexReceive = SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_TYPE, true);
        isHexSend = SPUtils.getInstance().getBoolean(SPKey.SETTING_SEND_TYPE, true);
        isShowSend = SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_SHOW_SEND, true);
        isShowTime = SPUtils.getInstance().getBoolean(SPKey.SETTING_RECEIVE_SHOW_TIME, true);
        mRepeatDuring = SPUtils.getInstance().getInt(SPKey.SETTING_RECEIVE_TYPE, 1000);

        mBd = new Bd3201(1);
        /*
        try {
            mSendHistory = (LinkedHashSet<String>) SPUtils.getInstance()
                .getStringSet(SPKey.SEND_HISTORY, new LinkedHashSet<String>(30));
        } catch (ClassCastException e) {
            e.printStackTrace();
            mSendHistory = new LinkedHashSet<String>(30);
        }
        */
    }

    private void refreshValueFormSp() {
        mPath = SPUtils.getInstance().getString(SPKey.SERIAL_PORT, "");
        mBaudRate = Integer.parseInt(SPUtils.getInstance().getString(SPKey.BAUD_RATE, "9600"));
        mCheckDigit = Integer.parseInt(SPUtils.getInstance().getString(SPKey.CHECK_DIGIT, "0"));
        mDataBits = Integer.parseInt(SPUtils.getInstance().getString(SPKey.DATA_BITS, "8"));
        mStopBit = Integer.parseInt(SPUtils.getInstance().getString(SPKey.STOP_BIT, "1"));
    }

    public void getLeftData() {
        refreshValueFormSp();

        ArrayList<MultiItemEntity> list = new ArrayList<>();

        list.add(getOpenPortBean());

        list.add(getLeftSerialPortBean());
        list.add(getLeftBaudRateBean());
        list.add(getLeftCheckDigitBean());
        list.add(getLeftDataBitsBean());
        list.add(getLeftStopBitsBean());

        mView.setLeftData(list);
    }

    @NonNull
    private LeftHeadBean getOpenPortBean() {

        LeftHeadBean bean = new LeftHeadBean(LeftAdapter.TYPE_HEADCHK);
        //bean.imageRes = R.mipmap.ic_check;
        bean.title = "自动打开串口";
        bean.spKey = SPKey.CONF_DIRCOPEN_PORT;
        bean.value = mOpenPort ? "1" : "0";

        return bean;
        }

    private LeftHeadBean getLeftSerialPortBean() {
        ArrayList<LeftDetailBean> list = new ArrayList<>();

        SerialPortFinder finder = new SerialPortFinder();
        String[] path = finder.getAllDevicesPath();
        for (String s : path) {
            if (TextUtils.isEmpty(mPath)) {
                mPath = s;
                SPUtils.getInstance().put(SPKey.SERIAL_PORT, mPath);
            }

            if (s.equals(mPath)) {
                list.add(new LeftDetailBean(s, true));
            } else {
                list.add(new LeftDetailBean(s));
            }
        }

        LeftHeadBean bean = new LeftHeadBean();
        bean.imageRes = R.mipmap.ic_serial_port;
        bean.title = "串口";
        bean.spKey = SPKey.SERIAL_PORT;
        bean.value = mPath;

        for (LeftDetailBean detailBean : list) {
            bean.addSubItem(detailBean);
        }

        return bean;
    }

    private LeftHeadBean getLeftBaudRateBean() {
        ArrayList<LeftDetailBean> list = new ArrayList<>();

        int[] array = Utils.getApp().getResources().getIntArray(R.array.baud_rate);
        for (int i : array) {
            if (i == mBaudRate) {
                list.add(new LeftDetailBean(String.valueOf(i), true));
            } else {
                list.add(new LeftDetailBean(String.valueOf(i)));
            }
        }

        LeftHeadBean bean = new LeftHeadBean();
        bean.imageRes = R.mipmap.ic_baud;
        bean.title = "波特率";
        bean.spKey = SPKey.BAUD_RATE;
        bean.value = String.valueOf(mBaudRate);

        for (LeftDetailBean leftDetailBean : list) {
            bean.addSubItem(leftDetailBean);
        }

        return bean;
    }

    private LeftHeadBean getLeftCheckDigitBean() {
        ArrayList<LeftDetailBean> list = new ArrayList<>();

        int[] array = Utils.getApp().getResources().getIntArray(R.array.check_digit);
        for (int i : array) {
            if (i == mCheckDigit) {
                list.add(new LeftDetailBean(String.valueOf(i), true));
            } else {
                list.add(new LeftDetailBean(String.valueOf(i)));
            }
        }

        LeftHeadBean bean = new LeftHeadBean();
        bean.imageRes = R.mipmap.ic_check;
        bean.title = "校验位";
        bean.spKey = SPKey.CHECK_DIGIT;
        bean.value = String.valueOf(mCheckDigit);

        for (LeftDetailBean leftDetailBean : list) {
            bean.addSubItem(leftDetailBean);
        }

        return bean;
    }

    private LeftHeadBean getLeftDataBitsBean() {
        ArrayList<LeftDetailBean> list = new ArrayList<>();

        int[] array = Utils.getApp().getResources().getIntArray(R.array.data_bits);
        for (int i : array) {
            if (i == mStopBit) {
                list.add(new LeftDetailBean(String.valueOf(i), true));
            } else {
                list.add(new LeftDetailBean(String.valueOf(i)));
            }
        }

        LeftHeadBean bean = new LeftHeadBean();
        bean.imageRes = R.mipmap.ic_data;
        bean.title = "数据位";
        bean.spKey = SPKey.DATA_BITS;
        bean.value = String.valueOf(mDataBits);

        for (LeftDetailBean leftDetailBean : list) {
            bean.addSubItem(leftDetailBean);
        }

        return bean;
    }

    private LeftHeadBean getLeftStopBitsBean() {
        ArrayList<LeftDetailBean> list = new ArrayList<>();

        int[] array = Utils.getApp().getResources().getIntArray(R.array.stop_bits);
        for (int i : array) {
            if (i == mStopBit) {
                list.add(new LeftDetailBean(String.valueOf(i), true));
            } else {
                list.add(new LeftDetailBean(String.valueOf(i)));
            }
        }

        LeftHeadBean bean = new LeftHeadBean();
        bean.imageRes = R.mipmap.ic_stop;
        bean.title = "停止位";
        bean.spKey = SPKey.STOP_BIT;
        bean.value = String.valueOf(mStopBit);

        for (LeftDetailBean leftDetailBean : list) {
            bean.addSubItem(leftDetailBean);
        }

        return bean;
    }

    public boolean open() {
        boolean Result = false;
/*
        refreshValueFormSp();
        try {
            mSerialPort =
                new SerialPort(new File(mPath), mBaudRate, mCheckDigit, mDataBits, mStopBit, 0);
        } catch (IOException e) {
            e.printStackTrace();
            ToastUtils.showLong("打开失败！请尝试其它串口！");
        } catch (SecurityException e) {
            e.printStackTrace();
            mView.showPermissionDialog();
        }

        if (mSerialPort != null) {
            isInterrupted = false;
            onReceiveSubscribe();
            onSendSubscribe();
            Result = true;
            ToastUtils.showLong("打开串口成功！");
        }

        mView.setOpen(mSerialPort != null);
        return Result;
        */

        refreshValueFormSp();
        try{
            mSerialPortAct = new SerialPortAct() {
                @Override
                protected void onDataReceived(byte[] buffer,boolean isQueryMode) {
                    String tmpStr = ByteUtils.bytesToHexString(buffer);
                    if (isQueryMode) {
                        mView.addData(MessageBean.TYPE_RECEIVE, DateTime.now().toString(mDateFormat), "QueryAck", tmpStr);

                    } else {
                        mView.addData(MessageBean.TYPE_RECEIVE, DateTime.now().toString(mDateFormat), "Device", tmpStr);
                        checkResult.append(tmpStr);
                        checkReady = true;
                    }
                }
            };
            mSerialPortAct.Open(mPath,mBaudRate,mCheckDigit,mDataBits,mStopBit);
            if (!mSerialPortAct.isOpened()) {
                ToastUtils.showLong(mPath +"打开失败！请尝试其它串口！");
            }
            isInterrupted = false;
            Result = true;
            ToastUtils.showLong("打开串口成功！");
        } catch (SecurityException e) {
            e.printStackTrace();
            mView.showPermissionDialog();
        }

        mView.setOpen(mSerialPortAct.isOpened());
        return Result;
    }

    private void onSendSubscribe() {
        mSendDisposable =
            Observable.create((ObservableOnSubscribe<String>) emitter -> mEmitter = emitter)
                .filter(s -> !TextUtils.isEmpty(s))
                //.doOnNext(s -> mSendHistory.add(s))
                .doOnNext(s -> mSerialPort.getOutputStream()
                          //.write(isHexSend ? ByteUtils.hexStringToBytes(s)
                          //: ByteUtils.stringToAsciiBytes(s)))
                          .write(getSendBytes(isHexSend,mAddCrc,s)) )
                //.doOnNext(s -> disconnect_io("A",s,"a",1000))
                .observeOn(AndroidSchedulers.mainThread())
                .filter(s -> isShowSend)
                .subscribe(s -> {
                    mView.addData(MessageBean.TYPE_SEND,
                        DateTime.now().toString(mDateFormat),"Local", s);
                }, Throwable::printStackTrace);

    }

    private byte[] getSendBytes(boolean isHexSend, boolean autoCrc, String str)
    {
        if (isHexSend)
        {
            return  ByteUtils.hexStringToBytes(str);
        }
        else
        {
            return ByteUtils.stringToAsciiBytes(str);
        }
    }

    public void close() {
        isInterrupted = true;
        disposable(mReceiveDisposable);

        disposable(mSendDisposable);
        mEmitter = null;

        disposable(mSendRepeatDisposable);

        //mSerialPort = null;

        mView.setOpen(false);
        mSerialPortAct.Close();

    }

    private void onReceiveSubscribe() {

        mReceiveDisposable = Flowable.create((FlowableOnSubscribe<byte[]>) emitter -> {
            InputStream is = mSerialPort.getInputStream();
            int available;
            int first;
            while (!isInterrupted
                && mSerialPort != null
                && is != null
                && (first = is.read()) != -1) {
                do {
                    available = is.available();
                    SystemClock.sleep(1);
                } while (available != is.available());

                available = is.available();
                byte[] bytes = new byte[available + 1];
                is.read(bytes, 1, available);
                bytes[0] = (byte) (first & 0xFF);
                emitter.onNext(bytes);
            }
            close();
        }, BackpressureStrategy.MISSING)
            .retry()
            .map(bytes -> isHexReceive ? addSpace(ByteUtils.bytesToHexString(bytes))
                : ByteUtils.bytesToAscii(bytes))
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(it -> mView.addData(MessageBean.TYPE_RECEIVE,DateTime.now().toString(mDateFormat),
                    "Local",it),
                Throwable::printStackTrace);

    }

    private String addSpace(String s) {
        if (s.length() % 2 == 0) {
            StringBuilder builder = new StringBuilder();
            char[] array = s.toCharArray();
            int length = array.length;
            for (int i = 0; i < length; i += 2) {
                if (i != 0 && i <= length - 2) {
                    builder.append(" ");
                }

                builder.append(array[i]);
                builder.append(array[i + 1]);
            }

            return builder.toString();
        }
        return s;
    }

    public boolean sendMsg(String contain) {
        boolean Result = false;
        if (mEmitter != null) {
            mEmitter.onNext(contain.replace(" ", ""));
            Result = true;
        } else {
            ToastUtils.showLong("请先打开串口！");
        }
        return Result;
    }
/*
    public void onDataReceived(byte[] buffer,boolean isQueryMode){
        if (!isQueryMode) {
            mView.addData(MessageBean.TYPE_RECEIVE,DateTime.now().toString(mDateFormat),"Device",ByteUtils.bytesToHexString(buffer));
        }
    }*/

    public ComRcvBean SendAndRcv (String sourceMsg,String from) {

        mView.addData(MessageBean.TYPE_SEND,DateTime.now().toString(mDateFormat),from,sourceMsg);
        ComRcvBean Result = new ComRcvBean();
        try {
            byte[] sBtyes = ByteUtils.hexStringToBytes(sourceMsg);
            if (mSerialPortAct.SendDataAndRcv(sBtyes,500,Result)){
                //String ackMsg = ByteUtils.bytesToHexString(Result.Received).trim();
                Result.State = true;
            }
        }
        catch (IOException e){
            Result.Msg.append("异常IO /r/n");
        }
        catch (InterruptedException e){
            Result.Msg.append("异常中断 /r/n");
        }
        return Result;
    }

    public ComRcvBean Sale(int LineNo,String from) {

        ComRcvBean comRcv = null;
        int count = 0;
        checkResult.setLength(0);
        checkReady = false;
        String onStr = mBd.OnCmd(LineNo);
        String offStr = mBd.OffCmd(LineNo);

        while (!checkReady && count < 3){
            comRcv = SendAndRcv(onStr,from);
            if (!comRcv.State) { break;}
            SystemClock.sleep(800);
            comRcv = SendAndRcv(offStr,from);
            if (!comRcv.State) { break;}

            long startTime = System.currentTimeMillis();
            while (!checkReady && System.currentTimeMillis() - startTime < chkTimeOut){
                SystemClock.sleep(1);
            }

            if (checkReady){
                checkReady = false;
                if (checkResult.length() > 0) {
                    comRcv.Msg.append("出货成功 \r\n");
                    comRcv.State = true;
                    break;
                } else {
                    comRcv.Msg.append("Teribble - Null CheckResult \r\n");
                    comRcv.State = false;
                }
            }else{
                comRcv.Msg.append(String.format("%s - 出货检测超时 \r\n",DateTime.now().toString(mDateFormat)));
                comRcv.State = false;
            }
            count++;
        }

        return comRcv;
    }

    public void refreshSendDuring(int result) {
        SPUtils.getInstance().put(SPKey.SETTING_SEND_DURING, result);
        mRepeatDuring = result;

        //正在运行
        if (mSendRepeatDisposable != null && !mSendRepeatDisposable.isDisposed()) {
            registerSendRepeat(true);
        }
    }

    public void refreshSendRepeat(boolean isChecked) {
        SPUtils.getInstance().put(SPKey.SETTING_SEND_REPEAT, isChecked);
        registerSendRepeat(isChecked);
    }

    public void refreshShowTime(boolean isChecked) {
        SPUtils.getInstance().put(SPKey.SETTING_RECEIVE_SHOW_TIME, isChecked);
        isShowTime = isChecked;
    }

    public void refreshShowSend(boolean isChecked) {
        SPUtils.getInstance().put(SPKey.SETTING_RECEIVE_SHOW_SEND, isChecked);
        isShowSend = isChecked;
    }

    public void refreshSendType(boolean isHex) {
        SPUtils.getInstance().put(SPKey.SETTING_SEND_TYPE, isHex);
        isHexSend = isHex;
    }

    public void refreshReceiveType(boolean isHex) {
        //Cancel it for ASC mode cause crash
        //SPUtils.getInstance().put(SPKey.SETTING_RECEIVE_TYPE, isHex);
        isHexReceive = isHex;
    }

    public void refreshAddCrc(boolean checked){
        SPUtils.getInstance().put(SPKey.CONF_ADD_CRC, checked);
        mAddCrc = checked;
    }

    public void registerSendRepeat(boolean checked) {
        if (mSerialPort == null) {
            ToastUtils.showLong("请先打开串口！");
            return;
        }

        disposable(mSendRepeatDisposable);

        if (checked) {
            mSendRepeatDisposable = Observable.interval(mRepeatDuring, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> sendMsg(mView.getEditText()), Throwable::printStackTrace);
        }
    }

    public void onDestroy(){
        mSerialPortAct.Close();
        close();

        //Maybe cause crash
        //SPUtils.getInstance().put(SPKey.SEND_HISTORY, mSendHistory);
    }

    public String getPort(){
        return mPath;
    }

    private void disposable(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public ArrayList<String> getHistory() {
        return new ArrayList<>(mSendHistory);
    }

}


