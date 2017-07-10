package com.iot.sentinel;

import java.util.Date;

public class HeartRateStatus
{
    private int UserID;
    private int HeartRate;
    private Date MeasureTime;

    public HeartRateStatus() {
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public int getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(int heartRate) {
        HeartRate = heartRate;
    }

    public Date getMeasureTime() {
        return MeasureTime;
    }

    public void setMeasureTime(Date measureTime) {
        MeasureTime = measureTime;
    }
}
