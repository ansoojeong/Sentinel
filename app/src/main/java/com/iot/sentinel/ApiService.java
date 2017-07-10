package com.iot.sentinel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    //주민번호로 본인 회원번호 조회
    @FormUrlEncoded
    @POST("GetUserId_Select")
    Call<ResponseBody>postResidentNum(
            @Field("ResidentNum") String residentNum);

    //맥박상태 1분간격 저장하기
    @FormUrlEncoded
    @POST("HeartRateStatus")
    Call<ResponseBody>postHeartRateStatus(
            @Field("MeasureTime") String measureTime,
            @Field("UserID") int userID,
            @Field("HeartRate") int heartRate);

    //비정상 맥박정보 등록하기
    @FormUrlEncoded
    @POST("RiskStatusRecords_Insert")
    Call<ResponseBody>postRiskStatusRecords_Insert(
            @Field("UserID") int userID,
            @Field("RiskStatusTime") String riskStatusTime,
            @Field("RiskLevel") String riskLevel,
            @Field("HeartRateStatus") int heartRateStatus);

    //비정상 맥박상태 통계 가져오기
    @FormUrlEncoded
    @POST("RiskStatusRecords_Select")
    Call<ResponseBody>postRiskStatusRecords_Select(
            @Field("UserID") int userId);

    @GET("baby")
    Call<ResponseBody>getData();

}