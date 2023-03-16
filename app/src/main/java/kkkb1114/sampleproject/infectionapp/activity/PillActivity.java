package kkkb1114.sampleproject.infectionapp.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kkkb1114.sampleproject.infectionapp.API.OpenApi;
import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.Notification.AlarmReceiver;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.pill.PillAdapter;
import kkkb1114.sampleproject.infectionapp.tools.TimeCalculationManager;

public class PillActivity extends AppCompatActivity {

    TimeCalculationManager timeCalculationManager;
    Context context;
    Button bt_pill_cancel, bt_pill_confirm, bt_pillSearch;
    EditText edt_pillSearch, pill_date;
    SQLiteDatabase sqlDB;
    View view;
    RecyclerView rv_pill;

    PillAdapter pillAdapter;
    String username;

    String searchText;

    SharedPreferences daymax;
    float imax;
    float amax;

    Cursor cursor;
    String Pdate;
    String pill_date_DateTime = "";

    ArrayList<String> ad = new ArrayList<>();
    ArrayList<String> af = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        context = this;
        timeCalculationManager = new TimeCalculationManager();

        SharedPreferences select_user;
        select_user = context.getSharedPreferences("login_user", MODE_PRIVATE);
        username = select_user.getString("userName", "선택된 사용자 없음");

        SharedPreferences daymax;
        daymax = context.getSharedPreferences("DayMax", MODE_PRIVATE);
        imax = daymax.getFloat("ibuprofen", 1);
        amax = daymax.getFloat("acetaminophen", 1);


        initView();
        setRecyclerView(view);
        setListner();
    }

    private void initView() {
        bt_pill_cancel = (Button) findViewById(R.id.bt_pill_cancle);
        bt_pill_confirm = (Button) findViewById(R.id.bt_pill_confirm);
        bt_pillSearch = (Button) findViewById(R.id.bt_pillSearch);
        edt_pillSearch = (EditText) findViewById(R.id.edt_pillSearch);
        pill_date = (EditText) findViewById(R.id.pill_date);

    }

    /**
     * 투약 창 날짜 기입
     **/
    public void setPill_dateTextWatcher(EditText et_pill_date) {
        et_pill_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String pill_date_get = et_pill_date.getText().toString();

                if (!pill_date_get.equals(pill_date_DateTime)) {
                    // 문자 replace
                    pill_date_get = pill_date_get.replaceAll("[^0-9]", "");

                    pill_date_DateTime = dateTimeFormat(pill_date_get);
                    et_pill_date.setText(pill_date_DateTime);
                    Selection.setSelection(et_pill_date.getText(), pill_date_DateTime.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * 날짜 문자열 변환
     **/
    public String dateTimeFormat(String dateTime) {
        if (dateTime.length() >= 5 && dateTime.length() <= 6) {
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, dateTime.length());
            return dateTime;
        } else if (dateTime.length() >= 7 && dateTime.length() <= 8) {
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, dateTime.length());
            return dateTime;
        } else if (dateTime.length() >= 9 && dateTime.length() <= 10) {
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, 8) + " " + dateTime.substring(8, dateTime.length());
            return dateTime;
        } else if (dateTime.length() >= 11 && dateTime.length() <= 12) {
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, 8) +
                    " " + dateTime.substring(8, 10) + ":" + dateTime.substring(10, dateTime.length());
            return dateTime;
        } else {
            return dateTime;
        }
    }

    public void setListner() {

        bt_pillSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_pillSearch.getText().toString().length() != 0) {
                    searchText = edt_pillSearch.getText().toString();
                    String targetName = edt_pillSearch.getText().toString();
                    ad.clear();
                    af.clear();
                    OpenApi pill = new OpenApi(targetName, ad, af, pillAdapter, context, rv_pill);
                    pill.execute();


                } else {

                }

            }

        });


        bt_pill_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LinearLayout linear = (LinearLayout) View.inflate(context, R.layout.dialog_pill, null);
                EditText edt_pill = (EditText) linear.findViewById(R.id.edt_pill);
                EditText pill_date = (EditText) linear.findViewById(R.id.pill_date);
                // TextWatcher 세팅
                setPill_dateTextWatcher(pill_date);

                AlertDialog dialog = new AlertDialog.Builder(context).setView(linear)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                sqlDB = MainActivity.bodytemp_dbHelper.getReadableDatabase();

                                Pdate = pill_date.getText().toString();

                                if (Pdate.equals(getResources().getString(R.string.et_surgeryDate))
                                        || Pdate.trim().isEmpty()
                                        || Pdate.length() < 16){

                                    Toast.makeText(context, getResources().getString(R.string.et_surgeryDate), Toast.LENGTH_LONG).show();
                                }else {
                                    Float amount = 0f;

                                    try {
                                        amount = Float.valueOf(edt_pill.getText().toString());
                                    } catch (NumberFormatException e) {
                                        Toast toast = Toast.makeText(context, "숫자로만 표기하여 주십시오.", Toast.LENGTH_LONG);
                                        toast.show();
                                    }


                                    if (amount != 0) {
                                        String value = ad.get(PillAdapter.getSelected());
                                        String source = af.get(PillAdapter.getSelected());

                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                                        Date date = null;
                                        try {
                                            date = dateFormat.parse(Pdate);
                                        } catch (ParseException e) {
                                            throw new RuntimeException(e);
                                        }
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);

                                        cal.add(Calendar.DATE, -1);

                                        String start = dateFormat.format(cal.getTime());

                                        Float total = 0f;
                                        Float total2 = 0f;

                                        if (source.contains("buprofen")) {
                                            String ibu = "ibuprofen";
                                            cursor = sqlDB.rawQuery("SELECT * FROM TIMELINEDATA WHERE name = '" + username + "' AND TimelineDateTime BETWEEN '" + start + "' AND '" + Pdate + "' AND Source LIKE '%" + ibu + "%'; ", null);
                                            while (cursor.moveToNext()) {
                                                Log.d("name ", cursor.getString(1));
                                                total += cursor.getFloat(4);
                                            }


                                            if (total + amount > imax) {
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(edt_pill.getWindowToken(), 0);
                                                Toast toast = Toast.makeText(context, "이부프로펜 1일 최대량을 넘었습니다. 현재(" + total + ")", Toast.LENGTH_LONG);
                                                toast.show();

                                            } else {

                                                sqlDB.execSQL("INSERT INTO TIMELINEDATA VALUES ('" + username + "', '" + value + "', '" + Pdate + "', '" + source + "', '" + amount + "');");
                                                Log.d("pass ", "pass");
                                                setAlarm_30minutes_after_administration(Pdate);

                                                Toast toast = Toast.makeText(context, "투약기록이 저장되었습니다.", Toast.LENGTH_LONG);
                                                toast.show();
                                                dialog.dismiss();
                                                finish();
                                            }

                                        } else if (source.contains("Acetaminophen")) {
                                            String ibu = "acetaminophen";
                                            cursor = sqlDB.rawQuery("SELECT * FROM TIMELINEDATA WHERE name = '" + username + "' AND TimelineDateTime BETWEEN '" + start + "' AND '" + Pdate + "' AND Source LIKE '%" + ibu + "%'; ", null);

                                            while (cursor.moveToNext()) {
                                                Log.d("name ", cursor.getString(1));
                                                total2 += cursor.getFloat(4);
                                            }

                                            if (total2 + amount > amax) {
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(edt_pill.getWindowToken(), 0);
                                                Toast toast = Toast.makeText(context, "아세트아미노펜 1일 최대량을 넘었습니다. 현재(" + total2 + ")", Toast.LENGTH_LONG);
                                                toast.show();

                                            } else {

                                                sqlDB.execSQL("INSERT INTO TIMELINEDATA VALUES ('" + username + "', '" + value + "', '" + Pdate + "', '" + source + "', '" + amount + "');");
                                                Log.d("pass ", "pass");
                                                setAlarm_30minutes_after_administration(Pdate);
                                                Toast toast = Toast.makeText(context, "투약기록이 저장되었습니다.", Toast.LENGTH_LONG);
                                                toast.show();
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }

                                    }
                                }
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();

                dialog.show();

            }
        });

        bt_pill_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * dialog 스피너 시간 세팅
     **/


    public void setRecyclerView(View view) {

        rv_pill = (RecyclerView) findViewById(R.id.rv_pill_list);
        rv_pill.setVisibility(View.VISIBLE);
        pillAdapter = new PillAdapter(ad, af, context);
        rv_pill.setLayoutManager(new LinearLayoutManager(context));
        rv_pill.setAdapter(pillAdapter);

    }

    /**
     * 투약 30분후 알람 추가
     **/
    public void setAlarm_30minutes_after_administration(String getDateTime) {
        try {
            /*
             * 투약은 30분 후 알람이기에 1800000 더함.
             *  - 잠시 테스트를 위해 1분으로 함
             */
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date getDate = dateFormat.parse(getDateTime);

            long getDateTime_toLong = getDate.getTime();
            long calculationTime = getDateTime_toLong + timeCalculationManager.thirty_MinutesMillis;

            if (timeCalculationManager.check_Within_30minutes_from_the_current_time(calculationTime)) {
                long requestID = timeCalculationManager.pill_30minutes_from_the_current_calculation_time(getDateTime_toLong + timeCalculationManager.thirty_MinutesMillis);

                Date mReDate = new Date(timeCalculationManager.pill_30minutes_from_the_current_calculation_time(getDateTime_toLong + timeCalculationManager.thirty_MinutesMillis));
                SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String formatDate = mFormat.format(mReDate);

                if (requestID > 0) {
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("alarm_mode", 2); // 0: 고온, 1: 저온

                    PendingIntent pendingIntent;
                    // SDK 31 이상이면 PendingIntent.FLAG_IMMUTABLE (바뀐 정책상 무조건 FLAG_IMMUTABLE 사용해야함.)
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
                        pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                                PendingIntent.FLAG_IMMUTABLE);
                    }else {
                        pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    AlarmManager alarmManager_administratione = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    alarmManager_administratione.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, requestID, pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
