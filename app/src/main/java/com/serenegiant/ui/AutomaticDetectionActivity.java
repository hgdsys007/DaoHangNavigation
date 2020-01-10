package com.serenegiant.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.navigation.timerterminal.BuildConfig;
import com.navigation.timerterminal.R;
import com.serenegiant.AppConfig;
import com.serenegiant.AppContext;
import com.serenegiant.dataFormat.UpdateInfo;
import com.serenegiant.db.SQLiteHelper;
import com.serenegiant.entiy.DeviceInfoRequest;
import com.serenegiant.entiy.DeviceInfoResponse;
import com.serenegiant.entiy.FaceOpenResponse;
import com.serenegiant.entiy.GPSInfo;
import com.serenegiant.entiy.MinuteRecord;
import com.serenegiant.entiy.StudentAndCoachInfoRequest;
import com.serenegiant.entiy.TimerDerminalEvent;
import com.serenegiant.http.HttpConfig;
import com.serenegiant.http.OkHttpClientManager;
import com.serenegiant.net.CommonInfo;
import com.serenegiant.net.DeviceParameter;
import com.serenegiant.net.TcpClient;
import com.serenegiant.receiver.GeoFenceReceiver;
import com.serenegiant.services.NewLocationService;
import com.serenegiant.services.SendPositionTask;
import com.serenegiant.utils.IUtil;
import com.serenegiant.utils.MessageDefine;
import com.serenegiant.utils.MyToast;
import com.serenegiant.utils.SharedPreferencesUtil;
import com.serenegiant.utils.ShellUtils;
import com.serenegiant.utils.SoundManage;
import com.serenegiant.utils.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static com.serenegiant.services.NewLocationService.GEOFENCE_BROADCAST_ACTION;


public class AutomaticDetectionActivity extends BaseActivity {

    private OkHttpClient mOkHttpClient;
    private boolean faceModeGetted = false;

    public static AutomaticDetectionActivity automaticDetectionActivity;

    private String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private static boolean flag = false;
    final String TAG = "AutomaticDetectionActivity";
    Handler refreshHand;
    StringBuilder strdata = new StringBuilder();
    private LocationManager locationManager;
    //    public static String applicationPath;
    boolean checkDeviceValid;
    //    Button btSelectLearn;
    LinearLayout mSettingLl;
    //    Button btDownloadStop;
//    Button btDownloadCancel;
    //    Button btExportTrainFiles;
//    Button btMainQuit;
    TextView tvData;
    TextView tvTrainHeadTime;
    TextView tvTrainNetworkType;
    TextView tvTrainLoginStatel;
    TextView tvSoftwareVersion;
    TextView tvDeviceNumber;
    //    TextView tvDownloadUrl;
    ImageView imgPhoneSem;
    ImageView gpsSignalSem;
    ImageView imei;
    TextView tvSendCount;
    //    ProgressBar downloadApkProgress;
    TelephonyManager Tel;
    MyPhoneStateListener MyListener;
    Thread myThread;
    boolean threadRunFlag;
    Thread updateSoftware;
    Toast toast;
    Timer timer;
    TimerTask task;
    Handler handler;
    Runnable runnable;
    boolean isCheckingSDCard = false;
    boolean isCheckingSelf = false;
    SharedPreferences sendMessageCount;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    //设置系统配置参数
    private AlertDialog mUnLockDialog;
    private AlertDialog mUploadVersionDialog;
    private static final int NEW_VERSION_DOWNLOAD = 13;
    private OkHttpClientManager mOkHttpClientManager;
    LinearLayout mPartTwoLl;
    LinearLayout mPartThreeLl;
    LinearLayout mStudyTimeSearchLl;
    LinearLayout mVideoStudyLl;
    LinearLayout mMockExamLl;
    private Context mContext;
    private int registerflag = -1;
    private boolean isHadRegister;
    private TextView select;

    private NewLocationService mLocationService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_automatic_detection);

        EventBus.getDefault().register(this);

        initPermissions();

        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        mOkHttpClient.setConnectTimeout(10000, TimeUnit.MILLISECONDS);

        mContext = this;
        sendMessageCount = this.getSharedPreferences("SendMessageCount", MODE_PRIVATE);
//        btSelectLearn = (Button) findViewById(R.id.bt_auto_detec_learn);
//        btDownloadStop = (Button) findViewById(R.id.btDownloadStop);
//        btDownloadCancel = (Button) findViewById(R.id.btDownloadCancel);
        tvSendCount = (TextView) findViewById(R.id.tvSendCount);
        tvTrainHeadTime = (TextView) findViewById(R.id.tvTrainHeadTime);
        tvTrainNetworkType = (TextView) findViewById(R.id.tvTrainNetworkType);
        tvTrainLoginStatel = (TextView) findViewById(R.id.tvTrainLoginStatel);
        tvSoftwareVersion = (TextView) findViewById(R.id.tvSoftwareVersion);
        tvDeviceNumber = (TextView) findViewById(R.id.tvDeviceNumber);
        imgPhoneSem = (ImageView) findViewById(R.id.imgPhoneSem);
        gpsSignalSem = (ImageView) findViewById(R.id.gpsSignalSem);
        select = findViewById(R.id.select);
//        tvDownloadUrl = (TextView) findViewById(R.id.tvDownloadUrl);
//        downloadApkProgress = (ProgressBar) findViewById(R.id.downloadApkProgress);
//        downloadApkProgress.setVisibility(View.GONE);
//        btDownloadStop.setVisibility(View.GONE);
//        btDownloadCancel.setVisibility(View.GONE);
//        tvDownloadUrl.setVisibility(View.GONE);
        gpsSignalSem.setImageResource(R.drawable.gps_signal_disable);
        SQLiteHelper.getInstance().openDateBase();
        initParameter();
//        TcpClient.getInstance(this).start();
//        btSelectLearn.setOnClickListener(myLisener);
//        btDownloadStop.setOnClickListener(myLisener);
//        btDownloadCancel.setOnClickListener(myLisener);

        //new version
        mPartTwoLl = (LinearLayout) findViewById(R.id.part_two_ll);
        mPartThreeLl = (LinearLayout) findViewById(R.id.part_three_ll);
        mStudyTimeSearchLl = (LinearLayout) findViewById(R.id.studytime_search_ll);
        mVideoStudyLl = (LinearLayout) findViewById(R.id.video_study_ll);
        mMockExamLl = (LinearLayout) findViewById(R.id.mock_exam_ll);
        mSettingLl = (LinearLayout) findViewById(R.id.bt_auto_detec_examine);
        mPartTwoLl.setOnClickListener(myLisener);
        mPartThreeLl.setOnClickListener(myLisener);
        mStudyTimeSearchLl.setOnClickListener(myLisener);
        mVideoStudyLl.setOnClickListener(myLisener);
        mMockExamLl.setOnClickListener(myLisener);
        mSettingLl.setOnClickListener(myLisener);

        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        //注册SD卡事件监听
        // 在IntentFilter中选择你要监听的行为
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
//        intentFilter.setPriority(1000);// 设置最高优先级
//        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
//        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
//        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为 USB大容量存储被共享，挂载被解除
//        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
//        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
//        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
//        intentFilter.addDataScheme("file");
//        registerReceiver(broadcastRec, intentFilter);// 注册监听函数
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        if (null == automaticDetectionActivity) {
            TcpClient.getInstance(AutomaticDetectionActivity.this).start();
            MessageDefine.isTcpStart = true;
            refreshHand.sendEmptyMessageDelayed(999, 100);
        }
        automaticDetectionActivity = this;

//        startService(new Intent(this, SendPositionService.class));
        // TODO: 2018/8/29 暂时屏蔽
        new Timer().schedule(new SendPositionTask(this), 10 * 1000, 10 * 1000);
        // TODO: 2019-05-22 暂时屏蔽旧定位
//        startService(new Intent(this, LocationService.class));
//        registerGeoFenceReceiver();


//        if(!MessageDefine.isPositionService)
//        {
//            startService(new Intent(this, BDGPSService.class));
//            startService(new Intent(this, SendPositionService.class));
//        }

        MinuteRecord record = checkRecordFile();
        if (record != null) {
            Intent intent;
            if (record.getCourseCode() == (byte) 2) {
                CommonInfo.setCurItem((byte) 2);
                CommonInfo.setTrainMode(2);
                intent = new Intent(mContext, TrainActivity.class);
                intent.putExtra("continueLearn", true);
                startActivity(intent);
            } else if (record.getCourseCode() == (byte) 3) {
                CommonInfo.setCurItem((byte) 3);
                CommonInfo.setTrainMode(2);
                intent = new Intent(mContext, TrainActivity.class);
                intent.putExtra("continueLearn", true);
                startActivity(intent);
            }
        }
    }

    /**
     * 获取人脸识别开关状态
     */
    private void getFaceOpen() {
        RequestBody body = RequestBody.create(null, "");

        Request request = new Request.Builder()
                .url(HttpConfig.faceOpen)
                .post(body)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("GetFaceOpen", "获取人脸识别开关状态失败");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                FaceOpenResponse info;
                if (response != null && response.body() != null) {
                    try {
                        info = new Gson().fromJson(response.body().string(), FaceOpenResponse.class);
                    } catch (Exception e) {
                        info = new FaceOpenResponse(false, "-1");
                    }

                    if (info.isSuccess()) {
                        IUtil.isFaceOpen = info.getResult().equals("1");
                    } else {
                        Log.d("GetFaceOpen", "无法获取人脸识别开关");
                    }
                } else {
                    Log.d("GetFaceOpen", "人脸识别开关状态接口调用失败");
                }

            }
        });
    }

    /**
     * 获取设备信息
     */
    private void getDeviceInfo() {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        // TODO: 2020-01-09 这里的imei号是假的
        RequestBody body = RequestBody.create(mediaType, new Gson().toJson(new DeviceInfoRequest("868704043712345")));
        Request request = new Request.Builder()
                .post(body)
                .url(HttpConfig.deviceInfo)
                .build();

        mOkHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        System.out.println("设备信息获取失败");
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        try {
                            DeviceInfoResponse deviceInfo;
                            if (response != null && response.body() != null) {
                                String result = response.body().string();
                                System.out.println("返回结果为："+ result);
                                if (!result.isEmpty()){
                                    System.out.println("设备信息获取成功");
                                    deviceInfo = new Gson().fromJson(result, DeviceInfoResponse.class);
                                    if (!deviceInfo.getResult().isEmpty()){
                                        System.out.println("设备信息赋值");
                                        DeviceInfoResponse.ResultBean bean = deviceInfo.getResult().get(0);
                                        DeviceParameter.setProviceID(bean.getProvinceId());
                                        DeviceParameter.setCityID(bean.getCityCountyId());
                                        DeviceParameter.setCarColor((byte) bean.getPlateColor());
                                        DeviceParameter.setManufacturerID(bean.getManufacturerId());
                                        DeviceParameter.setDeviceSerial(bean.getSn().getBytes());
                                        DeviceParameter.setDeviceType(bean.getModel());
                                    } else {
                                        System.out.println("未获取到设备信息");
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void registerGeoFenceReceiver() {
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        AppContext.mGeoFenceReceiver = new GeoFenceReceiver();
        registerReceiver(AppContext.mGeoFenceReceiver, filter);
    }

    private MinuteRecord checkRecordFile() {
        List<MinuteRecord> recordList = AppContext.getDaoSession().getMinuteRecordDao().loadAll();
        if (recordList == null || recordList.isEmpty())
            return null;
        else
            return recordList.get(0);
    }


    private void initPermissions() {
        boolean permissionAllGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionAllGranted = false;
                break;
            }
        }

        if (!permissionAllGranted)
            requestPermissions(permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, 1);
        }
    }

    Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NEW_VERSION_DOWNLOAD:
                    Bundle data = msg.getData();
                    int newVersion = data.getInt("version");
                    showUpdataDialog(newVersion);
                    break;
                case 5555:
                    if (!selectnum)
                        break;

                    select.setText("学生刷卡:" + IUtil.signnumStudent + " 教练刷卡:" + IUtil.signnumTeacher +
                            " 未上传照片:" + IUtil.getphotoNun + " 位置记录:" + IUtil.getlocationNun + " 培训记录:" + IUtil.getclassSignNun
                    );
                    break;
            }
        }
    };

    private void updateVersion() {
        Log.i("zxj", "update version");
        mOkHttpClientManager = OkHttpClientManager.getInstance();
        mOkHttpClientManager.postAysn(HttpConfig.updateVersionUrl, new OkHttpClientManager.ResultCallBack() {
            @Override
            public void sucCallBack(Response response) {
                try {
                    JSONObject mResult = null;
                    try {
                        mResult = new JSONObject(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int errorcode = mResult.optInt("errorcode");
                    String message = mResult.optString("message");
                    if (errorcode == 0) {
                        JSONObject dataJb = mResult.optJSONObject("data");
                        if (null == dataJb) return;
                        int version = dataJb.optInt("version");
//                        int localVersionCode = DeviceParameter.getVerCode(getApplicationContext());
                        int localVersionCode = BuildConfig.VERSION_CODE;
                        if (version != localVersionCode) {
                            //在这里直接调用下载也可以
                            Message msg = mUpdateHandler.obtainMessage();
                            msg.what = NEW_VERSION_DOWNLOAD;
                            Bundle bundle = new Bundle();
                            bundle.putInt("version", version);
                            msg.setData(bundle);
                            msg.sendToTarget();//当使用handler.obtainMessage时，可以使用这个。
                        }
                    } else {
                        Log.i("zxj", message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failCallBack(Exception e) {
                Log.i("zxj", e.toString());
            }
        });
    }

    private void downLoadApk(int version) {
        final ProgressDialog pd;    //进度条对话框
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载更新");
        pd.setCancelable(false);
//        pd.setProgressNumberFormat("%1.2fM/%2.2fM");
        pd.show();
        mOkHttpClientManager.downAysn(HttpConfig.getNewApk + version + "/" + HttpConfig.code + "/2", AppConfig.UPDATE_VERSION, "release_update.apk", pd, new OkHttpClientManager.ResultCallBack() {
            @Override
            public void sucCallBack(Response response) {
                pd.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AutomaticDetectionActivity.this, "正在安装,请稍后", Toast.LENGTH_SHORT).show();
                    }
                });
                installApkSilence(AppConfig.UPDATE_VERSION + "/release_update.apk");
//                loadPackageTask(AutomaticDetectionActivity.this);
            }

            @Override
            public void failCallBack(Exception e) {
                pd.dismiss();
                Log.i("zxj", "fail download" + e.toString());
            }
        });

    }

    View.OnClickListener myLisener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.bt_auto_detec_examine:
                    startActivity(new Intent(AutomaticDetectionActivity.this, SetChangeActivity.class));
                    break;
                case R.id.part_two_ll:
                    if (checkDeviceValid) {
                        if (!isHadRegister) {
                            Toast.makeText(AutomaticDetectionActivity.this, "设备未登录", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        CommonInfo.setCurItem((byte) 2);
                        CommonInfo.setTrainMode(2);
//                        intent = new Intent(mContext, RainingActivity.class);
                        intent = new Intent(mContext, TrainActivity.class);
                        startActivity(intent);
                    } else {
                        toast = Toast.makeText(AutomaticDetectionActivity.this, "设备初始化未完成，请等待初始化", Toast.LENGTH_SHORT);
                        toast.show();
                        play("设备初始化未完成，请等待初始化", 500);
                    }
                    break;
                case R.id.part_three_ll:
                    if (checkDeviceValid) {
                        if (!isHadRegister) {
                            Toast.makeText(AutomaticDetectionActivity.this, "设备未登录", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        CommonInfo.setCurItem((byte) 3);
                        CommonInfo.setTrainMode(2);
//                        intent =new Intent(mContext, RainingActivity.class);
                        intent = new Intent(mContext, TrainActivity.class);
                        startActivity(intent);
                    } else {
                        toast = Toast.makeText(AutomaticDetectionActivity.this, "设备初始化未完成，请等待初始化", Toast.LENGTH_SHORT);
                        toast.show();
                        play("设备初始化未完成，请等待初始化", 500);
                    }
                    break;
                case R.id.studytime_search_ll:
                    MyToast.show(mContext, "敬请期待");
                    break;
                case R.id.video_study_ll:
                    MyToast.show(mContext, "敬请期待");
                    break;
                case R.id.mock_exam_ll:
                    MyToast.show(mContext, "敬请期待");
                    break;
            }
        }
    };

    private boolean checkRegisterStatus(int flag) {
        if (flag == 1) {
            MyToast.show(this, "车辆已被注册");
            return false;
        } else if (flag == 2) {
            MyToast.show(this, "数据库中无该车辆");
            return false;
        } else if (flag == 3) {
            MyToast.show(this, "终端已被注册");
            return false;
        } else if (flag == 4) {
            MyToast.show(this, "数据库中无该终端");
            return false;
        } else if (flag == -1) {
            MyToast.show(this, "请稍后");
            return false;
        }
        return true;
    }

    static boolean sdcardAvailabilityDetected = false;
    static boolean sdcardAvailable = false;

    //检测SD卡状态
    public static synchronized boolean detectSDCardAvailability() {
        boolean result = false;
        try {
            Date now = new Date();
            long times = now.getTime();
            String fileName = AppConfig.EXAMINE_RECORD_LIST_DIRECTORY + File.separator + String.valueOf(times) + ".test";
            File file = new File(fileName);
            result = file.createNewFile();
            file.delete();
        } catch (Exception e) {
            // Can't create file, SD Card is not available
            e.printStackTrace();
        } finally {
            sdcardAvailabilityDetected = true;
            sdcardAvailable = result;
        }
        return result;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("AutomaticDetection Page")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    void initParameter() {

        refreshHand = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MessageDefine.REFRESH_SECOND_1:
                        String sTime;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        sTime = sdf.format(new Date());
                        tvTrainHeadTime.setText(sTime);
                        int phoptCount = sendMessageCount.getInt("PhoptCount", 0);
                        int positionCount = sendMessageCount.getInt("PositionCount", 0);
                        int trainCount = sendMessageCount.getInt("TrainCount", 0);
                        String sendMessageCountStr = "T:" + trainCount + " P:" + phoptCount + " G:" + positionCount;
                        tvSendCount.setText(sendMessageCountStr);
                        if (CommonInfo.getDeviceLoginState() == 1) {
                            tvTrainLoginStatel.setText("已登录");

                        } else {
                            tvTrainLoginStatel.setText("未登录");
                        }
                        if (GPSInfo.isValid()) {
                            gpsSignalSem.setImageResource(R.drawable.gps_signal_enable);
                            //play("GPS连接成功",300);
                        } else {
                            gpsSignalSem.setImageResource(R.drawable.gps_signal_disable);
                            //play("GPS连接失败",300);
                        }
                        break;
                    case 999:
                        //第一次点击保存时, this way
                        getDeviceInfo();
                        break;
                    case MessageDefine.MSG_PHONE_SEM_CHANGED:
                        switch (Tel.getNetworkType()) {
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                                //case TelephonyManager.NETWORK_TYPE_GSM:
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                tvTrainNetworkType.setText("2G网络");
                                break;
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                                tvTrainNetworkType.setText("3G网络");
                                break;
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                tvTrainNetworkType.setText("4G网络");
                                break;
                            default:
                                if (isWifi(AutomaticDetectionActivity.this)) {
                                    tvTrainNetworkType.setText("WIFI");
                                } else {
                                    tvTrainNetworkType.setText("未知?");
                                }
                                break;
                        }
                        switch (currentPhoneSem) {
                            case 0:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem0);
                                break;
                            case 1:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem1);
                                break;
                            case 2:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem2);
                                break;
                            case 3:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem3);
                                break;
                            case 4:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem4);
                                break;
                            case 5:
                                imgPhoneSem.setImageResource(R.drawable.phone_sem5);
                                break;
                        }
                        break;
                    case MessageDefine.MSG_AUTOMATIC_LATEST_VERSION: //当前版本为最新版本
                        notNewVersionDlgShow(); // 提示当前为最新版本
                        break;
                    case MessageDefine.MSG_GET_UNDATAINFO_ERROR:
                        //服务器超时
                        Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", Toast.LENGTH_SHORT).show();
                        break;
                    case MessageDefine.MSG_TCP_THREAD_START:
                        Log.v(TAG, "tcp thread is start");
                        break;
                    case MessageDefine.MSG_AUTOMATIC_ENTER_EXAMINE:
                        Intent intent = new Intent();
                        intent.setClass(AutomaticDetectionActivity.this, RainingActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        };

        String pathDirectory = AppConfig.PARAMETER_SAVE_PATH;
        String pathFile = pathDirectory + "/DeviceParameter.xml";
        DeviceParameter.setParameterFilePath(pathFile);
//        DeviceParameter.initParameter();  //初始化参数为默认值
        File f = new File(pathDirectory);
        if (f.exists() == false) {
            f.mkdirs();//注意是mkdirs()有个s 这样可以创建多重目录。
        }
        File f2 = new File(pathFile);//获取参数XML文件
        if (f2.exists() == false) {
            DeviceParameter.createParameterXML();//创建初始XML文件
        }
        DeviceParameter.getParameterData();
        checkDeviceValid = false;
        threadRunFlag = true;
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (threadRunFlag && System.currentTimeMillis() < 1473393210865L)//2016/09/09 11:54
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                checkDeviceValid = true;
            }
        }.start();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 判断GPS是否正常启动
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //  openGPS(this);//强制打开gps
            new AlertDialog.Builder(AutomaticDetectionActivity.this)
                    .setTitle(R.string.gps_title_tip)
                    .setMessage(R.string.gps_msg_gps_not_open)
                    .setPositiveButton(R.string.gps_btn_yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // 返回开启GPS导航设置界面、
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, 0);
                                    try {
                                        for (int k = 0; k < 10; k++) {
                                            Thread.sleep(1000);
                                            if (!(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
                                                dialog.dismiss();
                                                Thread.sleep(2500);
//                                                    new InitCheck().start();
                                                break;
                                            }
                                        }
                                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                            finish();
                                            System.exit(0);
                                            return;
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                            })
                    .setNegativeButton(R.string.gps_btn_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    onBackPressed();
                                    finish();
                                    System.exit(0);
                                    return;
                                }
                            }).show();
        }

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String identity = preference.getString("identity", null);
        if (identity == null) {
            identity = UUID.randomUUID().toString();
            preference.edit().putString("identity", identity);
        }
        getRegisterParameter();
        CommonInfo.setGuid(identity);
        //addDefaultCardInfo();//防止APP重启后NullPointException
//        Intent intent = new Intent(AutomaticDetectionActivity.this, GPSService.class);
//        startService(intent);
    }

    private static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /*
     * 证书验证
     * */
    void getRegisterParameter() {
        String sData = Utils.readFile(AppConfig.REGISTER_SAVE_PATH + File.separator + "register.info");
        if (sData != null && sData.length() > 0) {
            CommonInfo.setTelnetDeviceNumber(sData.substring(0, 16));
            CommonInfo.setCertificationPassword(sData.substring(17, sData.length()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //版本升级
        updateVersion();
//        installApkSilence(AppConfig.UPDATE_VERSION + "/release_update.apk");
        selectnum = true;

        //获取人脸识别开关状态
        getFaceOpen();

        new selectNUM().start();

        isHadRegister = getSharedPreferences(SharedPreferencesUtil.REGISTER_INFO_FILE, MODE_PRIVATE)
                .getBoolean(SharedPreferencesUtil.REGISTER_SUCCESS, false);

        task = new TimerTask() {
            @Override
            public void run() {
                refreshHand.sendEmptyMessage(MessageDefine.REFRESH_SECOND_1);
            }
        };
        timer = new Timer();
        timer.schedule(task, 1000, 1000);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        version_code = DeviceParameter.getVerCode(getApplicationContext());
        version_name = DeviceParameter.getVerName(getApplicationContext());
        DeviceParameter.setSoftVersionCode(version_code);
        DeviceParameter.setSoftVersionName(version_name);
        if (version_name != null) {
            tvSoftwareVersion.setText("Ver:" + DeviceParameter.getVerName(getApplicationContext()));
        }
        tvDeviceNumber.setText("ID:" + DeviceParameter.getDeviceNumber());
//        myThread.start();

    }

    @Override
    protected void onPause() {
        timer.cancel();
        super.onPause();
        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
//        myThread.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
        TcpClient.getInstance(this).setHandlerMain(refreshHand);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //要做的事情
                if (detectSDCardAvailability()) {
                    handler.removeCallbacks(runnable);
                    isCheckingSDCard = false;
                    Log.i(TAG, "remove call back check sdcard");
                } else {
                    handler.postDelayed(this, 10000);//每隔10秒钟检测一次
                    Log.i(TAG, "Excute check sd card");
                }
            }
        };
        //handler.postDelayed(runnable, 1000);
        isCheckingSDCard = true;
        Log.i(TAG, "start check sdcard");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }


    @Override
    protected void onStop() {
        super.onStop();

        selectnum = false;
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        if (toast != null) {
            toast.cancel();
        }
        TcpClient.getInstance(this).setHandlerMain(null);
        if (DeviceParameter.getIsExaminationEn() == 1) { //只有长考才会使用SDcard
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
                isCheckingSDCard = false;
            }
        }

        client.disconnect();

    }

    public void onEventMainThread(TimerDerminalEvent event) {
        if (null == event) {
            return;
        }
        registerflag = (int) event.getObj();
        //处理注册返回
        if (registerflag == 0) {
            mPartTwoLl.setClickable(true);
            mPartThreeLl.setClickable(true);
            play("终端注册成功", 200);
            MyToast.show(this, "终端注册成功");
            //存储注册成功的标识
            SharedPreferences sp = getSharedPreferences(SharedPreferencesUtil.REGISTER_INFO_FILE, MODE_PRIVATE);
            if (null != sp) {
                SharedPreferences.Editor editor = sp.edit();
                if (null != editor) {
                    editor.putBoolean(SharedPreferencesUtil.REGISTER_SUCCESS, true);
                    editor.commit();
                }
            }
            isHadRegister = true;

        } else if (registerflag == 1) {
            mPartTwoLl.setClickable(false);
            mPartThreeLl.setClickable(false);
            play("车辆已被注册", 200);
            MyToast.show(this, "车辆已被注册");
        } else if (registerflag == 2) {
            mPartTwoLl.setClickable(false);
            mPartThreeLl.setClickable(false);
            play("数据库中无该车辆", 200);
            MyToast.show(this, "数据库中无该车辆");
        } else if (registerflag == 3) {
            mPartTwoLl.setClickable(false);
            mPartThreeLl.setClickable(false);
            play("终端已被注册", 200);
            MyToast.show(this, "终端已被注册");
        } else if (registerflag == 4) {
            mPartTwoLl.setClickable(false);
            mPartThreeLl.setClickable(false);
            play("数据库中无该终端", 200);
            MyToast.show(this, "数据库中无该终端");
        } else if (registerflag == 5) {
            mPartTwoLl.setClickable(false);
            mPartThreeLl.setClickable(false);
            play("车辆已被注册", 200);//终端证书错误
            MyToast.show(this, "车辆已被注册");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy________________");
        threadRunFlag = false;
        refreshHand.removeCallbacksAndMessages(null);
//        unregisterReceiver(broadcastRec);//取消注册
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            return true;//屏蔽返回按键
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 提示当前为最新版本
     */
    private void notNewVersionDlgShow() {
        int verCode = DeviceParameter.getVerCode(getApplicationContext());
        String verName = DeviceParameter.getVerName(getApplicationContext());
        String str = "当前版本:" + verName + " Code:" + verCode + ", 已是最新版,无需更新!";
        Dialog dialog = new AlertDialog.Builder(this).setTitle("软件更新")
                .setMessage(str)// 设置内容
                .setPositiveButton("确定",// 设置确定按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //finish();
                            }
                        }).create();// 创建
        // 显示对话框
        dialog.show();
    }


    int currentPhoneSem = -1;
    boolean isConnect = false;

    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update  从得到的信号强度,每个tiome供应商有更新*/
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            String signalinfo = signalStrength.toString();
            String[] parts = signalinfo.split(" ");
            int ltedbm = Integer.parseInt(parts[9]);
            int asu = signalStrength.getGsmSignalStrength();
            int dbm = -113 + 2 * asu;

            switch (Tel.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    //case TelephonyManager.NETWORK_TYPE_GSM:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    if (asu < 0 || asu >= 99) {
                        currentPhoneSem = 0;
                    } else if (asu >= 18) {
                        currentPhoneSem = 5;
                    } else if (asu >= 12) {
                        currentPhoneSem = 4;
                    } else if (asu >= 8) {
                        currentPhoneSem = 3;
                    } else if (asu >= 4) {
                        currentPhoneSem = 2;
                    } else {
                        currentPhoneSem = 1;
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    if (dbm > -75) {
                        currentPhoneSem = 4;
                    } else if (dbm > -85) {
                        currentPhoneSem = 3;
                    } else if (dbm > -95) {
                        currentPhoneSem = 2;
                    } else if (dbm > -100) {
                        currentPhoneSem = 1;
                    } else {
                        currentPhoneSem = 0;
                    }
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    if (ltedbm > -65) {
                        currentPhoneSem = 5;
                    }
                    if (ltedbm > -75) {
                        currentPhoneSem = 4;
                    } else if (ltedbm > -90) {
                        currentPhoneSem = 3;
                    } else if (ltedbm > -100) {
                        currentPhoneSem = 2;
                    } else if (ltedbm > -120) {
                        currentPhoneSem = 1;
                    } else {
                        currentPhoneSem = 0;
                    }
                    break;
                default:
                    tvTrainNetworkType.setText("未知?");
                    break;
            }
            refreshHand.sendEmptyMessage(MessageDefine.MSG_PHONE_SEM_CHANGED);
        }
    }

    String version_name;
    int version_code;

    protected void showUpdataDialog(final int version) {
        if (null != mUploadVersionDialog) {
            mUploadVersionDialog.show();
        } else {
            mUploadVersionDialog = new AlertDialog.Builder(AutomaticDetectionActivity.this)
                    .setTitle("版本升级")
                    .setMessage("下载需要消耗少量流量噢~")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            downLoadApk(version);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            mUploadVersionDialog.show();
        }
    }


    //time 距离上次语音播报的最小时长ms
    long flagPlayTiem;

    private void play(String txt, long time) {
        if (System.currentTimeMillis() - flagPlayTiem >= time) {
            flagPlayTiem = System.currentTimeMillis();
            SoundManage.ttsPlaySound(AutomaticDetectionActivity.this, txt);
        }
    }

    /**
     * 静默安装
     *
     * @param path apk路径
     */
    private void installApkSilence(final String path) {
        new Thread() {
            @Override
            public void run() {
                ShellUtils.CommandResult commandResult = ShellUtils.execCommand(
                        Arrays.asList("pm install -r " + path,
                                "am start -n com.navigation.timerterminal/com.serenegiant.ui.SetActivity"), true);
                Log.d(TAG, "run: " + commandResult.toString());
            }
        }.start();
    }

    /*
     * 更新页面上新加数据库条数查询的结果
     * */
    class selectNUM extends Thread {
        @Override
        public void run() {
            super.run();
            while (selectnum) {
                mUpdateHandler.sendEmptyMessage(5555);
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean selectnum = false;// 是否开启UI更新上传记录信息

    private String getImei() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null)
            return manager.getDeviceId();
        else
            return "";
    }
}
