package com.iot.sentinel;

public class Clock extends Thread {

    int sec = 0;

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getSec() {
        return sec;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);

            } catch (Exception e) {
            }
            sec++;
        }
    }
}
