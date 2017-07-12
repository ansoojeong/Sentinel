package com.iot.sentinel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    //주민번호로 본인 회원번호 조회하기
    @FormUrlEncoded
    @POST("GetUserId_Select")
    Call<ResponseBody>post_ResidentNum(
            @Field("ResidentNum") String residentNum);

    //맥박상태 1분간격으로 저장하기
    @FormUrlEncoded
    @POST("HeartRateStatus_Insert")
    Call<ResponseBody>post_HeartRateStatus_Insert(
            @Field("MeasureTime") String measureTime,
            @Field("UserID") int userID,
            @Field("HeartRate") int heartRate);

    //맥박 시간별 통계 가져오기
    @FormUrlEncoded
    @POST("Statistics_Select_By_Hours")
    Call<ResponseBody>post_HeartRateStatistics_Select_By_Hours(
            @Field("UserID") int userID);

    //맥박 날짜별 통계 가져오기
    @FormUrlEncoded
    @POST("Statistics_Select_By_Dates")
    Call<ResponseBody>post_HeartRateStatistics_Select_By_Dates(
            @Field("UserID") int userID);

    //비정상 맥박정보 등록하기
    @FormUrlEncoded
    @POST("RiskStatusRecords_Insert")
    Call<ResponseBody>post_RiskStatusRecords_Insert(
            @Field("UserID") int userID,
            @Field("RiskStatusTime") String riskStatusTime,
            @Field("RiskLevel") String riskLevel,
            @Field("HeartRateStatus") int heartRateStatus);

    //비정상 맥박정보 가져오기
    @FormUrlEncoded
    @POST("RiskStatusRecords_Select")
    Call<ResponseBody>post_RiskStatusRecords_Select(
            @Field("UserID") int userId);

    @GET("baby")
    Call<ResponseBody>getData();

}