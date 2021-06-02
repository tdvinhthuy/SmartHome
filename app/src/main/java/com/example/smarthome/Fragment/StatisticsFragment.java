package com.example.smarthome.Fragment;

import android.app.DatePickerDialog;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import com.example.smarthome.Activity.MainActivity;
import com.example.smarthome.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

enum DATE_ERRORS {
    NO_ERROR,
    INVALID,
    EXCEEDCURRENTDAY
}
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

//        Context context = view.getContext();

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
                String todayDate = String.format("%s/%s/%s", day, month+1, year);
                Log.d("TODAY", todayDate);
                loadReport(todayDate, todayDate);
            }
        });

        btnYesterday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("Yesterday");

                String yesterday;
                Calendar date = Calendar.getInstance();
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                date.add(Calendar.DATE, -1);
                yesterday = dateFormat.format(date.getTime());

                loadReport(yesterday, yesterday);
//                Log.d("YESTERDAY DATE", yesterday);
            }
        });

        btnThisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("This Week");
                String[] firstAndLastDaysOfWeek = firstAndLastDaysOfWeek();
                loadReport(firstAndLastDaysOfWeek[0], firstAndLastDaysOfWeek[1]);
            }
        });


        btnThisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStatisticSpecLayout();
                tv_statisticOption.setText("This Month");
                String[] firstAndLastDayOfMonth = new String[2];
                Calendar date = Calendar.getInstance();
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                int maxDayOfMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
                int currentDay = date.get(Calendar.DATE);
                if (currentDay != 1) date.add(Calendar.DATE, -(currentDay - 1));
                firstAndLastDayOfMonth[0] = dateFormat.format(date.getTime());
                date.add(Calendar.DATE, maxDayOfMonth - 1);
                firstAndLastDayOfMonth[1] = dateFormat.format(date.getTime());
                loadReport(firstAndLastDayOfMonth[0], firstAndLastDayOfMonth[1]);
//                Log.d("DAYS OF MONTH", firstAndLastDayOfMonth[0] + " - " + firstAndLastDayOfMonth[1]);
            }
        });

        // button click
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDate() == DATE_ERRORS.NO_ERROR) {
                    loadStatisticSpecLayout();
                    tv_statisticOption.setText(tvFromDate.getText().toString() + " - " + tvToDate.getText().toString());

                    loadReport(tvFromDate.getText().toString(), tvToDate.getText().toString());
                }
                else if (checkDate() == DATE_ERRORS.INVALID) {
                    tvDateError.setText("To-Date must be after From-Date!");
                }
                else if (checkDate() == DATE_ERRORS.EXCEEDCURRENTDAY) {
                    tvDateError.setText("Date cannot exceed today!");
                }
            }
        });


        btnBackToStatisticGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutStatisticGen.setVisibility(View.VISIBLE);
                layoutStatisticSpec.setVisibility(View.GONE);
                tvDateError.setText("");
            }
        });


        return view;
    }

    private void loadStatisticSpecLayout() {
        // change layout
        layoutStatisticGen.setVisibility(View.GONE);
        layoutStatisticSpec.setVisibility(View.VISIBLE);
    }

    private void loadReport(String fromDate, String toDate) {
        long fromDateInMillis = convertDateStringToMils(fromDate);
        long toDateInMillis = convertDateStringToMils(toDate) + 86400000L;

        // Avg Light Intensity
        lightRecord.orderBy("timestamp", Query.Direction.ASCENDING)
                .whereGreaterThan("timestamp", new Date(fromDateInMillis))
                .whereLessThan("timestamp", new Date(toDateInMillis)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int totalLightIntense = 0;
                            int count = 0;
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
//                                Log.d("INFORMATION", document.getId() + " => " + document.getData().get("data"));
                                totalLightIntense = totalLightIntense + Integer.parseInt(Objects.requireNonNull(document.getData().get("data")).toString());
                                count = count + 1;
                            }
                            double avgLightIntensity = Math.ceil((double) totalLightIntense / (double) count);
                            tv_AvgLightIntenseVal.setText(String.format("%s lux", String.format("%.0f", avgLightIntensity)));
                        } else {
                            Log.d("ERROR", "Error getting documents: ", task.getException());
                        }
                    }
                });


    }


    private DATE_ERRORS checkDate() {
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
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

//        if (fromYear > year || fromMonth > month + 1 || fromDay > day) return "exceedCurrentDay";

        if (fromYear > year || toYear > year) return DATE_ERRORS.EXCEEDCURRENTDAY;
        if ((fromYear == year && fromMonth > month) || (toYear == year && toMonth > month)) return DATE_ERRORS.EXCEEDCURRENTDAY;
        if ((fromYear == year && fromMonth == month && fromDay > day) || (toYear == year && toMonth == month && toDay > day)) return DATE_ERRORS.EXCEEDCURRENTDAY;

        if (fromYear > toYear) return DATE_ERRORS.INVALID;
        if (fromYear == toYear && fromMonth > toMonth) return DATE_ERRORS.INVALID;
        if (fromYear == toYear && fromMonth == toMonth && fromDay >= toDay) return DATE_ERRORS.INVALID;

        return DATE_ERRORS.NO_ERROR;
    }

    private String[] firstAndLastDaysOfWeek() {
        String[] result = new String[2];
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        result[0] = dateFormat.format(date.getTime());
        date.add(Calendar.DATE, 6);
        result[1] = dateFormat.format(date.getTime());
        return result;
    }

    private long convertDateStringToMils (String stringDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(stringDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date != null ? date.getTime() : 0;
    }

}