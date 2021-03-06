package com.serenegiant.entiy;

import com.serenegiant.net.CommonInfo;

/**
 * Created by Administrator on 2016/8/19.
 */
public class PhotoHeadInformation {
    public byte getUpMode() {
        return upMode;
    }

    public void setUpMode(byte upMode) {
        this.upMode = upMode;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(byte size) {
        this.size = size;
    }

    public byte getEventType() {
        return eventType;
    }

    public void setEventType(byte eventType) {
        this.eventType = eventType;
    }

    public String getStuNumber() {
        return stuNumber;
    }

    public void setStuNumber(String stuNumber) {
        this.stuNumber = stuNumber;
    }

    public byte[] getTakeID() {
        return takeID;
    }

    public void setTakeID(byte[] takeID) {
        this.takeID = takeID;
    }

    public byte[] getGpsInfo() {
        return gpsInfo;
    }

    public void setGpsInfo(byte[] gpsInfo) {
        this.gpsInfo = gpsInfo;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public PhotoHeadInformation()
    {
        upMode = 1;
        channel = 0;
        size = 2;
        eventType = 5;
//        stuDrivingNo = new String(CommonInfo.getStuDrivingNumber());
//        stuNumber = new String(CommonInfo.getStuNumber());
//        stuLoginNumber = CommonInfo.getStuLoginNumber(false);
//        takeID = CommonInfo.getPictureNumber(false);
//        gpsInfo = CommonInfo.getGpsData();
        state = 0;
    }

    public PhotoHeadInformation(String path)
    {
        super();
        upMode = 1;
        channel = 0;
        size = 2;
        eventType = 5;
        //stuNumber = new String(CommonInfo.getStuNumber()==null?"0".getBytes():CommonInfo.getStuNumber());
        takeID = String.format("%010d", System.currentTimeMillis()/1000).getBytes();
        //gpsInfo = CommonInfo.getGpsData();
        gpsInfo = CommonInfo.getGpsPackage();
        state = 0;
        savePath = path;
    }

    byte upMode;//上传模式
    byte channel;//摄像头通道号
//    SQCIF（160x120）	0
//    QCIF（176x144）	1
//    CIF（352x288）	2
//    QQVGA（160x120）	3
//    QVGA（320x240）	4
//    VGA（640x480）	5
    byte size;//图像尺寸
//    0：中心查询的图片
//    1：紧急报警主动上传的图片
//    2：关车门后达到指定车速主动上传的图片
//    3：侧翻报警主动上传的图片
//    4：上客
//    5：定时拍照
//    6：进区域
//    7：出区域
//    8：事故疑点(紧急刹车)
//    9：开车门
//    17(0x11)--学员登录拍照
//    18(0x12)--学员登出拍照
//    19(0x13)--学员培训过程中拍照
    byte eventType;//发起图片的事件类型
    String stuNumber;//学员编号

    public long getClassID() {
        return classID;
    }

    public void setClassID(long classID) {
        this.classID = classID;
    }

    long classID;
    byte[] takeID;//拍摄编号
    byte[] gpsInfo;//完整GPS数据包
    String savePath;//保存地址
    byte state; //上传状态 0未上传

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    String createTime;

    byte[] gpsDate; //GNSS数据

    public int getIsBlindArea() {
        return isBlindArea;
    }

    public void setIsBlindArea(int isBlindArea) {
        this.isBlindArea = isBlindArea;
    }

    public byte[] getGpsDate() {
        return gpsDate;
    }

    public void setGpsDate(byte[] gpsDate) {
        this.gpsDate = gpsDate;
    }

    int isBlindArea; //是否盲区
}
