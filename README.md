> ####  **定义一个服务**

```java
public class MyService extends Service {
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
```
···重写服务中最常用的三个方法：`onCreate()`服务创建的时候调用  `onStartCommand()` 每次服务启动的时候调用 `onDestroy()`服务销毁的时候调用

···在AndroidMainifest.xml文件中注册服务：
```java
<service android:name=".MyService"> </service>
```
> #### 启动和停止服务
> 
> #### 服务和活动通信

> 在xml中添加四个按钮 

> 新建MyService服务

```java
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

```
> MainActivity中

```java
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button startService;
    private Button stopService;
    private Button bindService;
    private Button unbindService;
    private MyService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
    //创建一个ServiceConnection的匿名类，重写以下两种方法：在活动与服务绑定和解除时使用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        //向下转型得到DownloadBinder实例，进行简单的下载 显示进度 测试
        downloadBinder = (MyService.DownloadBinder)service;
            downloadBinder.StartDownload();
            downloadBinder.grtProgress();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService = (Button)findViewById(R.id.bind_service);
        unbindService = (Button)findViewById(R.id.unbind_service);
        startService = (Button)findViewById(R.id.start_service);
        stopService = (Button)findViewById(R.id.stop_service);
        startService.setOnClickListener(this);
        stopService.setOnClickListener(this);
        bindService.setOnClickListener(this);
        unbindService.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_service:
                Intent startIntent = new Intent(this,MyService.class);
                startService(startIntent);//启动活动
                break;
            case R.id.stop_service:
                Intent stopIntent = new Intent(this,MyService.class);
                stopService(stopIntent);//停止活动
                break;
            case R.id.bind_service:
                Intent bindIntent = new Intent(this,MyService.class);
                bindService(bindIntent,connection,BIND_AUTO_CREATE);
                break;
                //将MainActivity和MyService进行绑定。三个参数：Intent对象；ServiceConnection实例；标志位 BIND_AUTO_CREATE表示在活动和服务绑定后自动创建服务——使得onCreate()方法执行。
            case R.id.unbind_service:
                unbindService(connection);//解除服务。
                break;
            default:
                break;
        }
    }
}

```
挨个点击效果图：

![](https://github.com/sunlianglong/Img/raw/master/Photos/service.png) 

> #### 注意

···每调用一次`stopService()`方法，`onStartCommand()`方法就会执行一次，但实际上每个服务都只存在一个实例，所以销毁时只需调用一次`stopService()`方法或者
`stopSelf()`方法。

···一个既调用了startService()方法和bindServicae()方法的服务，要想销毁掉，必须同时调用stopService()方法和unbindService()方法，onDestroy()方法才会执行。

> ### IntentService的使用

服务默认运行在主线程中，若在服务里添加一些耗时的逻辑，可能会出现ANR（application not responding）的情况，此时，一个比较标注的服务：在MyService中的`onStartCommand()`方法中添加以下代码  缺点：一旦启动便会一直运行 需要`stopService()`方法或者`stopSelf()`方法

```java
@Override
    public int onStartCommand(Intent intent,int flags,int startId){
        new Thread(new Runnable){
        @Override
        public void run(){
        //处理耗时逻辑
        }
    }).start();
    return super.onStartCommand(intent,flags,startId);
 }
```



新建一个MyIntentService类继承自IntentService（不要忘记注册）：

```java
public class MyIntentService extends IntentService {
    public MyIntentService() {
        super("MyIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        //打印当前线程的id
       Log.d("MyIntentService", "Thread id is" + Thread.currentThread().getId());
       
      //在这个方法中去实现一些具体的逻辑，因为这个方法已经在子线程中运行了。
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("MyIntentService","onDestroy executed");
    }
}
```
添加一个StartIntentService按钮：
```java
case R.id.start_intent_service:
                //打印主线程的id
                Log.d("MainActivity","Thread id is"+Thread.currentThread().getId());
                Intent intentService = new Intent(this,MyIntentService.class);
                startService(intentService);
                break;
                //在点击事件中启动MyIntentService服务。
```
点击按钮后：

![](https://github.com/sunlianglong/Img/raw/master/Photos/log.png) 




****
> ### Service实现 后台执行定时服务



---
- 安卓定时任务有两种：java API中提供的Timer类：android的Alarm机制。
- Alarm：具有唤醒CPU的功能（唤醒CPU和唤醒屏幕不是一个概念）
- Timer：不适用长期在后台执行的定时任务。


> ##### Alarm机制的用法

```java
//获取一个AlarmManager实例
AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//调用其set()方法就可以设定一个定时任务了，比如设定一个任务在10秒之后执行：
long triggerAtTime = SystemClock.elapsedRealtime()+10*1000;
manager.set(AlarmManager.ELAPSED_REALTIME_WAKUP,triggerTime,pendingIntent);
//SystemClock.elapsedRealtime()可以获取系统开机至今所经历的毫秒数
//System。currentTimeMillis()可以获取1970年1月1日0时至今所经历的毫秒数
//System。currentTimeMillis()  +   RTC_WAKEUP 也可以实现
```

set()方法的三个参数：

1···整型参数(用于指定Alarm的工作类型)：
`AlarmManager.ELAPSED` 让定时任务的触发时间从系统时间开始算起，但不会唤醒CPU

`AlarmManager.ELAPSED_REALTIME_WAKUP` 让定时任务的触发时间从1970年1月1日0时开始算起，会唤醒CPU

`RTC` 让定时任务的触发时间从系统时间开始算起，但不会唤醒CPU

`RTC_WAKEUP  让定时任务的触发时间从1970年1月1日0时开始算起，但不会唤醒CPU

2···定时任务触发的时间，以毫秒为单位。

3···这里我们一般调用`getBroadcast()`方法来获取一个能够执行广播的PendingIntent，这样当定时任务被触发时，广播接收器的`onReceive()`方法可以得到执行。

> ##### 创建实例演示

- 创建新项目 新建LongRunningService类并注册

```java
public class LongRunningService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
    //在onStartCommand()方法里开一个子线程，进行逻辑操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LongRunningService","executed at"+new Date().toString());
            }
        }).start();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anhour = 10*1000; //十秒的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime()+anhour;
        Intent i = new Intent(this,AlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        //用PendingIntent指定处理定时任务时的广播接收器为AlarmManager 当然要建一个AlarmReceiver类
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }
}
```
- 新建AlarmReceiver类并注册

```java
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context,LongRunningService.class);
        context.startService(i);
    }
}
```
一旦启动LongRunningService，就会在`onStartCommand()`方法中设定一个定时任务，这样10s后AlarmReceiver的`onReceive()`方法会得到执行，我们在这里再次启动LongRunningService，实现无限循环。接下来的思路：打开程序的时候启动一次LongRunningService。
```java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,LongRunningService.class);
        startService(intent);
    }
}

```
