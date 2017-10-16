package com.xunle.zipai;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MoSplashActivity extends Activity {

	private Boolean hasSplash;
	private xunleGame ziPai;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	   try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	   
		hasSplash = false;
		
		try {
			ApplicationInfo appInfo;
			
			appInfo = this.getPackageManager()
			        .getApplicationInfo(this.getPackageName(), 
			PackageManager.GET_META_DATA);
			
			String msg=String.valueOf(appInfo.metaData.get("SHOW_SPLASH"));
			if(msg != null && !"null".equals(msg) && "1".equals(msg)) {
				hasSplash = true;
			} else {
				hasSplash = false;
			}
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
   
		if(hasSplash){
			String path = "app/bg_logo.jpg";
	 	 	WindowManager wm = this.getWindowManager();    	 
			int width = wm.getDefaultDisplay().getWidth();
			int height = wm.getDefaultDisplay().getHeight();
//	        Bitmap loadingBitmap = getImageFromAssetsFile(R.mipmap.drlogozt);
//	        final ImageView loadingImg = new ImageView(this);
//	        if(loadingBitmap != null){
////	        	loadingBitmap = Utils.zoomImage(loadingBitmap,width,height);
//	            loadingImg.setImageBitmap(loadingBitmap);
//	            //loadingImg.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//	            loadingImg.setScaleType(ScaleType.CENTER_CROP);
//	            this.setContentView(loadingImg);
//
//	        }
			final ImageView loadingImg = new ImageView(this);
			loadingImg.setImageResource(R.mipmap.drbeijinglogo);
			addContentView(loadingImg, new WindowManager.LayoutParams(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT));


			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					
					//渐变展示启动屏
					AlphaAnimation aa = new AlphaAnimation(1.0f,0f);
					aa.setDuration(700);
					loadingImg.startAnimation(aa);
					aa.setAnimationListener(new AnimationListener()
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
					
					
					//overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);  
					
				}
			}, 3000); // 启动动画持续3秒钟	
		}else{
			onSplashStop();
		}
		
	}
  
  
	public void onSplashStop() {
		Intent intent = new Intent(this, this.ziPai.Zp.getClass());
		startActivity(intent);
		this.finish();
	}

	  /*  
     * 从Assets中读取图片  
     */  
    private Bitmap getImageFromAssetsFile(String fileName)  
    {  
        Bitmap image = null;  
        AssetManager am = getResources().getAssets();  
        try  
        {  
            InputStream is = am.open(fileName);  
            image = BitmapFactory.decodeStream(is);  
            is.close();  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    
        return image;  
    
    } 
    
    
}