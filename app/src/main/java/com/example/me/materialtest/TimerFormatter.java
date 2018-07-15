package com.example.me.materialtest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by me on 2018/3/24.
 */

public class TimerFormatter {
    public static String formatterTime(int currentPosition) {
        SimpleDateFormat sdateformat=new SimpleDateFormat("mm:ss");
        String format = sdateformat.format(new Date(currentPosition + 0));

        return format;
    }
}
