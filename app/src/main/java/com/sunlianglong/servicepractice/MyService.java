package com.sunlianglong.servicepractice;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

public class MyService extends Service {
    private DownloadBinder mBinder = new DownloadBinder();

    class DownloadBinder extends Binder {
        //思路：创建一个专门的Binder对象来对下载功能进行管理。
        public void StartDownload() {
            System.out.print("startDownload executed");
        }

        public int grtProgress() {
            System.out.print("getProgress executed");
            return 0;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        System.out.println("onCreate executed");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // 必需的通知内容
        builder.setContentTitle("content title")
                .setContentText("content describe")
                .setSmallIcon(R.mipmap.ic_launcher);
        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.notify(1, notification);
        startForeground(1, notification);
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        System.out.println("onStartCommand executed");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        System.out.println("onDestroy executed");
    }
}
