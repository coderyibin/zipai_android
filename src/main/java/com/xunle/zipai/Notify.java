package com.xunle.zipai;

import com.tencent.gcloud.voice.GCloudVoiceEngine;
import com.tencent.gcloud.voice.IGCloudVoiceNotify;
import android.widget.Toast;

import org.egret.egretframeworknative.engine.EgretGameEngine;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by Administrator on 2017/6/7.
 */

public class Notify implements IGCloudVoiceNotify{

    private EgretGameEngine gameEngine;
    private GCloudVoiceEngine voiceEngine;
    // Notify/OnJoinRoom spakerCode0i1s0320i12
    @Override
    public void OnJoinRoom(int i, String s, int i1) {
        int code = voiceEngine.OpenSpeaker();
        System.out.println("Notify/OnJoinRoom spakerCode"+code+"i"+i+"s"+s+"i1"+i1);
        gameEngine.callEgretInterface("joinVoiceRoom",String.valueOf(i1));
    }

    @Override
    public void OnStatusUpdate(int i, String s, int i1) {
        System.out.println("Notify/OnStatusUpdate"+"i"+i+"s"+s+"i1"+i1);
    }

    @Override
    public void OnQuitRoom(int i, String s) {
        int code = voiceEngine.CloseSpeaker();
        System.out.println("Notify/OnQuitRoom CloseSpeaker"+code+"i"+i+"s"+s);
    }
    //Notify/OnMemberVoice[I@53c122f8logStrmemberid:1  state:1 count1
    @Override
    public void OnMemberVoice(int[] members, int count) {
       String logStr = "";
        for(int i=0; i < count && (i+1) < members.length; ++i)
        {
            logStr += members[i] +"-" + members[i+1] +",";
            ++i;
        }
        logStr = logStr.substring(0,logStr.length()-1);
        System.out.println("Notify/OnMemberVoice" + members + "logStr" + logStr +"count" + count);
       gameEngine.callEgretInterface("voiceState", logStr);
    }

    @Override
    public void OnUploadFile(int i, String s, String s1) {
        System.out.println("Notify/OnUploadFile");
    }

    @Override
    public void OnDownloadFile(int i, String s, String s1) {
        System.out.println("Notify/OnDownloadFile");
    }

    @Override
    public void OnPlayRecordedFile(int i, String s) {
        System.out.println("Notify/OnPlayRecordedFile");
    }

    @Override
    public void OnApplyMessageKey(int i) {
        System.out.println("Notify/OnApplyMessageKey");
    }

    @Override
    public void OnSpeechToText(int i, String s, String s1) {
        System.out.println("Notify/OnSpeechToText");
    }

    @Override
    public void OnRecording(char[] chars, int i) {
        System.out.println("Notify/OnRecording");
    }

    public void setGameEngine(EgretGameEngine  engine){
        gameEngine = engine;
    }

    public void setVoiceEngine(GCloudVoiceEngine engine){
        voiceEngine = engine;
    }
}
