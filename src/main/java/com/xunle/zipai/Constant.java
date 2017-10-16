package com.xunle.zipai;

import android.Manifest;

/**
 * Created by Administrator on 2017/4/28.
 */
public  class Constant {
    public static String IP = "http://zp.xunlegame.com/";
//    public static String IP = "http://192.168.0.250/";
//    public static String IP = "http://192.168.0.134/";
    public static int code;
    public static  int roomId;
    public static boolean Apk;
    public static String ApkPage = IP+"wxshare/download.html?apk='1'&version=";
//    public static String update_code = IP+"HotUpdate/public/runtime.json?version=";//本地
    public static String update_code = IP+"hotupdate/runtime.json?version=";
    public static String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION//定位权限
    };
}
