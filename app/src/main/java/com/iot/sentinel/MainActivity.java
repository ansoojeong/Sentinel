package com.iot.sentinel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {
    private static final String TO_SERVER_REQUEST_MY_USERID = "GetUserId_Select";  //주민등록번호로 UserId 가져오기
    private static final String TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT = "HeartRateStatus"; //맥박 1분씩저장
    private static final String TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_INSERT = "RiskStatusRecords_Insert"; //비정상 맥박저장
    private static final String TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_SELECT = "RiskStatusRecords_Select"; // 비정상맥박 통계 조회

    TextView showUserID;
    Button connect;
    Button statistics;
    Button riskStatus_insert;
    Button riskStatus_select;
    TextView status;
    DialogActivity dialogActivity;

    String residentNum = "901205";
    int userID = 1;
    // DB에 저장할 시간타입과 자바의 타입이 달라서 String 으로 보내고 DB에서 DATE 타입으로 변환
    String measureTime = "";
    int heartRate = 30;

    String riskStatusTime = "";
    String riskLevel = "경고수준";
    int heartRateStatus = 50;

    //nodeJS 와 REST API 사용하는 모듈
    ApiService apiService = null;

    //현재시간 구하는 클래스
    NowTimeInformation nowTime = new NowTimeInformation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showUserID = (TextView) findViewById(R.id.showUserID);
        connect = (Button) findViewById(R.id.connect);
        statistics = (Button) findViewById(R.id.statistics);
        riskStatus_insert = (Button) findViewById(R.id.riskStatus_insert);
        riskStatus_select = (Button) findViewById(R.id.riskStatus_select);
        status = (TextView) findViewById(R.id.status);

        dialogActivity = new DialogActivity(MainActivity.this);
        dialogActivity.setTitle("주민번호 입력");

        dialogActivity.show();

        /*dialogActivity.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                residentNum = dialogActivity.getNumber();

                showUserID.setText(residentNum);
                Toast.makeText(
                        getApplicationContext(),
                        "주민번호를 입력하셨습니다.",
                        Toast.LENGTH_SHORT).show();
                status.append("입력\n");
            }
        });*/

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

        statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_HEARTRATE_STATUS_INSERT);
            }
        });

        riskStatus_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_INSERT);
            }
        });
        riskStatus_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSendMessage(TO_SERVER_REQUEST_RISKSTATUS_RECODEDS_SELECT);
            }
        });
    }

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
                Call<ResponseBody> call1 = apiService.postResidentNum(residentNum);
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
                            JSONArray jArray = new JSONArray("[" + jsonData + "]");
                            JSONObject jObject = jArray.getJSONObject(0);
                            userID = jObject.getInt("userID");
                            System.out.println(userID);
                            showUserID.append(userID + "\n");
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
            case "HeartRateStatus":
                System.out.println(measureTime);
                measureTime = nowTime.get_the_current_time();
                Call<ResponseBody> call2 = apiService.postHeartRateStatus(measureTime, userID, heartRate);
                call2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            //비정상 맥박정보 등록하기
            case "RiskStatusRecords_Insert":
                riskStatusTime = nowTime.get_the_current_time();
                Call<ResponseBody> call3 = apiService.postRiskStatusRecords_Insert(userID, riskStatusTime, riskLevel, heartRateStatus);
                call3.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;

            /*비정상 맥박상태 통계 가져오기*/
            case "RiskStatusRecords_Select":
                Call<ResponseBody> call4 = apiService.postRiskStatusRecords_Select(userID);
                call4.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
                break;
        }

    }

    private void executeGetMessage() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://10.10.14.77:3005/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> call = apiService.getData();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.i("Test", response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
