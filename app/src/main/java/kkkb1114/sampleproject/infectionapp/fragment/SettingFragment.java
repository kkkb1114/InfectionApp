package kkkb1114.sampleproject.infectionapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import kkkb1114.sampleproject.infectionapp.BleConnect.ConnectActivity;
import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.activity.AlarmActivity;
import kkkb1114.sampleproject.infectionapp.activity.MyProfileActivity;
import kkkb1114.sampleproject.infectionapp.activity.UserSettingActivity;
import kkkb1114.sampleproject.infectionapp.tools.PreferenceManager;

public class SettingFragment extends Fragment implements View.OnClickListener {

    TextView tv_setting_connect;
    TextView tv_setting_profile;
    TextView tv_setting_alarm;
    TextView tv_setting_select_user;

    String userPurpose = "";
    boolean isSelectUser = false;

    public SettingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initView(view);
        isSelectUserCheck();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 설정화면에서 사용자 변경후 다시 바뀌어야하기에 onResume()에 추가
        isSelectUserCheck();
    }

    public void initView(View view){
        tv_setting_connect = view.findViewById(R.id.tv_setting_connect);
        tv_setting_connect.setOnClickListener(this);
        tv_setting_profile = view.findViewById(R.id.tv_setting_profile);
        tv_setting_profile.setOnClickListener(this);
        tv_setting_alarm = view.findViewById(R.id.tv_setting_alarm);
        tv_setting_alarm.setOnClickListener(this);
        tv_setting_select_user = view.findViewById(R.id.tv_setting_select_user);
        tv_setting_select_user.setOnClickListener(this);
    }

    /** 선택된 사용자 있는지 확인 **/
    public void isSelectUserCheck(){
        PreferenceManager.PREFERENCES_NAME = "login_user";
        String selectUser = PreferenceManager.getString(getContext(), "userName");
        if (selectUser.trim().length() <= 0){
            isSelectUser = false;
        }else {
            isSelectUser = true;
            userPurpose = PreferenceManager.getString(getContext(), "userPurpose");
            // 사용 목적이 '배란'이면 알람 클릭 막는다.
            if (userPurpose.equals("배란")){
                tv_setting_alarm.setEnabled(false);
            }else {
                tv_setting_alarm.setEnabled(true);
            }
            tv_setting_select_user.setText(selectUser);
        }
    }

    /** 클릭 이벤트 모음 **/
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_setting_connect:
                // 블루투스 연결 화면 이동
                Intent connectIntent = new Intent(view.getContext(), ConnectActivity.class);
                startActivity(connectIntent);
                break;
            case R.id.tv_setting_profile:
                // 내 유저 리스트로 이동
                Intent userListIntent = new Intent(view.getContext(), UserSettingActivity.class);
                startActivity(userListIntent);
                break;
            case R.id.tv_setting_alarm:
                PreferenceManager.PREFERENCES_NAME = "login_user";
                String selectUser = PreferenceManager.getString(getContext(), "userName");

                if (selectUser == null || selectUser.trim().isEmpty()){
                    Toast.makeText(getContext(), "사용자 등록을 먼저 진행해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent alarmIntent = new Intent(view.getContext(), AlarmActivity.class);
                    startActivity(alarmIntent);
                    break;
                }
            case R.id.tv_setting_select_user:
                if (isSelectUser){
                    // 내 프로필로 이동
                    Intent myProfileIntent = new Intent(view.getContext(), MyProfileActivity.class);
                    myProfileIntent.putExtra("userName", tv_setting_select_user.getText().toString());
                    startActivity(myProfileIntent);
                }else {
                    Toast.makeText(getContext(), "현재 선택하신 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}