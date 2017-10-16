package com.xunle.zipai;

import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameLoadingView extends FrameLayout {

    private ProgressBar bar;
    private TextView tv;

    /**
     * 游戏下载进度条 上线前请替换渠道自定制进度条
     * 
     * @param context
     */
    public GameLoadingView(Context context) {
        super(context);

//        LayoutInflater inflater = LayoutInflater.from(context);
//        View v = inflater.inflate(R.layout.activity_pro, null);

        ImageView img = new ImageView(context);
        img.setImageResource(R.mipmap.drbeijinglogo);
        this.addView(img, new WindowManager.LayoutParams(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT));

        bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        LayoutParams params = new LayoutParams(500, 20);
        params.gravity = Gravity.CENTER|Gravity.TOP;
        params.topMargin = 700 * 4/5;
        bar.setLayoutParams(params);
        this.addView(bar);

        tv = new TextView(context);
        tv.setText("游戏加载中，请耐心等待...");
//        tv.setTextColor(0x82EEFA);
        LayoutParams params2 = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.CENTER|Gravity.TOP;
        params2.topMargin = params.topMargin + 15;
        tv.setLayoutParams(params2);
        this.addView(tv);

    }

    public void onProgress(float progress) {
        bar.setProgress((int) progress);
    }

    public void onGameZipUpdateProgress(float percent) {
        bar.setProgress((int) percent);
    }

    public void onGameZipUpdateError() {

    }

    public void onGameZipUpdateSuccess() {

    }

}
