package com.sunlianglong.servicepractice;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.security.PublicKey;

/**
 * Created by sun liang long on 2016/8/24.
 */
public class MyIntentService extends IntentService {
    public MyIntentService() {
        super("MyIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        //打印当前线程的id
       Log.d("MyIntentService", "Thread id is" + Thread.currentThread().getId());
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("MyIntentService","onDestroy executed");
    }
}
