package com.xunle.zipai.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xunle.zipai.Constant;

import org.egret.egretframeworknative.engine.EgretGameEngine;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	
	private static final String TAG = "hello_app_as_test";
	private EgretGameEngine gameEngine;

	private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.pay_result);
/*		View gameEngineView = gameEngine.game_engine_get_view();
		setContentView(gameEngineView);*/

		Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ pay onCreate@@@@@@@@@@@@@@@@@@");

		api = WXAPIFactory.createWXAPI(this,"wxe30b001f4a755043");
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ pay onNewIntent@@@@@@@@@@@@@@@@@@");

		super.onNewIntent(intent);
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ pay onReq@@@@@@@@@@@@@@@@@@");

	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ pay onResp@@@@@@@@@@@@@@@@@@" + resp.getType());

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0){
				Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ 支付成功 @@@@@@@@@@@@@@@@@@" + String.valueOf(resp.errCode));
				System.out.println("支付成功");
				Constant.code = 1001;
			}else {
				Log.d(TAG,"@@@@@@@@@@@@@@@@@@@@@ 支付失败 @@@@@@@@@@@@@@@@@@" + String.valueOf(resp.errCode));
				Constant.code = 1002;
			}
			finish();
		}
	}
}