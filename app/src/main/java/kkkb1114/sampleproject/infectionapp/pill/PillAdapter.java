package kkkb1114.sampleproject.infectionapp.pill;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kkkb1114.sampleproject.infectionapp.R;

public class PillAdapter extends RecyclerView.Adapter<PillAdapter.ViewHolder> {
    ArrayList<String> ad;
    ArrayList<String> af;
    Context context;
    HashMap<Integer,String> map = new HashMap<>();

    static int selectedPosition = 0;

    public static int getSelected()
    {
        return selectedPosition;
    }
    private List<ViewHolder> mViewHolderList = new ArrayList<>();


    public PillAdapter(ArrayList<String> asd,ArrayList<String> af,Context context){
        this.ad = asd;
        this.af=af;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_pill_search, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(String.valueOf(holder.getAbsoluteAdapterPosition()).equals(String.valueOf(selectedPosition))){
            holder.pill_layout.setBackgroundColor(context.getResources().getColor(R.color.user_list_select_user,null));
        }
        else{
            holder.pill_layout.setBackgroundColor(Color.WHITE);
        }

        mViewHolderList.add(holder);


        holder.pill_name.setText(ad.get(position));
        holder.pill_source.setText(af.get(position));
        map.put(holder.getAbsoluteAdapterPosition(),ad.get(position));
        holder.setListner(holder,position,map);

    }

    @Override
    public int getItemCount() {

        return ad.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout pill_layout;
        TextView pill_name;
        TextView pill_source;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initView(itemView);

        }

        public void initView(View itemView){
            pill_layout = itemView.findViewById(R.id.pill_layout);
            pill_name = itemView.findViewById(R.id.pill_name);
            pill_source = itemView.findViewById(R.id.pill_source);
        }

        public void setListner(@NonNull ViewHolder holder,int position,HashMap map) {

           pill_layout.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View view) {
                    selectedPosition=getAbsoluteAdapterPosition();

                    changeAllBackgroundColors(Color.WHITE);
                    if(holder.pill_name.getText().toString().equals(map.get(holder.getAbsoluteAdapterPosition())))
                        holder.pill_layout.setBackgroundColor(context.getResources().getColor(R.color.user_list_select_user,null));
                }
            });
        }

        public void changeAllBackgroundColors(int color) {
            for (ViewHolder holder : mViewHolderList) {
                holder.pill_layout.setBackgroundColor(color);
            }
        }




    }


}
