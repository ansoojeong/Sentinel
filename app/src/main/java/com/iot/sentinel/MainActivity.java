package com.iot.sentinel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.StringTokenizer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    private static final String TAG = "MainActiviy";
    private static final String GPS = "GPS";

    /*경고 단계 기준*/
    private static final int Red_Level_Low = 40;
    private static final int Yellow_Level = 100;
    private static final int Red_Level_High = 120;

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

    /**
     * Web Server 와 통신하는 변수
     */
    //주민번호로 본인 회원번호 조회하기
    private static final String TO_SERVER_REQUEST_MY_USERID = "GetUserId_Select";
    //맥박상태 1분간격으로 저장하기
    private static final String TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT = "HeartRateStatus_Insert";
    //맥박 시간별 통계 가져오기
    private static final String TO_SERVER_REQUEST_HEARTRATE_STATISTICS_SELECT_BY_HOURS = "HeartRateStatistics_Select_By_Hours";
    //맥박 날짜별 통계 가져오기
    private static final String TO_SERVER_REQUEST_HEARTRATE_STATISTICS_SELECT_BY_DAILY = "HeartRateStatistics_Select_By_Daily";
    //비정상 맥박정보 등록하기
    private static final String TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_INSERT = "RiskStatusRecords_Insert";
    //비정상 맥박정보 가져오기
    private static final String TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_SELECT = "RiskStatusRecords_Select";
    //Web Server 통신하는 변수 끝

    /**
     * Service 프로그램 동작
     */
    AppService _appService = null;


    TextView showUserID;
    TextView showNowHeartRate;
    Button connect;
    Button statistics_By_Hours;
    Button statistics_By_Daily;
    Button riskStatus_Select;

    Button button_bluetooth_connect;
    TextView showData;
    TextView showData2;
    TextView textview_filed1;
    TextView textview_filed2;

    DialogActivity dialogActivity;

    String residentNum = "901205";
    String name;
    int userID;
    // DB에 저장할 시간타입과 자바의 타입이 달라서 String 으로 보내고 DB에서 DATE 타입으로 변환
    String measureTime = "";
    int heartRate = 30;

    String term;
    int heartRateAvg;

    String riskStatusTime = "";
    String riskLevel = "경고수준";

    String riskStatusTime_Select;
    String riskLevel_Select;
    int heartRateStatus_Select;

    String nowLocal;

    //nodeJS 와 REST API 사용하는 모듈
    ApiService apiService = null;
    private GPSService _gpsService;

    //현재시간 구하는 클래스
    NowTimeInformation nowTime = new NowTimeInformation();
    //시간 1분씩 재는 타이머
    Clock _clock = new Clock();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showUserID = (TextView) findViewById(R.id.showUserID);
        connect = (Button) findViewById(R.id.connect);

        statistics_By_Hours = (Button) findViewById(R.id.statistics_By_Hours);
        statistics_By_Daily = (Button) findViewById(R.id.statistics_By_Daily);

        riskStatus_Select = (Button) findViewById(R.id.riskStatus_Select);
        showData = (TextView) findViewById(R.id.showData);
        showData2 = (TextView) findViewById(R.id.showdata2);
        textview_filed1 = (TextView) findViewById(R.id.textView_filed1);
        textview_filed2 = (TextView) findViewById(R.id.textView_filed2);
        showNowHeartRate = (TextView) findViewById(R.id.textView_nowHeartRate);


        /**
         *  GPS
         */
        _gpsService = new GPSService(MainActivity.this);
        if (_gpsService.isGpsCheck() == false)
            createAlertDialog("GPS");

        /**
         * bluetooth
         */
        if (bluetoothService_obj == null) {
            bluetoothService_obj = new BluetoothService(this, mHandler);
            mOutStringBuffer = new StringBuffer("");
        }

        button_bluetooth_connect = (Button) findViewById(R.id.btn_bluetooth);
        button_bluetooth_connect.setOnClickListener(mClickListener);

        dialogActivity = new DialogActivity(MainActivity.this);
        dialogActivity.setTitle("주민번호 입력");

        dialogActivity.show();

        dialogActivity.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                residentNum = dialogActivity.getNumber();
                showUserID.setText(residentNum);
            }
        });

        dialogActivity.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                Toast.makeText(
                        getApplicationContext(),
                        "주민번호를 입력하지 않았습니다.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(residentNum);
                executeSendMessage(TO_SERVER_REQUEST_MY_USERID);

            }
        });


        statistics_By_Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATISTICS_SELECT_BY_HOURS);
            }
        });

        statistics_By_Daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATISTICS_SELECT_BY_DAILY);
            }
        });

        riskStatus_Select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_SELECT);
            }
        });

        _clock.start();
    }

    //onCreate End

    private void executeSendMessage(String postName) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://10.10.14.56:1337/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        apiService = retrofit.create(ApiService.class);
        onEvent_Type(postName);
    }

    public void onEvent_Type(String msg) {
        switch (msg) {
            //주민번호로 본인 회원번호 조회하기
            case "GetUserId_Select":
                Call<ResponseBody> call1 = apiService.post_ResidentNum(residentNum);
                call1.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String jsonData = null;
                        try {
                            jsonData = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(jsonData);

                        try {
                            JSONArray jArray = new JSONArray(jsonData);
                            JSONObject jObject = jArray.getJSONObject(0);
                            userID = jObject.getInt("UserID");
                            name = jObject.getString("Name");
                            System.out.println(userID + " " + name);
                            showUserID.setText(userID + "");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //맥박상태 1분간격으로 저장하기
            case "HeartRateStatus_Insert":
                measureTime = nowTime.get_the_current_time();
                System.out.println(measureTime + " 1분간격저장");
                Call<ResponseBody> call2 = apiService.post_HeartRateStatus_Insert(measureTime, userID, heartRate);
                call2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //맥박 시간별 통계 가져오기
            case "HeartRateStatistics_Select_By_Hours":
                Call<ResponseBody> call3 = apiService.post_HeartRateStatistics_Select_By_Hours(userID);
                call3.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String jsonData = null;
                        try {
                            jsonData = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(jsonData);

                        try {
                            JSONArray jArray = new JSONArray(jsonData);
                            textview_filed1.setText("날 짜");
                            textview_filed2.setText("맥 박 정 보");
                            showData.setText("");
                            showData2.setText("");
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject jObject = jArray.getJSONObject(i);
                                term = jObject.getString("Term");
                                heartRateAvg = jObject.getInt("HeartRateAvg");

                                StringTokenizer term_Date = new StringTokenizer(term, "T");
                                String[] term_Date_String = new String[term_Date.countTokens()];
                                int j = 0;
                                while (term_Date.hasMoreTokens()) {
                                    term_Date_String[j] = term_Date.nextToken();
                                    j++;
                                }

                                StringTokenizer term_Time = new StringTokenizer(term_Date_String[1], ".");
                                String[] term_Time_String = new String[term_Time.countTokens()];
                                int k = 0;
                                while (term_Time.hasMoreTokens()) {
                                    term_Time_String[k] = term_Time.nextToken();
                                    k++;
                                }

                                System.out.println("Date : " + term_Date_String[0] + " , " + term_Time_String[0] + " ,  Avg : " + heartRateAvg);

                                showData.append(term_Date_String[0] + " / " + term_Time_String[0] + "\n");
                                showData2.append("      " + heartRateAvg + "\n");
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //맥박 날짜별 통계 가져오기
            case "HeartRateStatistics_Select_By_Daily":
                Call<ResponseBody> call4 = apiService.post_HeartRateStatistics_Select_By_Daily(userID);
                call4.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String jsonData = null;
                        try {
                            jsonData = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(jsonData);

                        try {
                            JSONArray jArray = new JSONArray(jsonData);
                            textview_filed1.setText("날 짜");
                            textview_filed2.setText("맥 박 정 보");
                            showData.setText("");
                            showData2.setText("");
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject jObject = jArray.getJSONObject(i);
                                term = jObject.getString("Term");
                                heartRateAvg = jObject.getInt("HeartRateAvg");

                                StringTokenizer term_Date = new StringTokenizer(term, "T");
                                String[] term_Date_String = new String[term_Date.countTokens()];
                                int j = 0;
                                while (term_Date.hasMoreTokens()) {
                                    term_Date_String[j] = term_Date.nextToken();
                                    j++;
                                }

                                System.out.println("Date : " + term_Date_String[0] + "  | " + heartRateAvg);

                                showData.append(term_Date_String[0] + "\n");
                                showData2.append("             " + heartRateAvg + "\n");
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //비정상 맥박정보 등록하기
            case "RiskStatusRecords_Insert":
                riskStatusTime = nowTime.get_the_current_time();
                System.out.println("비정상상태 저장" + "data : " + heartRate);
                Call<ResponseBody> call5 = apiService.post_RiskStatusRecords_Insert(userID, riskStatusTime, riskLevel, heartRate);
                call5.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //비정상 맥박정보 가져오기
            case "RiskStatusRecords_Select":
                Call<ResponseBody> call6 = apiService.post_RiskStatusRecords_Select(userID);
                call6.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String jsonData = null;
                        try {
                            jsonData = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(jsonData);

                        try {
                            JSONArray jArray = new JSONArray(jsonData);
                            textview_filed1.setText("날 짜");
                            textview_filed2.setText("경고수준 / 맥박 정보 ");
                            showData.setText("");
                            showData2.setText("");
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject jObject = jArray.getJSONObject(i);
                                riskStatusTime_Select = jObject.getString("RiskStatusTime");
                                riskLevel_Select = jObject.getString("RiskLevel");
                                heartRateStatus_Select = jObject.getInt("HeartRateStatus");

                                StringTokenizer term_Date = new StringTokenizer(riskStatusTime_Select, "T");
                                String[] term_Date_String = new String[term_Date.countTokens()];
                                int j = 0;
                                while (term_Date.hasMoreTokens()) {
                                    term_Date_String[j] = term_Date.nextToken();
                                    j++;
                                }

                                StringTokenizer term_Time = new StringTokenizer(term_Date_String[1], ".");
                                String[] term_Time_String = new String[term_Time.countTokens()];
                                int k = 0;
                                while (term_Time.hasMoreTokens()) {
                                    term_Time_String[k] = term_Time.nextToken();
                                    k++;
                                }

                                System.out.println("Date : " + term_Date_String[0] + " , " + term_Time_String[0] + " ,  RiskLevel : " + riskLevel_Select + " ,  HeartRate : " + heartRateStatus_Select);
                                showData.append(term_Date_String[0] + " / " + term_Time_String[0] + "\n");
                                showData2.append("     " + riskLevel_Select + " / " + heartRateStatus_Select + "\n");

                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;
        }

    }

    /**
     * 맥박상태 수준 확인
     */
    public String heartRateStatus_check(int heartRate_data) {
        if (heartRate_data <= Red_Level_Low) {
            showNowHeartRate.setTextColor(Color.RED);
            return "Red";
        } else if (heartRate_data > Red_Level_Low && heartRate_data < Yellow_Level) {
            showNowHeartRate.setTextColor(Color.GREEN);
            return "Green";
        } else if (heartRate_data >= Yellow_Level && heartRate_data < Red_Level_High) {
            showNowHeartRate.setTextColor(Color.YELLOW);
            return "Yellow";
        } else if (heartRate_data >= Red_Level_High) {
            showNowHeartRate.setTextColor(Color.RED);
            return "Red";
        } else
            return "No";
    }

    //상태별 행동
    public void action_By_Status(String status) {

        switch (status) {
            case "Red":
                riskLevel = "위험";
                _gpsService.startLocationService();
                nowLocal = String.valueOf(_gpsService.getlatLng());
                if (_clock.getSec() > 20) {
                    executeSendMessage(TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_INSERT);
                    executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT);
                    _clock.setSec(0);
                }
                //신고
                break;
            case "Yellow":
                riskLevel = "경고";
                if (_clock.getSec() > 30) {
                    executeSendMessage(TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_INSERT);
                    executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT);
                    _clock.setSec(0);
                }
                break;
            case "Green":
                if (_clock.getSec() > 60) {
                    executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT);
                    _clock.setSec(0);
                }
                break;
        }
    }

    /**
     * 블루투스 시리얼 통신 주고받는 핸들러 및 버튼 이벤트 처리
     */
    private int mSelectedBtn;
    private final Handler mHandler = new Handler() {
        //핸들러의 기능을 수행할 클래스(handleMessage)
        public void handleMessage(Message msg) {
            //BluetoothService로부터 메시지(msg)를 받는다.
            super.handleMessage(msg);

            switch (msg.what) {

                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE : " + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 성공하였습니다!", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
                    if (readMessage != null) {
                        String data = readMessage.replace("#", "");
                        if (!data.equals("")) {
                            heartRate = Integer.valueOf(data);
                            System.out.println("출력될 값 : " + heartRate + " // 진행시간 :" + _clock.getSec());
                            action_By_Status(heartRateStatus_check(heartRate));
                            showNowHeartRate.setText(heartRate + " bpm");
                        }
                    }
                    break;
                case MESSAGE_WRITE:
                    String writeMessage = null;
                    if (mSelectedBtn == 1) {
                        mSelectedBtn = -1;
                    } /*else if ( mSelectedBtn == 2 ) {
                        writeMessage = mbtn2.getText().toString() ;
                        mSelectedBtn = -1 ;
                    } */ else { // mSelectedBtn = -1 : not selected
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        writeMessage = new String(writeBuf);
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*블루투스 접속에 따른 결과를 처리하는 메소드 이다.*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + resultCode);
        // TODO Auto-generated method stub

        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                //When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)  //불루투스가 off일떄 연결을 눌러 활성화 시켰을 경우
                {
                    bluetoothService_obj.scanDevice();//기기검색을 요청하는 메소드 추가

                } else {//취소를 눌렀을때
                    Log.d(TAG, "Bluetooth is not enable");
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothService_obj.getDeviceInfo(data);
                }
                break;
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //분기.
            switch (v.getId()) {

                case R.id.btn_bluetooth:  //모든 블루투스의 활성화는 블루투스 서비스 객체를 통해 접근한다.

                    if (bluetoothService_obj.getDeviceState()) // 블루투스 기기의 지원여부가 true 일때
                    {

                        bluetoothService_obj.enableBluetooth();  //블루투스 활성화 시작.
                    } else {
                        finish();
                    }
                    break;

                // 블루투스 모듈에 데이터 전송   /*아두이노로 데이터 쓰기*/
//                case R.id.btn_response :
//                    //연결된 상태에서만 값을 보낸다.
//                    if( bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){
//                        sendMessage("0", MODE_REQUEST);
//                        mSelectedBtn = 1;
//                    }else {
//                        Toast.makeText(getApplicationContext(), "블루투스 연결을 먼저 해 주세요!! ", Toast.LENGTH_SHORT).show();
//                    }
//                    break ;

                default:
                    break;

            }//switch
        }
    };

    /*메시지를 보낼 메소드 정의*/
    private synchronized void sendMessage(String message, int mode) {


        if (mSendingState == STATE_SENDING) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mSendingState = STATE_SENDING;

        // Check that we're actually connected before trying anything
        if (bluetoothService_obj.getState() != BluetoothService.STATE_CONNECTED) {
            mSendingState = STATE_NO_SENDING;
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService_obj.write(send, mode);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

        }

        mSendingState = STATE_NO_SENDING;
        notify();
    }


    /**
     * GPS 다이얼로그 실행
     */

    public void createAlertDialog(String requester) {
        AlertDialog.Builder dig = new AlertDialog.Builder(MainActivity.this);
        dig.setTitle(requester);
        dig.setMessage(requester + "를 실행 하시겠습니까?");
        dig.setPositiveButton("예", (dialog, which) -> {
            if (requester.equals(GPS)) {
                _gpsService.turnOnGps();
            }
        });
        dig.setNegativeButton("아니요", (dialog, which) -> {
            if (requester.equals(GPS)) {
                finish();
            }
        });

        dig.show();
    }

}
