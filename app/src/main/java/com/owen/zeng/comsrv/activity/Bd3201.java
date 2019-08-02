package com.owen.zeng.comsrv.activity;

import com.deemons.serialportlib.ByteUtils;
import com.deemons.serialportlib.Crc16mb;

public class Bd3201 implements MainContract.Iio {
    private int BoardCount;
    private int PortCount_oneB = 32;

    public Bd3201(int count){ BoardCount = count;}

    //return HexStr as Ctrl code
    public  String OnCmd(int PNo){
        //Set single switch on;
        //Format is AddressCode(1byte)+FuncCode(1byte)+SwitchBeginNo(2byte)+SwitchCount(2byte);Total 6 byte

        byte[] CtrlCode = new byte[6];
        //CtrlCode = ByteUtils.intToBytes(AddrNo);
        CtrlCode[0] = ByteUtils.intToBytes(getBdNo(PNo))[0];  //AddressCode
        CtrlCode[1] = 0x05;                             //FuncCode
        byte[] BeginNo = ByteUtils.intToBytes(PNo-1);     //SwitchNo need - 1 for human habit
        CtrlCode[2] = BeginNo[1];
        CtrlCode[3] = BeginNo[0];
        CtrlCode[4] = (byte) 0xff;
        CtrlCode[5] = 0x00;

        String sCtrl = ByteUtils.bytesToHexString(CtrlCode);
        String sCtrlC = new Crc16mb().getCrc(sCtrl);

        return  sCtrl+sCtrlC;
    }

    public String OnCmd(int[] PNo){
        return "";
    }

    public String OffCmd(int PNo){
        //Set single switch off;
        //Format is AddressCode(1byte)+FuncCode(1byte)+SwitchBeginNo(2byte)+SwitchCount(2byte);Total 6 byte

        byte[] CtrlCode = new byte[6];
        CtrlCode[0] = ByteUtils.intToBytes(getBdNo(PNo))[0];  //AddressCode
        CtrlCode[1] = 0x05;                             //FuncCode
        byte[] BeginNo = ByteUtils.intToBytes(PNo-1);     //SwitchNo need - 1 for human habit
        CtrlCode[2] = BeginNo[1];
        CtrlCode[3] = BeginNo[0];
        CtrlCode[4] = 0x00;
        CtrlCode[5] = 0x00;

        String sCtrl = ByteUtils.bytesToHexString(CtrlCode);
        String sCtrlC = new Crc16mb().getCrc(sCtrl);

        return sCtrl+sCtrlC;
    }

    public String OffCmd(int[] PNo){
        return "";
    }

    public  String getStautsCmd(int PNo){
        //Read switch status;
        //Format is AddressCode(1byte)+FuncCode(1byte)+SwitchBeginNo(2byte)+SwitchCount(2byte);Total 6 byte

        byte[] CtrlCode = new byte[6];
        CtrlCode[0] = ByteUtils.intToBytes(getBdNo(PNo))[0];  //AddressCode
        CtrlCode[1] = 0x01;                             //FuncCode
        byte[] BeginNo = ByteUtils.intToBytes(PNo);     //SwitchBeginNo
        CtrlCode[2] = BeginNo[1];
        CtrlCode[3] = BeginNo[0];
        CtrlCode[4] = 0x00;
        CtrlCode[5] = 0x01;

        String sCtrl = ByteUtils.bytesToHexString(CtrlCode);
        String sCtrlC = new Crc16mb().getCrc(sCtrl);

        return sCtrl+sCtrlC;
    }

    public String getStautsCmd(int[] PNo){return  "";}

    public String getAllPortsStatusCmd(){
        //Read all ports status;
        //Format is AddressCode(1byte)+FuncCode(1byte)+PortBeginNo(2byte)+PortCount(2byte);Total 6 byte

        byte[] CtrlCode = new byte[6];
        CtrlCode[0] = 0x00;  //AddressCode
        CtrlCode[1] = 0x01;                             //FuncCode
        byte[] BeginNo = ByteUtils.intToBytes(0);     //SwitchBeginNo
        CtrlCode[2] = BeginNo[1];
        CtrlCode[3] = BeginNo[0];
        CtrlCode[4] = 0x00;
        CtrlCode[5] = 0x08;

        String sCtrl = ByteUtils.bytesToHexString(CtrlCode);
        String sCtrlC = new Crc16mb().getCrc(sCtrl);

        return sCtrl+sCtrlC;
    }

    private int getBdNo(int PNo){
        return (PNo-1)/PortCount_oneB + 1;
    }

}
