package kkkb1114.sampleproject.infectionapp.tools;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCalculationManager {

    public long one_MinutesMillis = 1 * 60 * 1000; // 1분에 해당하는 밀리초 계산
    public long five_MinutesMillis = 5 * 60 * 1000; // 5분에 해당하는 밀리초 계산
    public long ten_MinutesMillis = 10 * 60 * 1000; // 10분에 해당하는 밀리초 계산
    public long thirty_MinutesMillis = 30 * 60 * 1000; // 30분에 해당하는 밀리초 계산
    public long sixty_MinutesMillis = 60 * 60 * 1000; // 60분에 해당하는 밀리초 계산


    /**
     * 현재 시간에서 +10분 구하기
     **/
    public long getFormatTimeNow(long ten_MinutesMillis) {
        long currentTimeMillis = System.currentTimeMillis(); // 현재 시간을 밀리초 단위로 가져옴
        long time = currentTimeMillis + ten_MinutesMillis; // 10분 후의 시간 계산

        Date mReDate = new Date(time);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = mFormat.format(mReDate);
        return Long.parseLong(formatDate);
    }

    /**
     * 오늘 날짜 구하기
     **/
    public String getToday() {
        long currentTimeMillis = System.currentTimeMillis(); // 현재 시간을 밀리초 단위로 가져옴

        Date mReDate = new Date(currentTimeMillis);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = mFormat.format(mReDate);
        return formatDate;
    }

    /**
     * 현재 시간으로부터 30분 내인지 확인
     **/
    public boolean check_Within_30minutes_from_the_current_time(Long calculationTime) {
        long currentTime = System.currentTimeMillis();
        // 받은 시간의 30분 후보다 현재시간이 작으면 통과
        if (calculationTime > currentTime) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * (투약 알람 전용) 적은 시간에 30분 더한 시간이 현재 시간보다 크면 뺀 값을 뱉어줌
     **/
    public long pill_30minutes_from_the_current_calculation_time(Long calculationTime) {
        long currentTime = System.currentTimeMillis();
        long resultTime = 0;
        resultTime = calculationTime - currentTime;
        return currentTime + resultTime;
    }
}
