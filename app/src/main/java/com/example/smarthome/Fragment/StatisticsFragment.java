package com.example.smarthome.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Activity.MainActivity;
import com.example.smarthome.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class StatisticsFragment extends Fragment {
    TextView tvFromDate;
    TextView tvToDate;
    TextView tvDateError;
    TextView btnGo;
    DatePickerDialog.OnDateSetListener FromDateListener;
    DatePickerDialog.OnDateSetListener ToDateListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        Context context = view.getContext();
        tvFromDate = view.findViewById(R.id.tv_fromDate);
        tvToDate = view.findViewById(R.id.tv_toDate);
        tvDateError = view.findViewById(R.id.tvDateError);
        btnGo = view.findViewById(R.id.btnGo);
        // today
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        // default value
        tvFromDate.setText(String.format("%s/%s/%s", day, month+1, year));
        tvToDate.setText(String.format("%s/%s/%s", day, month+1, year));
        tvDateError.setText("");
        // date listener
        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        android.R.style.Theme_DeviceDefault,
                        FromDateListener,
                        year,
                        month,
                        day);
//                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));
                datePickerDialog.show();
            }
        });

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        android.R.style.Theme_DeviceDefault,
                        ToDateListener,
                        year,
                        month,
                        day);
//                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.));
                datePickerDialog.show();
            }
        });

        FromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth+"/"+month+"/"+year;
                tvFromDate.setText(date);
            }
        };
        ToDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth+"/"+month+"/"+year;
                tvToDate.setText(date);
            }
        };
        // button click
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDate()) {
                    loadReport();
                    tvDateError.setText("");
                }
                else {
                    tvDateError.setText("To-Date must be after From-Date!");
                }
            }
        });
        return view;
    }

    private void loadReport() {
    }

    private boolean checkDate() {
        String[] fromDate = tvFromDate.getText().toString().split("/");
        String[] toDate = tvToDate.getText().toString().split("/");

        int fromYear = Integer.parseInt(fromDate[2]);
        int fromMonth = Integer.parseInt(fromDate[1]);
        int fromDay = Integer.parseInt(fromDate[0]);
        int toYear = Integer.parseInt(toDate[2]);
        int toMonth = Integer.parseInt(toDate[1]);
        int toDay = Integer.parseInt(toDate[0]);

        if (fromYear > toYear) return false;
        if (fromYear == toYear && fromMonth > toMonth) return false;
        if (fromYear == toYear && fromMonth == toMonth && fromDay > toDay) return false;

        return true;
    }
}