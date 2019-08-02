package com.owen.zeng.comsrv.activity;

import android.os.SystemClock;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.deemons.serialportlib.ByteUtils;
import com.deemons.serialportlib.SerialPort;
import com.owen.zeng.comsrv.bean.ComRcvBean;

public abstract class SerialPortAct {

    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    //private AckThread mAckThread;
    private MixModeThread mMixModeThread = null;
    private ReentrantLock mLock;
    //private boolean QueryMode = true;

    private boolean t_isReady = false;
    private int t_AckSize = 0;
    private byte[] t_bytes = new byte[1024];


    private class MixModeThread extends Thread{
        private boolean QueryMode = false;
        private int available = 0;
        @Override
        public void run() {
            super.run();

            try{
                while (true) {
                    while (isQueryMode()){
                        while (available != mInputStream.available()) {
                            SystemClock.sleep(2);
                            available = mInputStream.available();
                        }
                        if (available == 0) {
                            SystemClock.sleep(1);
                            continue;
                        }
                        byte[] buffer = new byte[available];
                        mInputStream.read(buffer);
                        t_AckSize = available;
                        System.arraycopy(buffer,0,t_bytes,0,available);
                        t_isReady = true;
                        onDataReceived(buffer,true);
                        available = 0;
                    }

                    while (!isQueryMode()){
                        while (available != mInputStream.available()) {
                            SystemClock.sleep(1);
                            available = mInputStream.available();
                        }
                        if (available == 0) {
                            SystemClock.sleep(1);
                            continue;
                        }
                        byte[] buffer = new byte[available];
                        mInputStream.read(buffer);
                        onDataReceived(buffer,false);
                        available = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isQueryMode() { return QueryMode;}

        public void setQueryMode(boolean value){ QueryMode = value;}
    }

    private class PlusJudgeThread extends Thread{

        @Override
        public void run() {
            super.run();

            int available = 0;
            if (mInputStream == null) return ;

            SystemClock.sleep(20); //waiting input enough

            try{
                while (available != mInputStream.available() ) {
                    SystemClock.sleep(3);
                    available = mInputStream.available();
                }
                byte[] buffer = new byte[available];
                mInputStream.read(buffer);
                t_AckSize = available;
                System.arraycopy(buffer,0,t_bytes,0,buffer.length);
                t_isReady = true;
                onDataReceived(buffer,true);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class AckThread implements Callable {
        private int AckSize;
        private byte[] AckBuffer;

        public AckThread(int size){
            AckSize = size;
            AckBuffer = new byte[AckSize];
        }

        @Override
        public byte[] call() {
            int available = 0;
            if (mInputStream == null) return null;

            try{
                    available = mInputStream.available();
                    while (available < AckSize ) {
                        SystemClock.sleep(1);
                        available = mInputStream.available();
                    }
                    mInputStream.read(AckBuffer, 0, available);
                    onDataReceived(AckBuffer,true);
                }
                catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return AckBuffer;
            }
    }

    public boolean isOpened(){
        return mSerialPort.isOpened;
    }

    private void DisplayError(int resourceId) {
        /*
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SerialPortAct.this.finish();
            }
        });
        b.show();
        */
    }

    public SerialPort Open(String path, int baudRate, int parity, int dataBits, int stopBit) {
        try
        {
            mSerialPort = new SerialPort(new File(path), baudRate, parity, dataBits, stopBit,0);
            if (mSerialPort.isOpened ) {
                mInputStream = mSerialPort.getInputStream();
                mOutputStream = mSerialPort.getOutputStream();
                mLock = new ReentrantLock();

                mMixModeThread = new MixModeThread();
                mMixModeThread.setQueryMode(false);
                mMixModeThread.start();
            }
        } catch (SecurityException e) {
            //DisplayError(R.string.error_security);
        } catch (IOException e) {
            //DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            //DisplayError(R.string.error_configuration);
        }
        return mSerialPort;
    }

    protected abstract void onDataReceived(final byte[] buffer,boolean isQueryMode);

    public boolean SendData(byte[] sData) throws IOException, InterruptedException{
        boolean Result = false;

        if (mLock.tryLock(200, TimeUnit.MILLISECONDS)){
            try {
                mOutputStream.write(sData);
                Result = true;
            }finally {
                mLock.unlock();
            }
        }
        return Result;
    }

    public boolean SendDataAndRcv(byte[] sData, int timeOut, ComRcvBean ackData) throws IOException, InterruptedException{
        boolean Result = false;

        /* use Java new thread feature, but can't get expect result,may bug exist in it's future class
        if (mLock.tryLock(100, TimeUnit.MILLISECONDS)){
            try{
                if (QueryMode) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    AckThread mAckThread = new AckThread(sData.length);
                    Future<byte[]> future = executor.submit(mAckThread);
                    executor.shutdown();
                    mOutputStream.write(sData);
                    byte[] AckResult = future.get(timeOut,TimeUnit.MILLISECONDS);
                    if (AckResult != null) {
                        ackData.Received = new byte[AckResult.length];
                        ackData.Msg.append(String.format("Rcv: %s", ByteUtils.bytesToHexString(AckResult)));
                        System.arraycopy(AckResult, 0, ackData.Received, 0, AckResult.length);
                    }
                    //String tmpStr = ByteUtils.bytesToHexString(AckResult);
                    //future = null;
                    Result = true;
                }
            }
            catch (TimeoutException e) {
                ackData.Msg.append("Serial应答超时");
            }
            catch (ExecutionException e){
                ackData.Msg.append("Serial监听出错");
            }
            finally{
                mLock.unlock();
            }
        } else {
            ackData.Msg.append("锁定超时");
        }
        */


        if (mLock.tryLock(100, TimeUnit.MILLISECONDS)){
            try{
                //for check PlusJudge thread ;ReplyThread mAckThread = new ReplyThread();
                //PlusJudgeThread mAckThread = new PlusJudgeThread();
                //new Thread(mAckThread).start();
                //MixModeThread mAckThread = new MixModeThread();
                //mAckThread.setQueryMode(true);
                //mAckThread.start();
                //t_AckSize = replySize;

                mMixModeThread.setQueryMode(true);
                t_isReady = false;
                mOutputStream.write(sData);
                long startTime = System.currentTimeMillis();

                while (!t_isReady && System.currentTimeMillis() - startTime < timeOut){
                    SystemClock.sleep(1);
                }

                if (t_isReady){
                    //mAckThread.setQueryMode(false);
                    mMixModeThread.setQueryMode(false);
                    ackData.Received = new byte[t_AckSize];
                    System.arraycopy(t_bytes, 0, ackData.Received, 0, t_AckSize);
                    ackData.Msg.append(String.format("Rcv: %s \r\n", ByteUtils.bytesToHexString(ackData.Received)));
                    Result = true;
                }else{
                    ackData.Msg.append("串口接收超时\r\n");
                    Result = false;
                }
            }
            finally{
                mLock.unlock();
            }
        } else {
            ackData.Msg.append("锁定超时\r\n");
        }
        return Result;
    }

    public void Close(){
        try {
            if (mMixModeThread != null) mMixModeThread.isInterrupted();

            mInputStream.close();
            mOutputStream.close();
            //isOpened = false;
            //mSerialPort.close();
            mSerialPort = null;
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
