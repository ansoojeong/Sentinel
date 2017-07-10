package com.iot.sentinel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NowTimeInformation {

    public NowTimeInformation() {
    }

    public String get_the_current_time(){

        Calendar today = Calendar.getInstance();
        today.add(today.HOUR ,+9);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = simpleDateFormat.format(today.getTime());

        return nowTime;
    }
}
