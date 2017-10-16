package com.xunle.zipai.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xunle.zipai.Constant;
import com.xunle.zipai.xunleGame;

/**
 * Created by Administrator on 2017/5/8.
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI iwxapi;
    private String APP_ID = "wxe30b001f4a755043";
    private xunleGame zp;
    //    private String APP_SECRET = "aee5e6c6d1e079fbf9591a9b81b3b06c";
    public String Code = "";
    private static final String TAG = "WXEntry";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(WXEntryActivity.this, "初始化微信登录", Toast.LENGTH_LONG).show();
        iwxapi = WXAPIFactory.createWXAPI(this, APP_ID, true);
        iwxapi.handleIntent(getIntent(), this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iwxapi.handleIntent(intent, this);//必须调用此句话
    }
    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }
    @Override
    public void onResp(BaseResp resp) {
        //登录回调
        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx login 登录回调@@@@@@@@@@@@@@@@@@");

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp instanceof SendAuth.Resp) {
                    // 微信登录
                    try {
                        SendAuth.Resp send = (SendAuth.Resp) resp;
                        Code = send.token;
//                            this.getAccessToken(code);
//                            Toast.makeText(WXEntryActivity.this, "code是***" + Code, Toast.LENGTH_LONG).show();
                        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx login entry@@@@@@@@@@@@@@@@@@");

                        zp.WeCode = Code;
                        //登录成功，调用egret
//                            zp.gameEngine.callEgretInterface("loginSucceed", zp.WeCode);
                        zp.Zp.runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                new Handler().postDelayed(new Runnable(){
                                    public void run() {
                                        //execute the task
//                                            Toast.makeText(WXEntryActivity.this, "线程回调" + zp.WeCode, Toast.LENGTH_LONG).show();
                                        Message message=new Message();
                                        message.what=1;
                                        zp.Zp.mHandler.sendMessage(message);
                                    }
                                }, 2);

                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    //分享成功的回调
                    Toast.makeText(WXEntryActivity.this, "分享成功", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx share entry@@@@@@@@@@@@@@@@@@");
                    if(Constant.code == 2000){
                        Constant.code = 2001;
                    }else if (Constant.code == 3000){
                        Constant.code = 3001;
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx share entry ERR_USER_CANCEL@@@@@@@@@@@@@@@@@@" + Constant.code);
                if (resp instanceof SendAuth.Resp) {
                    zp.WeCode = "cancel";
                    zp.Zp.runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            new Handler().postDelayed(new Runnable(){
                                public void run() {
                                    Message message=new Message();
                                    message.what=2;
                                    zp.Zp.mHandler.sendMessage(message);
                                }
                            }, 2);
                        }
                    });
                    this.finish();
                }


                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED://登录失败
                Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx share entry ERR_SENT_FAILED@@@@@@@@@@@@@@@@@@" + Constant.code);

                zp.WeCode = "cancel";
                zp.Zp.runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                Message message=new Message();
                                message.what=2;
                                zp.Zp.mHandler.sendMessage(message);
                            }
                        }, 2);
                    }
                });
                this.finish();
                break;
            default:
                break;
        }
        finish();
        Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ wx share finish@@@@@@@@@@@@@@@@@@" + Constant.code);

    }
}
