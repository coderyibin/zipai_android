package com.xunle.zipai;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.gcloud.voice.GCloudVoiceEngine;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.egret.egretframeworknative.EgretRuntime;
import org.egret.egretframeworknative.engine.EgretGameEngine;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static com.xunle.zipai.Constant.permissions;

public class xunleGame extends Activity {
    private interface IRuntimeInterface {
        public void callback(String message) throws JSONException;
        // 因为遗留问题 callBack 也是接受的
    }
//    public String _url = "http://192.168.0.134/HotUpdate/public/runtime.json?version";
    public String _url = Constant.update_code;
    private String result;
    private String packageSize;
    private static final String EGRET_ROOT = "egret";
    //TODO: egret publish之后，修改以下常量为生成的game_code名
    private static final String EGRET_PUBLISH_ZIP = "game_code_170420111310.zip";
    protected static final String TAG = "xunleGame";
    
   	//若bUsingPlugin为true，开启插件
    private boolean bUsingPlugin = false;

    public static EgretGameEngine gameEngine;
    private String egretRoot;
    private String gameId;
    private String loaderUrl;
    private String updateUrl;
    public static xunleGame Zp;
    public View ProView;
    public IWXAPI iwxapi;
    private String APP_ID = "wxe30b001f4a755043";
    public static String WeCode = "null";
    // 支付api
    private IWXAPI api;
    private ImageView img;
    private ImageView GameLoadImg;
    private static Handler mUIHandler = new Handler();
    private GCloudVoiceEngine engine;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    public static Location locate;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println("view安卓时间-----"+df.format(new Date()));// new Date()为获取当前系统时间
        //语音========
        System.loadLibrary("GCloudVoice");
        GCloudVoiceEngine.getInstance().init(getApplicationContext(), this);
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        //======

        Zp = this;
        iwxapi = WXAPIFactory.createWXAPI(this, APP_ID, true);
        iwxapi.registerApp(APP_ID);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        egretRoot = new File(getFilesDir(), EGRET_ROOT).getAbsolutePath();
        gameId = "local";
        this.initGame();
        this.createBg();
        this.startLocation();
//        this.openGPS(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("权限log","回调");
                    this.startLocation();
                } else {
                    this.startLocation();
                    // Permission Denied
//                    Toast.makeText(this, "ACCESS_COARSE_LOCATION Denied", Toast.LENGTH_SHORT)
//                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void startLocation () {
        if (Build.VERSION.SDK_INT >= 23) {
            int check = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PermissionChecker.PERMISSION_GRANTED) {
                this.getLocation();
            }else {//手动去请求用户打开权限(可以在数组中添加多个权限) 1 为请求码 一般设置为final静态变量
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        gameEngine.setRuntimeInterface("Location", new IRuntimeInterface() {
            @Override
            public void callback(String message) {
                String locat = locate.getLatitude() + "-" + locate.getLongitude();
                gameEngine.callEgretInterface("sendLatiLongiToJs", locat);
            }
        });
//        gameEngine.setRuntimeInterface("Location", new IRuntimeInterface() {
//            @Override
//            public void callback(String message) {
//                System.out.println("开启定位！！！");
//                if (Build.VERSION.SDK_INT >= 23) {
//                    int check = ContextCompat.checkSelfPermission(Zp, permissions[0]);
////                    Zp.openGPSSettings();
//                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
//                    if (check == PermissionChecker.PERMISSION_GRANTED) {
//                        Zp.getLocation();
//                    } else {//手动去请求用户打开权限(可以在数组中添加多个权限) 1 为请求码 一般设置为final静态变量
//                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//                    }
//                } else {
//                    Zp.getLocation();
//                }
//            }
//        });
    }

    private void getLocation() {
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        // android通过criteria选择合适的地理位置服务
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
        criteria.setAltitudeRequired(false);// 设置不需要获取海拔方向数据
        criteria.setBearingRequired(false);// 设置不需要获取方位数据
        criteria.setCostAllowed(true);// 设置允许产生资费
        criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
        String provider = locationManager.getBestProvider(criteria, true);// 获取GPS信息
        Location location = locationManager.getLastKnownLocation(provider);// 通过GPS获取位置
        System.out.println("位置");

        // 定义对位置变化的监听函数
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                System.out.println("onLocationChanged");
                System.out.println("纬度：" + location.getLatitude() + "\n经度"
                        + location.getLongitude());
                Zp.updateUIToNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("onStatusChanged");
                System.out.println("privider:" + provider);
                System.out.println("status:" + status);
                System.out.println("extras:" + extras);
            }

            public void onProviderEnabled(String provider) {
                System.out.println("onProviderEnabled");
                System.out.println("privider:" + provider);
            }

            public void onProviderDisabled(String provider) {
                System.out.println("onProviderDisabled");
                System.out.println("privider:" + provider);
            }
        };
        this.updateUIToNewLocation(location);
        // 设置监听器，自动更新的最小时间为间隔N秒(这里的单位是微秒)或最小位移变化超过N米(这里的单位是米)
//        locationManager.requestLocationUpdates(provider, 100, /*0.00001F*/0,
//                locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0,
                locationListener);
    }
    private void updateUIToNewLocation(Location location) {
        if (location != null) {
            String loca = "纬度：" + location.getLatitude() + "\n经度"
                    + location.getLongitude();
            String locat = location.getLatitude() + "-" + location.getLongitude();
            locate = location;
//            Toast.makeText(this, loca, Toast.LENGTH_SHORT).show();
//            gameEngine.callEgretInterface("sendLatiLongiToJs", locat);
        } else {
        }
    }

    //动态申请权限的测试方法
    public void test() {
        // 要申请的权限 数组 可以同时申请多个权限
//        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT >= 23) {
            //如果超过6.0才需要动态权限，否则不需要动态权限
            //如果同时申请多个权限，可以for循环遍历
            int check = ContextCompat.checkSelfPermission(this,Constant.permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //写入你需要权限才能使用的方法
//                run();
            } else {
                //手动去请求用户打开权限(可以在数组中添加多个权限) 1 为请求码 一般设置为final静态变量
                this.requestPermissions(Constant.permissions, 1);
            }
        } else {
            //写入你需要权限才能使用的方法
//            run();
        }
    }
    /**
     * 强制帮用户打开GPS
     * @param context
     */
    public final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    //初始化游戏
    public void initGame () {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println("创建引擎时间-----"+df.format(new Date()));// new Date()为获取当前系统时间
        gameEngine = new EgretGameEngine();
        //TODO: DEBUG 使用 2
        setLoaderUrl(1);
        // 设置游戏的选项  (set game options)
        HashMap<String, Object> options = getGameOptions();
        gameEngine.game_engine_set_options(options);
        // 设置加载进度条  (set loading progress bar)
//        gameEngine.game_engine_set_loading_view(new GameLoadingView(this));
        //创建Egret<->Runtime的通讯 (create pipe between Egret and Runtime)
        setInterfaces();
        // 初始化并获得渲染视图 (initialize game engine and obtain rendering view)
        gameEngine.game_engine_init(this);
        View gameEngineView = gameEngine.game_engine_get_view();

        setContentView(gameEngineView);
        // 注册
        api = WXAPIFactory.createWXAPI(this, "wxe30b001f4a755043");
        api.registerApp("wxe30b001f4a755043");

        // 调用支付界面 完成支付
        this.WeChatPay();
        //微信登录
        this.WeChatLogin();
        // 微信分享
        this.WeChatShare();
        // 微信结算截屏分享
        this.WeChatJieSuanJiePing();
        // 微信分享后用URL打开APP，回传房间ID
        setShareFunc();
        // =====================语音部分的  addVoiceRoom  leaveVoiceRoom
        this.Voice();
    }

    //创建view背景图
    public void createBg () {
        img = new ImageView(this);
        img.setImageResource(R.mipmap.drbeijinglogo);
        addContentView(img, new WindowManager.LayoutParams(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT));
    }

    public void WeChatLogin () {
        // 微信登录
        gameEngine.setRuntimeInterface("egretToNative", new IRuntimeInterface() {
            @Override
            public void callback(String message) {
                if("login".equals(message)) {
                    if (iwxapi != null && iwxapi.isWXAppInstalled()) {
                        Toast tas = Toast.makeText(xunleGame.this, "获取授权中，请稍等", Toast.LENGTH_LONG);
                        tas.show();
                        final SendAuth.Req req = new SendAuth.Req();
                        req.scope = "snsapi_userinfo";
                        req.state = "wechat_test";
                        iwxapi.sendReq(req);
                        tas.cancel();
                    } else {
                        System.out.println("未安装微信");
                        Toast.makeText(xunleGame.this, "请安装微信客户端", Toast.LENGTH_LONG).show();
                    }
                }else if ("GameEnd".equals(message)){
                    Zp.GameEnd();
                } else if ("removeImg".equals(message)) {
                    Zp.removeLaunchImage();
                } else if("startVoice".equals(message)){
                    startVoice();
                } else if("endVoice".equals(message)){
                    endVoice();
                } else if("cancelVoice".equals(message)){
                    cancelVoice();
                } else if("endGame".equals(message)){
                    deinitEngine();
                }
            }
        });
    }

    public void WeChatPay () {
        gameEngine.setRuntimeInterface("sendToNative", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  message @@@@@@: " + message);

                //gameEngine.callEgretInterface("sendToJS", "2001");

                String content = new String(message) ;
                JSONObject contentJson = new JSONObject(content);

                JSONObject json = contentJson.getJSONObject("result");

                if(null != json  ){
                    Log.d(TAG, "@@@@@@@sendToNative  json.getString(\"mch_id\") @@@@@@: " + json.getString("mch_id"));

                    PayReq req = new PayReq();
                    //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
                    req.appId			= json.getString("appid");
                    req.partnerId		= json.getString("mch_id");
                    req.prepayId		= json.getString("prepay_id");
                    req.nonceStr		= json.getString("nonce_str");
                    req.timeStamp		= json.getString("time_stamp");
                    req.packageValue	= json.getString("package_value");
                    req.sign			= json.getString("sign");

                    api.sendReq(req);
                }else{
                    Log.d("PAY_GET", "返回错误"+json.getString("retmsg"));
                }


            }
        });
    }

    //微信分享
    public void WeChatShare () {
        gameEngine.setRuntimeInterface("androidShare", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  androidShare @@@@@@: " + message);
                Constant.code = 2000;
                Log.d(TAG, "@@@@@@@sendToNative  Constant.code @@@@@@: " + Constant.code);

                String content = new String(message);

                JSONObject contentJson = new JSONObject(content);

                String tilte  = contentJson.getString("title");

                String desc  = contentJson.getString("desc");

                int type  = contentJson.getInt("type");

                String roomId  = contentJson.getString("roomId");

                WXWebpageObject webpage = new WXWebpageObject();
                // 分享跳转网址
                webpage.webpageUrl = "http://zp.xunlegame.com/wxshare/zpshare.html?game=glzp";
                //webpage.webpageUrl = "http://192.168.0.250:8088/wxshare/zpshare.html?game=glzp";
                if(roomId != "0"){
                    webpage.webpageUrl =  webpage.webpageUrl + "&roomid=" + roomId;
                }

                Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + roomId);

                Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl @@@@@@: " + webpage.webpageUrl);

                WXMediaMessage msg = new WXMediaMessage(webpage);

                msg.title = tilte;   //
                msg.description = desc;
                //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.send_music_thumb);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_108);
                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                bmp.recycle();
                msg.thumbData = Util.bmpToByteArrayExtend(thumbBmp, true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;

                //public static final int WXSceneSession = 1;分享好友
                //public static final int WXSceneTimeline = 0;分享朋友圈
                //public static final int WXSceneFavorite = 2;分享到会话

                Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl type@@@@@@: " + type);

                if(type == 0){
                    Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到朋友圈");
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;

                }else if (type ==1){
                    Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到好友");
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                }
                api.sendReq(req);
            }
        });
    }

    public void WeChatJieSuanJiePing () {

        gameEngine.setRuntimeInterface("resultShare", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  resultShare @@@@@@: " + message);
                Constant.code = 3000;

                FileInputStream fis = null;

                String content = new String(message);
                Log.d(TAG, "@@@@@@@resultShare  @@@@@@: " + content);
                JSONObject contentJson = new JSONObject(content);

                int type  = contentJson.getInt("type");

                try {
                    fis = new FileInputStream( getSDPath() + "/egret_shot.png");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bmp=BitmapFactory.decodeStream(fis);
                WXImageObject imgObj = new WXImageObject(bmp);

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
                bmp.recycle();
                msg.thumbData = Util.bmpToByteArrayExtend(thumbBmp, true);  //         ͼ

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                if(type == 0){
                    Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到朋友圈");
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                }else if (type == 1){
                    Log.d(TAG, "@@@@@@@sendToNative  webpage.webpageUrl roomId@@@@@@: " + type+"分享到好友");
                    req.scene = SendMessageToWX.Req.WXSceneSession;
                }
                api.sendReq(req);
            }
        });
    }

    public void Voice () {
        gameEngine.setRuntimeInterface("addVoiceRoom", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  addVoiceRoom @@@@@@: " + message);
                addVoiceRoom(message);
            }
        });

        gameEngine.setRuntimeInterface("leaveVoiceRoom", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  leaveVoiceRoom @@@@@@: " + message);
                leaveVoiceRoom(message);
            }
        });

        gameEngine.setRuntimeInterface("initGVoice", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                if(engine == null) {
                    Log.d(TAG, "@@@@@@@sendToNative  initGVoice @@@@@@: " + message);
                    engine = GCloudVoiceEngine.getInstance();
                    engine.SetAppInfo("1389244654", "d2fd313a38bdcae913b211f6c6711288", message);
                    engine.Init();
                    engine.SetMode(0);
                    Notify notify = new Notify();
                    notify.setGameEngine(gameEngine);
                    notify.setVoiceEngine(engine);
                    engine.SetNotify(notify);
                    runGVoice();
                }
            }
        });
    }

    // 当资源加载好了之后要删除之前创建的imageView
    // 否则一直会在界面上显示的;-)
    public void removeLaunchImage() {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (img != null) {
                    //渐变展示启动屏
                    AlphaAnimation aa = new AlphaAnimation(1.0f,0f);
                    aa.setDuration(500);
                    img.startAnimation(aa);
                    aa.setAnimationListener(new Animation.AnimationListener()
                    {
                        @Override
                        public void onAnimationEnd(Animation arg0) {
                            img.setAlpha(0);
                            img.setVisibility(View.GONE);
                            //overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationStart(Animation animation) {}

                    });
                }
            }
        });
    }
    public  void  setShareFunc(){
        gameEngine.setRuntimeInterface("shareGetRoomId", new IRuntimeInterface() {
            @Override
            public void callback(String message) throws JSONException {
                Log.d(TAG, "@@@@@@@sendToNative  resultShare @@@@@@: " + message);
                String rid = String.valueOf(Constant.roomId);
                gameEngine.callEgretInterface("sendRoomIdToJs", rid);
                Constant.roomId = -1;

            }
        });
    }
    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }


    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void setInterfaces() {
        // Egret（TypeScript）－Runtime（Java）通讯
        // setRuntimeInterface(String name, IRuntimeInterface interface) 用于设置一个runtime的目标接口
        // callEgretInterface(String name, String message) 用于调用Egret的接口，并传递消息
        gameEngine.setRuntimeInterface("RuntimeInterface", new IRuntimeInterface() {
            @Override
            public void callback(String message) {
                Log.d(TAG, message);
                gameEngine.callEgretInterface("EgretInterface", "A message from runtime");
            }
        });
    }

    public Handler mHandler=new Handler() {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 1:
//                    Toast.makeText(zipai.this, "消息列表的回调" + WeCode, Toast.LENGTH_LONG).show();
                    Log.d("WXEntry Tocken","@@@@@@@@@@@@@@@@@@@@@ mHandler@@@@@@@@@@@@@@@@@@" + WeCode);

                    gameEngine.callEgretInterface("loginSucceed", WeCode);
                    break;
                case 2:gameEngine.callEgretInterface("loginCancel", "user cancel");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private HashMap<String, Object> getGameOptions() {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(EgretRuntime.OPTION_EGRET_GAME_ROOT, egretRoot);
        options.put(EgretRuntime.OPTION_GAME_ID, gameId);
        options.put(EgretRuntime.OPTION_GAME_LOADER_URL, loaderUrl);
        options.put(EgretRuntime.OPTION_GAME_UPDATE_URL, updateUrl);
        if(bUsingPlugin){
        	String pluginConf = "{'plugins':[{'name':'androidca','class':'org.egret.egretframeworknative.CameraAudio','types':'jar,so'}]}";
					options.put(EgretRuntime.OPTION_GAME_GLVIEW_TRANSPARENT, "true");
	        options.put(EgretRuntime.OPTION_EGRET_PLUGIN_CONF, pluginConf);
        }
        return options;
    }

    private void setLoaderUrl(int mode) {
        switch (mode) {
        case 2:
            // local DEBUG mode
            // 本地DEBUG模式，发布请使用0本地zip，或者1网络获取zip
            loaderUrl = "";
            updateUrl = "";
            break;
        case 1:
            Random r = new Random();
            int version = r.nextInt(100000);
            // http request zip RELEASE mode, use permission INTERNET
            // 请求网络zip包发布模式，需要权限 INTERNET
//            loaderUrl =  "http://zp.xunlegame.com/hotupdate/runtime.json?version"+ version;
//            loaderUrl =  "http://zipai.xunlegame.com:8088/hotupdate/runtime.json?version"+ version;
            //loaderUrl =  "http://192.168.0.116/runtime.json?version"+ version;
//            loaderUrl =  "http://192.168.0.117/hotupdate/runtime.json?version"+ version;
            loaderUrl = _url + version;
            updateUrl = "";
            break;
        default:
            // local zip RELEASE mode, default mode, `egret publish -compile --runtime native`
            // 私有空间zip包发布模式, 默认模式, `egret publish -compile --runtime native`
            loaderUrl = EGRET_PUBLISH_ZIP;
            updateUrl = "";
            break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        gameEngine.game_engine_onPause();
    }

    @Override
    /**
     * 判断支付成功与失败
     * */
    public void onResume() {
        super.onResume();
        gameEngine.game_engine_onResume();

        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ onResume @@@@@@@@@@@@@@@@@@");

        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ onResume122 @@@@@@@@@@@@@@@@@@" + Constant.code);
        System.out.println("onResume122---------------------"+Constant.code);


        // 支付成功，调用相应的界面。如果需要
        if ( Constant.code == 1001){
            Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ 1001 @@@@@@@@@@@@@@@@@@");
            System.out.println("支付成功1001");
            gameEngine.callEgretInterface("sendToJS", "1001");
            Toast.makeText(xunleGame.this, "支付成功", Toast.LENGTH_LONG).show();
        }
        // 支付失败
        else if (Constant.code == 1002){
            Log.d(TAG,"1002");
            gameEngine.callEgretInterface("sendToJS", "1002");
            Toast.makeText(xunleGame.this, "支付失败", Toast.LENGTH_LONG).show();
        }else if(Constant.code == 2001){
           //gameEngine.callEgretInterface("shareToJS", "2001");
            gameEngine.callEgretInterface("shareToJS", "2000");
            Toast.makeText(xunleGame.this, "分享成功", Toast.LENGTH_LONG).show();
        }else if (Constant.code == 2000){
            gameEngine.callEgretInterface("shareToJS", "2002");
            Toast.makeText(xunleGame.this, "分享失败", Toast.LENGTH_LONG).show();
        }else if (Constant.code == 3001){
            gameEngine.callEgretInterface("resultShareToJS", "3001");
            Toast.makeText(xunleGame.this, "战绩分享成功", Toast.LENGTH_LONG).show();
        }else if(Constant.code == 3000){
            gameEngine.callEgretInterface("resultShareToJS", "3000");
            Toast.makeText(xunleGame.this, "战绩分享失败", Toast.LENGTH_LONG).show();
        }
        Constant.code = 1000;
        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ onResume@@@@@@@@@@@@@@@@@@" + Constant.code);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            gameEngine.game_engine_onStop();
            finish();
            return true;
        default:
            return super.onKeyDown(keyCode, event);
        }
    }
    public void GameEnd () {
        System.exit(0);
    }

    //语音功能 ====================================
    //初始化语音
    public void runGVoice(){
        //实时帧听语音回调
        final long timeInterval = 500;
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    // ------- code for task to run
                    int pollCode = engine.Poll();
                    System.out.println("gvoice callback value"+ pollCode);
                    // ------- ends here
                    try {
                        Thread.sleep(timeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    //加入语音房间  info    roomname-uid
    public void addVoiceRoom(String info) {
        String[] arr = info.split("-");
        if(arr[0] != "null"&&arr[0] != null && arr[0] != "" && arr[0] instanceof  String) {
            int joinCode = engine.JoinTeamRoom(arr[0], 10000);
            System.out.println("addVoiceRoom joinCode" + joinCode);
        } else {
            System.out.println("addVoiceRoom joinCode" + info);
        }
    }

    //离开语音房间
    public void leaveVoiceRoom(String info){
        String[] arr = info.split("-");
        if(arr[0] != "null"&&arr[0] != null && arr[0] != "" && arr[0] instanceof  String) {
            int code = engine.QuitRoom(arr[0], 10000);
            System.out.println("leaveVoiceRoomOK" + info + "code" + code);
        } else {
            System.out.println("leaveVoiceRoom" + info);
        }
    }

    //开始录音
    public void startVoice(){
        int code = engine.OpenMic();
        System.out.println("startVoice"+code);
    }

    //结束录音
    public void endVoice(){
        int code =  engine.CloseMic();
        System.out.println("endVoice"+ code);
    }

    //取消录音
    public void cancelVoice(){
        int code = engine.Pause();
        System.out.println("cancelVoice"+code);
    }

    //反初始化engine
    public void deinitEngine(){

    }

}
