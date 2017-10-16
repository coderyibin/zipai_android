package com.xunle.zipai;

/**
 * Created by Administrator on 2017/5/26.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import static com.xunle.zipai.xunleGame.TAG;

public class LogoActivity extends Activity{
    public LogoActivity Logo;
    private String result;
    private JSONObject Version;
    private String packageSize;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameInit();

        Logo = this;
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_hello);

        final ImageView loadingImg = new ImageView(this);
        loadingImg.setImageResource(R.mipmap.drbeijinglogo);
        addContentView(loadingImg, new WindowManager.LayoutParams(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT));

        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    //渐变展示启动屏
                    AlphaAnimation aa = new AlphaAnimation(1.0f,0f);
                    aa.setDuration(500);
                    loadingImg.startAnimation(aa);
                    aa.setAnimationListener(new Animation.AnimationListener()
                    {
                        @Override
                        public void onAnimationEnd(Animation arg0) {
                            loadingImg.setAlpha(0);
                            onSplashStop();
                            //overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationStart(Animation animation) {}

                    });
                }
            }, 500); // 启动动画持续0.5秒钟

    }

    public void JumpEngineView () {
        Intent intent = new Intent(this, xunleGame.class);
        startActivity(intent);
        this.finish();
        overridePendingTransition(0,0);
    }

    public void onSplashStop() {
        //版本检测
        this.checkUpdate();
        System.out.println("检测版本更新!!!!!!!");
    }
    //获取当前app版本号
    public int getAppVersionCode () {
        int version = 0;
        try {
            PackageManager pm = this.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version = -1;
        }
        System.out.println("当前app版本-----"+version);
        return version;
    }
    //检查更新
    public void checkUpdate (){
        new Thread(getThread).start();
    }
    private Thread getThread = new Thread(){
        public void run() {
            HttpURLConnection connection = null;
            try {
                Random r = new Random();
                int version = r.nextInt(100000);
                URL url = new URL(Constant.update_code+version);
                connection = (HttpURLConnection) url.openConnection();
                // 设置请求方法，默认是GET
                connection.setRequestMethod("GET");
                // 设置字符集
                connection.setRequestProperty("Charset", "UTF-8");
                // 设置文件类型
                connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
                // 设置请求参数，可通过Servlet的getHeader()获取
                connection.setRequestProperty("Cookie", "AppName=" + URLEncoder.encode("你好", "UTF-8"));
                // 设置自定义参数
                connection.setRequestProperty("MyProperty", "this is me!");

                if(connection.getResponseCode() == 200){
                    InputStream is = connection.getInputStream();
                    BufferedReader bf=new BufferedReader(new InputStreamReader(is,"UTF-8"));
                    //最好在将字节流转换为字符流的时候 进行转码
                    StringBuffer buffer=new StringBuffer();
                    String line="";
                    while((line=bf.readLine())!=null){
                        buffer.append(line);
                    }
                    String str = buffer.toString();
                    result = str;
                    try {
                        Version = new JSONObject(str);
                    } catch (Exception e) {
                        System.out.println("Jsons parse error !");
                        e.printStackTrace();
                    }
                    Message message=new Message();
                    message.what=5;
                    Logo.mHandler.sendMessage(message);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
        };
    };
    public Handler mHandler=new Handler() {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 5:
                    try {
                        //apk版本相同-判断zip版本
                        System.out.println("最新的app版本" + Logo.Version.getString("apk"));
                        if (Logo.getAppVersionCode() == Integer.parseInt(Logo.Version.getString("apk"))) {
                            System.out.println("本地存url--" + Logo.getLocaData("URL"));
                            if (Logo.getLocaData("URL").equals(Logo.Version.getString("code_url")) || Logo.getLocaData("URL").equals("none")) {
                                Logo.SaveLocaData("URL", Logo.Version.getString("code_url"));
                                //版本都一样--进入游戏
                                Logo.JumpEngineView();
//                                Logo.showDlg();
                            } else {
                                //代码版本不一样
                                Constant.Apk = false;
                                packageSize = Logo.Version.getString("size");
                                Logo.showDlg();
                                System.out.println("size------"+Logo.Version.getString("size"));
                            }
                        } else {
                            //apk版本不一样--前往下载
                            Constant.Apk = true;
                            Logo.showDlg();
                        }
                    } catch (Exception e) {
                        System.out.println("Jsons parse error !");
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //显示版本更新弹窗
    public void showDlg () {
        CheckUpdate.Builder builder = new CheckUpdate.Builder(this);
        if (Constant.Apk == true) {
            builder.setMessage("检测到最新版本，请前往下载");
        } else {
            builder.setMessage("检测到版本更新，本次需要更新"+packageSize+"M资源，是否立即更新");
        }
        builder.setTitle("版本更新");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //设置你的操作事项
                if (Constant.Apk == true) {//前往下载最新的apk包
                    Uri uri= Uri.parse(Constant.ApkPage+new Random().nextInt(100000));
                    Intent intent= new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    Uri content_url = uri;
                    intent.setData(content_url);
                    startActivity(intent);
//                    Logo.finish();
                    System.exit(0);
                } else {
                    dialog.dismiss();
                    try {
                        Logo.SaveLocaData("URL", Logo.Version.getString("code_url"));
                        Logo.SaveLocaData("size", Logo.Version.getString("size"));
                        Logo.SaveLocaData("apk", Logo.Version.getString("apk"));
                    } catch (Exception e) {
                        System.out.println("Jsons parse error !");
                        e.printStackTrace();
                    }
                    Logo.JumpEngineView();
                }
            }
        });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                });
        builder.create().show();
    }
    //数据保存本地
    public void SaveLocaData (String key, String value) {
        //获取SharedPreferences对象
        Context ctx = LogoActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }
    //获取本地数据
    public String getLocaData (String key) {
        //获取SharedPreferences对象
        Context ctx = LogoActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("SP", MODE_PRIVATE);
        return sp.getString(key, "none");
    }
    public  void  gameInit() {
        /**进入房间全局变量*/
        Constant.code = 1000;
        Constant.roomId = -1;

        /**进入房间*/
        Intent i_getvalue = getIntent();
        String action = i_getvalue.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = i_getvalue.getData();
            if (uri != null) {
                String roomid = uri.getQueryParameter("roomid");
                String game = uri.getQueryParameter("game");

                Log.d(TAG, "@@@@@@@ getparam  roomid @@@@@@: " + roomid);
                Log.d(TAG, "@@@@@@@ getparam  game @@@@@@: " + game);
                //gameEngine.callEgretInterface("joinRoom", "@@@@@@@message from Android2222@@@@@@@@@@@@");
                if (roomid != null) {
                    int rid = Integer.parseInt(roomid);
                    Constant.roomId = rid;
                    Log.d(TAG, "@@@@@@@ getparam  game @@@@@@: " + Constant.roomId);
                }


                //gameEngine.callEgretInterface("sendRoomIdToJs", roomid);

            }
        }
    }
}
