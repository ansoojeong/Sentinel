package com.iot.sentinel;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class AppService extends Service {
    public static final String TAG = "AppService";

    /**
     * 블루투스 사용 변수
     */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final boolean D = true;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MODE_REQUEST = 1;

    public static final int STATE_SENDING = 1;
    public static final int STATE_NO_SENDING = 2;
    private int mSendingState;

    //BluetoothService클래스에 접근하기 위한 객체이다.
    private BluetoothService bluetoothService_obj = null;
    private StringBuffer mOutStringBuffer;

    // End 블루투스

    Activity _activity = null;
    BluetoothService _bluetoothService = null;

    public AppService(Activity activity , BluetoothService bluetoothService) {
        _activity = activity;
        _bluetoothService = bluetoothService;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){

            return Service.START_STICKY;
        }else{
            processCommand(intent);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void processCommand(Intent intent) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }





}

