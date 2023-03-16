package kkkb1114.sampleproject.infectionapp.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

import kkkb1114.sampleproject.infectionapp.R;

public class WeightPickerDialog extends DialogFragment implements View.OnClickListener {

    TextView bt_confirm;
    TextView bt_cancle;
    NumberPicker np_weight_integer;
    NumberPicker np_weight_float;
    TextView tv_myProfile_weight;

    private DatePickerDialog.OnDateSetListener listener;
    private Calendar calendar = Calendar.getInstance();

    public WeightPickerDialog(TextView tv_myProfile_weight) {
        this.tv_myProfile_weight = tv_myProfile_weight;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.dialog_weight_picker, null);
        initView(view);
        setPicker();
        builder.setView(view);

        return builder.create();
    }

    public void initView(View view) {
        np_weight_integer = view.findViewById(R.id.np_weight_integer);
        np_weight_float = view.findViewById(R.id.np_weight_float);
        bt_confirm = view.findViewById(R.id.bt_confirm);
        bt_confirm.setOnClickListener(this);
        bt_cancle = view.findViewById(R.id.bt_cancle);
        bt_cancle.setOnClickListener(this);
    }

    public void setPicker() {
        np_weight_integer.setMinValue(0);
        np_weight_integer.setMaxValue(150);
        np_weight_float.setMinValue(0);
        np_weight_float.setMaxValue(9);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_confirm:
                String weight = String.valueOf(np_weight_integer.getValue()) + " . " + String.valueOf(np_weight_float.getValue()) + " Kg";
                tv_myProfile_weight.setText(weight);
                this.getDialog().cancel();
                break;
            case R.id.bt_cancle:
                this.getDialog().cancel();
                break;
        }
    }
}
