package kkkb1114.sampleproject.infectionapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.database.Bodytemp_DBHelper;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile_DBHelper;
import kkkb1114.sampleproject.infectionapp.userList.UserListAdapter;

public class UserSettingActivity extends AppCompatActivity implements View.OnClickListener {

    MyProfile_DBHelper myProfile_dbHelper;

    Toolbar toolbar_user_setting;
    Context context;
    ArrayList<MyProfile> userList;
    TextView tv_close;

    RecyclerView rv_user_list;
    UserListAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        context = this;
        myProfile_dbHelper = new MyProfile_DBHelper();
        initView();
        setToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 설정화면에서 사용자 변경후 다시 바뀌어야하기에 onResume()에 추가
        getUserData();
        setRecyclerView();
    }

    public void initView(){
        toolbar_user_setting = findViewById(R.id.toolbar_user_setting);
        rv_user_list = findViewById(R.id.rv_user_list);
        tv_close = findViewById(R.id.tv_close);
        tv_close.setOnClickListener(this);
    }

    /** DB에서 유저 데이터 ArrayList 가져온다. **/
    public void getUserData(){
        userList = myProfile_dbHelper.DBselectAll();
    }

    /** 리사이클러뷰 세팅 **/
    public void setRecyclerView(){
        userListAdapter = new UserListAdapter(context, userList);
        rv_user_list.setLayoutManager(new LinearLayoutManager(context));
        rv_user_list.setAdapter(userListAdapter);
        userListAdapter.notifyDataSetChanged();
    }


    /** 툴바 세팅 **/
    public void setToolbar(){
        setSupportActionBar(toolbar_user_setting);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_user_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.user_add:
                // 유저 추가 화면 이동
                Intent addUserIntent = new Intent(context, MyProfileActivity.class);
                startActivity(addUserIntent);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_close:
                finish();
                break;
        }
    }
}