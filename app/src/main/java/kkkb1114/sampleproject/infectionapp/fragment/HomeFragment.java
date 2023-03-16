package kkkb1114.sampleproject.infectionapp.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import kkkb1114.sampleproject.infectionapp.MainActivity;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.activity.PillActivity;
import kkkb1114.sampleproject.infectionapp.thermometer.ThermometerView;

public class HomeFragment extends Fragment {

    TextView tv_temperature;
    ThermometerView thermometer;
    ImageView pill;
    ImageView significant;
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;
    SharedPreferences select_user;

    SQLiteDatabase sqlDB;

    String username;
    String significant_date_DateTime = "";

    public HomeFragment() {
//
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity();

        setUser();

        initView(view);
        this.setListner();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUser();
    }

    public void initView(View view){
        tv_temperature = view.findViewById(R.id.tv_temperature);
        thermometer = view.findViewById(R.id.thermometer);
        pill = view.findViewById(R.id.iv_pill);
        significant = view.findViewById(R.id.iv_significant);
    }

    public void setTextTvTemperature(String data){
        if (tv_temperature != null){
            tv_temperature.setText(data);
        }
    }

    public void setTextThermometerView(Float data){
        if (thermometer != null){
            thermometer.setCurValue(data);
        }
    }

    public void setListner()
    {
        pill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, PillActivity.class);
                startActivity(intent);
            }

        });

        significant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LinearLayout linear = (LinearLayout) View.inflate(getActivity(), R.layout.dialog_significant, null);
                final SeekBar seekBar = (SeekBar) linear.findViewById(R.id.seekBar_significant);
                final TextView textView = (TextView) linear.findViewById(R.id.text_seek);

                AlertDialog.Builder dialog =new AlertDialog.Builder(getActivity()).setView(linear);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(progress==0)
                            textView.setText(String.valueOf(progress)+" 아무런 통증 없음");
                        switch(progress){
                            case 0:
                                textView.setText(String.valueOf(progress)+" 매우 약한 통증");
                                break;
                            case 1:
                                textView.setText(String.valueOf(progress)+" 약한 통증");
                                break;
                            case 2:
                                textView.setText(String.valueOf(progress)+" 약간의 통증");
                                break;
                            case 3:
                                textView.setText(String.valueOf(progress)+" 중간 정도의 통증");
                                break;
                            case 4:
                                textView.setText(String.valueOf(progress)+" 상당한 통증");
                                break;
                            case 5:
                                textView.setText(String.valueOf(progress)+" 강한 통증");
                                break;
                            case 6:
                                textView.setText(String.valueOf(progress)+" 매우 강한 통증");
                                break;
                            case 7:
                                textView.setText(String.valueOf(progress)+" 아무런 통증 없음");
                                break;
                            case 8:
                                textView.setText(String.valueOf(progress)+" 극심한 통증");
                                break;
                            default:
                                textView.setText(String.valueOf(progress)+" 의식을 잃을 정도의 통증");
                                break;

                        }




                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                EditText edt_significant = (EditText) linear.findViewById(R.id.edt_significant);
                EditText edt_significantTime = (EditText) linear.findViewById(R.id.edt_significantTime);
                // TextWatcher 세팅
                setSignificant_dateTextWatcher(edt_significantTime);

                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String value = edt_significant.getText().toString() + "/" +textView.getText().toString();
                        String time = edt_significantTime.getText().toString();


                        if (time.equals(getResources().getString(R.string.et_surgeryDate))
                                || time.trim().isEmpty()
                                || time.length() < 16){

                            Toast.makeText(context, getResources().getString(R.string.et_surgeryDate), Toast.LENGTH_LONG).show();
                        }else {
                            float amount=0f;
                            String source = " ";
                            sqlDB = MainActivity.bodytemp_dbHelper.getReadableDatabase();
                            sqlDB.execSQL("INSERT INTO TIMELINEDATA VALUES ('"+username+"', '"+value+"', '"+ time +"', '"+source+"', '"+amount+"');");

                            Toast.makeText(context, "특이사항이 저장되었습니다.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }

        });
    }

    public void setUser()
    {
        select_user = context.getSharedPreferences("login_user",MODE_PRIVATE);
        username = select_user.getString("userName","선택된 사용자 없음");

        /*
        long now =System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String str = username+dateFormat.format(date)+"timelineData";


        preferences = context.getSharedPreferences(str, MODE_PRIVATE);
        editor = preferences.edit();
        */


    }


    /** 투약 창 날짜 기입 **/
    public void setSignificant_dateTextWatcher(EditText et_significant_date){
        et_significant_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String significant_date_get = et_significant_date.getText().toString();

                if (!significant_date_get.equals(significant_date_DateTime)){
                    // 문자 replace
                    significant_date_get = significant_date_get.replaceAll("[^0-9]", "");
                               /*pill_date_get = et_surgeryDate_get.replaceAll("-", "");
                                pill_date_get = et_surgeryDate_get.replaceAll(" ", "");
                                pill_date_get = et_surgeryDate_get.replaceAll(":", "");*/

                    significant_date_DateTime = dateTimeFormat(significant_date_get);
                    et_significant_date.setText(significant_date_DateTime);
                    Selection.setSelection(et_significant_date.getText(), significant_date_DateTime.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /** 날짜 문자열 변환 **/
    public String dateTimeFormat(String dateTime){
        if (dateTime.length() >= 5 && dateTime.length() <= 6){
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, dateTime.length());
            return dateTime;
        }else if (dateTime.length() >= 7 && dateTime.length() <= 8){
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, dateTime.length());
            return dateTime;
        }else if (dateTime.length() >= 9 && dateTime.length() <= 10){
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, 8) + " " + dateTime.substring(8, dateTime.length());
            return dateTime;
        }else if (dateTime.length() >= 11 && dateTime.length() <= 12){
            dateTime = dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, 8) +
                    " " + dateTime.substring(8, 10) + ":" + dateTime.substring(10, dateTime.length());
            return dateTime;
        }else {
            return dateTime;
        }
    }
}