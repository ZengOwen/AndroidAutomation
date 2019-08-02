## AndroidAutomation 

### 说明

AndroidAutomation 以安卓为控制器,实现了本地控制指令的Web化，以便借助浏览器的跨平台的特性,低成本实现各平台的用户交互。

本程序实现了对COM口的控制, 支持设置 su 路径、串口路径、波特率、校验位、数据位、停止位; 亦可作为串口调试工具。
<br>

### 样例
Android工控机为平台<br>
32路modbus协议继电器控制动作机构<br>
NPN光电管负责出货结果检测<br>

由以上元件构建智能售卖系统原型。

[点我观看视频](http://v.youku.com/v_show/id_XMzg5Mjg5OTY4NA==.html?spm=a2hzp.8253869.0.0)<br>
http://v.youku.com/v_show/id_XMzg5Mjg5OTY4NA==.html?spm=a2hzp.8253869.0.0

### 效果预览
<img src="https://github.com/ZengOwen/AndroidAutomation/blob/master/app/S1.jpeg" width="30%" />   <img src="https://github.com/ZengOwen/AndroidAutomation/blob/master/app/S2.jpeg" width="30%" />   <img src="https://github.com/ZengOwen/AndroidAutomation/blob/master/app/S3.jpeg" width="30%" />  
<br>

### 使用

打开串口

   ```java
   SerialPort mSerialPort = new SerialPort("/dev/ttyS1", 9600);
   //获取串口文件的输入输出流，以便数据的收发
   InputStream is = mSerialPort.getInputStream();
   OutputStream os = mSerialPort.getOutputStream();
   ```
<br>

### API

#### 设置 su 路径

Android 主板在与其它硬件进行串口通信时，串口作为底层实现，Android 系统把设备作为一个文件，与其他设备进行串口通信就相当于读写此文件。

因此需要 root 权限来操作串口文件，默认的 su 文件路径在 `/system/bin/su` ，你可以重新设置 su 的文件路径，以便获取 root 权限，例如，设置路径`/system/xbin/su`

```java
//需要在打开串口前调用
SerialPort.setSuPath("/system/xbin/su");
```
<br>

#### 查看串口设备列表

Android 串口文件都在 ``/proc/tty/drivers`` 目录下，因此可以获取所有串口文件。

```java
SerialPortFinder serialPortFinder = new SerialPortFinder();
String[] allDevices = serialPortFinder.getAllDevices();
String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
```

<br>

#### 打开串口

如果你需要设置更多参数，请使用以下构造函数

```java
/**
 * 打开串口
 * @param device 串口设备文件
 * @param baudRate 波特率
 * @param parity 奇偶校验，0 None（默认）； 1 Odd； 2 Even
 * @param dataBits 数据位，5 ~ 8  （默认8）
 * @param stopBit 停止位，1 或 2  （默认 1）
 * @param flags 标记 0（默认）
 */
public SerialPort(File device, int baudRate, int parity, int dataBits, int stopBit, int flags)
```

检验位一般默认是0（NONE），数据位一般默认为8，停止位默认为1。

<br>

#### 读写串口

##### 读数据

```java
// 配合 Rxjava2 ，处理异常更方便
mReceiveDisposable = Flowable.create((FlowableOnSubscribe<byte[]>) emitter -> {
    InputStream is = mSerialPort.getInputStream();
    int available;
    int first;
    while (!isInterrupted && mSerialPort != null 
           && is != null && (first = is.read()) != -1) {
        do {
            available = is.available();
            SystemClock.sleep(1);
        } while (available != is.available());

        byte[] bytes = new byte[is.available()+1];
        is.read(bytes,1,is.available());
        bytes[0] = (byte) (first & 0xFF);
        emitter.onNext(bytes);
    }
    close();
}, BackpressureStrategy.MISSING)
```


##### 写数据

```java
//获取输出流
OutputStream os = mSerialPort.getOutputStream();
os.write(ByteUtils.hexStringToBytes("CCAA0300"));
```

<br>

#### 32路继电器应答模式 及 执行结果触发

使用Rxjava2接受数据时,为定时触发,触发周期太长,响应太慢,触发周期太短,会造成完整的返回数据被切割,改用FutureTask,但只能正常触发一次,于是自己实现带超时控制的快速响应, 解决了内容完整与高效的统一

```java
    public boolean SendDataAndRcv(byte[] sData, int timeOut, ComRcvBean ackData) throws IOException, InterruptedException{
        boolean Result = false;

        // use Java new thread feature, but can't get expect result,may bug exist in it's future class

        if (mLock.tryLock(100, TimeUnit.MILLISECONDS)){
            try{

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
```
<br>

#### 通过对本Web框架路由的映射,实现Web请求与本地功能链接

```java
    @ResponseBody
    @RequestMapping("Sale")
    public ResultBean Sale(@RequestParam("LNo") String LNo) {
        ResultBean Result = null;

        int PortNo = Integer.parseInt(LNo);
        if (PortNo < 1 || PortNo > 64){
            Result = new ResultBean(404,"货道编号超出范围",null);
            return Result;
        }

        ComRcvBean ComRcv = dCom.Sale(Integer.parseInt(LNo),"Web");
        if (ComRcv.State){
            Result = new ResultBean(200,"OK",ComRcv);
        }else{
            Result = new ResultBean(500,"Error",ComRcv);
        }
        return Result;
    }
```