package com.example.smarthome.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Activity.MainActivity;
import com.example.smarthome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StatisticsFragment extends Fragment {
    private View layoutStatisticGen, layoutStatisticSpec;

    private TextView btnToday, btnYesterday, btnThisWeek, btnThisMonth;

    // For Range Option
    private TextView tvFromDate, tvToDate, tvDateError;
    private TextView btnGo;

    // On Statistic Spec Page
    private ImageView btnBackToStatisticGen;
    private TextView tv_statisticOption;
    private TextView tv_AvgTempVal, tv_AvgFanOpTimeVal, tv_AvgLightIntenseVal, tv_AvgLightOpTimeVal;
    DatePickerDialog.OnDateSetListener FromDateListener;
    DatePickerDialog.OnDateSetListener ToDateListener;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference lightRecord;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        lightRecord = db.collection("light_records");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        layoutStatisticGen = view.findViewById(R.id.layoutStatisticGen);
        layoutStatisticSpec = view.findViewById(R.id.layoutStatisticSpec);
        layoutStatisticGen.setVisibility(View.VISIBLE);
        layoutStatisticSpec.setVisibility(View.GONE);

        Context context = view.getContext();

        btnToday = view.findViewById(R.id.tv_todayOption);
        btnYesterday = view.findViewById(R.id.tv_yesterdayOption);
        btnThisWeek = view.findViewById(R.id.tv_thisWeekOption);
        btnThisMonth = view.findViewById(R.id.tv_thisMonthOption);

        // Range Option
        tvFromDate = view.findViewById(R.id.tv_fromDate);
        tvToDate = view.findViewById(R.id.tv_toDate);
        tvDateError = view.findViewById(R.id.tvDateError);
        btnGo = view.findViewById(R.id.btnGo);

        // On StatisticSpec Page
        btnBackToStatisticGen = view.findViewById(R.id.imgBackStatistic);
        tv_statisticOption = view.findViewById(R.id.statisticOption);
        tv_AvgTempVal = view.findViewById(R.id.avgTempVal);
        tv_AvgFanOpTimeVal = view.findViewById(R.id.avgFanOpTimeVal);
        tv_AvgLightIntenseVal = view.findViewById(R.id.avgLightIntenseVal);
        tv_AvgLightOpTimeVal = view.findViewById(R.id.avgLightOpTimeVal);


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


        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("Today");
                tv_statisticOption.setTextColor(Color.parseColor("#2A954E"));
            }
        });

        btnYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("Yesterday");
                tv_statisticOption.setTextColor(Color.parseColor("#CA7240"));
            }
        });

        btnThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("This Week");
                tv_statisticOption.setTextColor(Color.parseColor("#B2B600"));
            }
        });


        btnThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("This Month");
                tv_statisticOption.setTextColor(Color.parseColor("#176BB8"));
            }
        });

        // button click
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDate() == "good") {
                    loadStatisticSpecLayout();
                    tv_statisticOption.setText(tvFromDate.getText().toString() + " - " + tvToDate.getText().toString());
                    tv_statisticOption.setTextColor(Color.parseColor("#3CA0FD"));
//                    loadReport();
                }
                else if (checkDate() == "invalid") {
                    tvDateError.setText("To-Date must be after From-Date!");
                }
                else if (checkDate() == "exceedCurrentDay") {
                    tvDateError.setText("Date cannot exceed today!");
                }
            }
        });


        btnBackToStatisticGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutStatisticGen.setVisibility(View.VISIBLE);
                layoutStatisticSpec.setVisibility(View.GONE);
            }
        });


        return view;
    }

    private void loadStatisticSpecLayout() {
        // change layout
        layoutStatisticGen.setVisibility(View.GONE);
        layoutStatisticSpec.setVisibility(View.VISIBLE);
        tvDateError.setText("");
    }

    private void loadReport(Date fromDate, Date toDate, String optionValue) {

    }


    private String checkDate() {
        String[] fromDate = tvFromDate.getText().toString().split("/");
        String[] toDate = tvToDate.getText().toString().split("/");

        int fromYear = Integer.parseInt(fromDate[2]);
        int fromMonth = Integer.parseInt(fromDate[1]);
        int fromDay = Integer.parseInt(fromDate[0]);
        int toYear = Integer.parseInt(toDate[2]);
        int toMonth = Integer.parseInt(toDate[1]);
        int toDay = Integer.parseInt(toDate[0]);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

//        if (fromYear > year || fromMonth > month + 1 || fromDay > day) return "exceedCurrentDay";

        if (fromYear > year || toYear > year) return "exceedCurrentDay";
        if ((fromYear == year && fromMonth > month) || (toYear == year && toMonth > month)) return "exceedCurrentDay";
        if ((fromYear == year && fromMonth == month && fromDay > day) || (toYear == year && toMonth == month && toDay > day)) return "exceedCurrentDay";

        if (fromYear > toYear) return "invalid";
        if (fromYear == toYear && fromMonth > toMonth) return "invalid";
        if (fromYear == toYear && fromMonth == toMonth && fromDay > toDay) return "invalid";

        return "good";
    }

    private String[] firstAndLastDaysOfWeek() {
        String[] result = new String[2];
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        result[0] = dateFormat.format(date.getTime());
        date.add(Calendar.DATE, 6);
        result[1] = dateFormat.format(date.getTime());
        return result;
    }

}