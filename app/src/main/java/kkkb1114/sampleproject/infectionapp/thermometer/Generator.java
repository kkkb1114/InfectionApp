package kkkb1114.sampleproject.infectionapp.thermometer;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Generator extends AppCompatActivity {

    public static String infection()
    {
        double max=40.0;
        double min=37.3;


        double temp =  Math.random() * (max-min+1)+min-1;
        Log.d("temp",String.valueOf(temp));
        String s = String.format("%.2f",temp);
        return s;
    }

    public static String inflammation()
    {
        double max=40.0;
        double min=37.3;


        double temp =  Math.random() * (max-min+1)+min-1;
        String s = String.format("%.2f",temp);
        return s;
    }

    public static String ovulation()
    {
        double max=36.9;
        double min=36.5;


        double temp =  Math.random() * (max-min+1)+min-1;
        Log.d("temp",String.valueOf(temp));
        String s = String.format("%.2f",temp);
        return s;
    }


}
