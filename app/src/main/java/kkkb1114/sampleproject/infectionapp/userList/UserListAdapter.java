package kkkb1114.sampleproject.infectionapp.userList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kkkb1114.sampleproject.infectionapp.R;
import kkkb1114.sampleproject.infectionapp.activity.MyProfileActivity;
import kkkb1114.sampleproject.infectionapp.database.Bodytemp_DBHelper;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile;
import kkkb1114.sampleproject.infectionapp.database.MyProfile.MyProfile_DBHelper;
import kkkb1114.sampleproject.infectionapp.tools.PreferenceManager;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    Context context;
    ArrayList<MyProfile> userList;

    MyProfile_DBHelper myProfile_dbHelper;
    Bodytemp_DBHelper bodytemp_dbHelper;

    public UserListAdapter(Context context, ArrayList<MyProfile> userList){
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String name = userList.get(position).name;
        holder.tv_user_name.setText(name);

        setUserNameClick(holder, name);
        setModifyClick(holder);
        setParentViewLongClick(holder, name);
        setSelectUser(holder, name);
    }

    /** 현재 사용자 구분 **/
    public void setSelectUser(ViewHolder holder, String name){
        PreferenceManager.PREFERENCES_NAME = "login_user";
        String select_user_name = PreferenceManager.getString(context, "userName");
        if (select_user_name != null && select_user_name.equals(name)){
            holder.ln_user.setBackgroundColor(context.getResources().getColor(R.color.user_list_select_user,null));
        }
    }

    /** 사용자 선택 **/
    public void setUserNameClick(ViewHolder holder, String name){
        holder.ln_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.PREFERENCES_NAME = "login_user";
                // 다른 화면에서 현재 선택된 사용자 구분이 되어야 하기에 현재 사용자 구분 쉐어드 파일 생성
                PreferenceManager.setString(holder.itemView.getContext(), "userName", name);
                ((Activity)context).finish();
            }
        });
    }

    /** 사용자 수정 **/
    public void setModifyClick(ViewHolder holder){
        holder.tv_user_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myProfileIntent = new Intent(context, MyProfileActivity.class);
                myProfileIntent.putExtra("userName", holder.tv_user_name.getText().toString());
                context.startActivity(myProfileIntent);
            }
        });
    }

    /** 아이템 부모뷰 롱클릭 이벤트 **/
    public void setParentViewLongClick(ViewHolder holder, String name){
        holder.ln_user.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PreferenceManager.PREFERENCES_NAME = "login_user";
                String select_user_name = PreferenceManager.getString(context, "userName");
                if (name.equals(select_user_name)){
                    cannotUserDeletedDialog();
                }else {
                    userDeleteDialog(name);
                }
                return false;
            }
        });
    }

    /** 현재 선택된 사용자는 삭제 할수 없다는 창 **/
    public void cannotUserDeletedDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("사용자 삭제")
                .setMessage("현재 선택된 사용자는 삭제할 수 없습니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** 사용자 삭제 창 **/
    public void userDeleteDialog(String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("사용자 삭제")
                .setMessage("사용자를 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PreferenceManager.PREFERENCES_NAME = "login_user";
                        if(PreferenceManager.getString(context,"userName")==name)
                            Toast.makeText(context, "로그인 중인 사용자는 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        else{
                            bodytemp_dbHelper = Bodytemp_DBHelper.getInstance(context, "Bodytemperature.db", null, 1);
                            bodytemp_dbHelper.ProfileDelete(name);
                            bodytemp_dbHelper.TempDelete(name);
                            bodytemp_dbHelper.TimeLineDelete(name);

                            for(int j=0; j<userList.size(); j++)
                            {
                                if(userList.get(j).name.equals(name))
                                {
                                    userList.remove(j);
                                }
                            }
                        }

                        dialogInterface.dismiss();
                        // 유저 삭제했으니 새로고침
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout ln_user;
        TextView tv_user_name;
        TextView tv_user_modify;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView();
        }

        public void initView(){
            ln_user = itemView.findViewById(R.id.ln_user);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_user_modify = itemView.findViewById(R.id.tv_user_modify);
        }
    }
}
