package com.example.smarthome.Fragment;

import android.app.DatePickerDialog;
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class StatisticsFragment extends Fragment {
    final String LIGHT = "13";
    final String TEMP_HUMID = "7";
    final String LED = "1";
    final String FAN = "10";
    final int LIGHT_HIGH = 0;
    final int LIGHT_LOW = 1;
    final int TEMP_XLOW = 2;
    final int TEMP_LOW = 3;
    final int TEMP_MEDIUM = 4;
    final int TEMP_HIGH = 5;

    enum DATE_ERRORS {
        NO_ERROR,
        INVALID,
        EXCEED_CURRENTDAY
    }

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
    private CollectionReference lightRecord, tempRecord, fanState, lightState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
//        lightRecord = db.collection("Records").whereEqualTo("device", LIGHT);
//        tempRecord = db.collection("temp_humid_records");
//        fanState = db.collection("fan_states");
//        lightState = db.collection("light_states");
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

        // Yesterday
        String yesterday;
        Calendar date = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("d/M/yyyy");
        date.add(Calendar.DATE, -1);
        yesterday = dateFormat.format(date.getTime());

        // default value
        tvFromDate.setText(yesterday);
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
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String todayDate = String.format("%s/%s/%s", day, month+1, year);
                Log.d("TODAYTIME_NOW", todayDate);
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
                Log.d("YESTERDAY", String.valueOf(yesterday));
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
                else if (checkDate() == DATE_ERRORS.EXCEED_CURRENTDAY) {
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
        Date toDayDate = new Date();
        long currentTimeInMillis = toDayDate.getTime();
//        Log.d("TO_DAY_DATE", "Current date using Date = "+toDayDate.toString());
//        Log.d("TO_DAY_IN_MILLIS", "Current time in milliseconds using Date = "+toDayInMillis);

        // Avg Light Intensity
        db.collection("Records")
                .whereEqualTo("device", LIGHT)
                .orderBy("time", Query.Direction.ASCENDING)
                .whereGreaterThan("time", new Date(fromDateInMillis))
                .whereLessThan("time", new Date(toDateInMillis))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            tv_AvgLightIntenseVal.setText("No Data");
                            return;
                        }
                        int totalLightIntense = 0;
                        int count = querySnapshot.size();
                        for (DocumentSnapshot document : querySnapshot) {
                            totalLightIntense = totalLightIntense + Integer.parseInt(document.getString("value"));
                        }
                        double avgLightIntensity = Math.ceil((double) totalLightIntense / (double) count);
                        tv_AvgLightIntenseVal.setText(String.format("%.0f lux", avgLightIntensity));
                    }
                });

        // Avg Temperature
        db.collection("Records").whereEqualTo("device", TEMP_HUMID)
                .orderBy("time", Query.Direction.ASCENDING)
                .whereGreaterThan("time", new Date(fromDateInMillis))
                .whereLessThan("time", new Date(toDateInMillis))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            tv_AvgTempVal.setText("No Data");
                            return;
                        }
                        int totalTemp = 0;
                        int count = querySnapshot.size();
                        for (DocumentSnapshot document : querySnapshot) {
                            totalTemp = totalTemp + Integer.parseInt(document.getString("value").split("-")[0]);
                        }
                        double avgLightIntensity = (double) totalTemp / (double) count;
                        tv_AvgTempVal.setText(String.format("%.1f ˚C", avgLightIntensity));
                    }
                });

        // Avg Fan Operating Time
        db.collection("States").whereEqualTo("device", FAN)
                .orderBy("time", Query.Direction.ASCENDING)
                .whereGreaterThan("time", new Date(fromDateInMillis))
                .whereLessThan("time", new Date(toDateInMillis))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            tv_AvgFanOpTimeVal.setText("No Data");
                            return;
                        };
                        long totalFanOpTime = 0;
                        int numberOfState = querySnapshot.size();

                        int nthState = 1;
                        int timesOfFanInOp = 0;
                        boolean lastState = false;
                        for (DocumentSnapshot document : querySnapshot) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                String state = document.get("state").toString();
                                Timestamp timestamp = (Timestamp) document.get("time");
                                long timestampInMillis = timestamp.toDate().getTime();
                                if (nthState == 1) {
                                    if (Objects.equals(state, String.valueOf(TEMP_XLOW))) {
                                        totalFanOpTime = totalFanOpTime - fromDateInMillis;
                                        lastState = true;
                                    }
                                }
                                if (nthState == numberOfState) {
                                    if (!Objects.equals(state, String.valueOf(TEMP_XLOW))) {
                                        long currentTimeInMillis = toDayDate.getTime();
                                        if (toDateInMillis <= currentTimeInMillis) totalFanOpTime = totalFanOpTime + toDateInMillis;
                                        else totalFanOpTime = totalFanOpTime + currentTimeInMillis;
                                    }
                                }

                                if (Objects.equals(state, String.valueOf(TEMP_XLOW))) {
                                    if (lastState == true) {
                                        lastState = false;
                                        totalFanOpTime = totalFanOpTime + timestampInMillis;
                                    }
                                }
                                else {
                                    if (lastState == false) {
                                        totalFanOpTime = totalFanOpTime - timestampInMillis;
                                        lastState = true;
                                    }
                                }
                            }
                            nthState++;
                        }
                        // Convert total time to second
                        totalFanOpTime /= 1000;
                        // Calculate hours, mins and secs
                        long numberOfHour = totalFanOpTime/3600L;
                        long numberOfMin = (totalFanOpTime - numberOfHour*3600L)/60L;
                        long numberOfSec = totalFanOpTime - numberOfHour*3600L - numberOfMin*60L;
                        String output = "";
                        if (numberOfHour > 2) output = output + numberOfHour + "hrs ";
                        else if (numberOfHour > 0) output = output + numberOfHour + "hr ";
                        if (numberOfMin > 2) output = output + numberOfMin + "mins ";
                        else if (numberOfMin > 0) output = output + numberOfMin + "min ";
                        if (numberOfSec > 2) output = output + numberOfSec + "secs";
                        else if (numberOfSec > 0) output = output + numberOfSec + "sec";
                        //Log.d("NUMBER OF FAN STATE", String.valueOf(numberOfState));
                        tv_AvgFanOpTimeVal.setText(output);
                    }
                });

        // Average Light Operating Time
        db.collection("States").whereEqualTo("device", LED)
                .orderBy("time", Query.Direction.ASCENDING)
                .whereGreaterThan("time", new Date(fromDateInMillis))
                .whereLessThan("time", new Date(toDateInMillis))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (querySnapshot == null || querySnapshot.isEmpty()) {
                            tv_AvgLightOpTimeVal.setText("No Data");
                            return;
                        }
                        long totalLightOpTime = 0;
                        int numberOfState = querySnapshot.size();
                        int nthState = 1;
                        int timesOfLightInOp = 0;
                        boolean lastState = false; // Fasle for OFF, True for ON
                        for (DocumentSnapshot document : querySnapshot) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                String state = document.get("state").toString();
                                Timestamp timestamp = (Timestamp) document.get("time");
                                long timestampInMillis = timestamp.toDate().getTime();
                                if (nthState == 1) {
                                    if (Objects.equals(state, String.valueOf(LIGHT_HIGH))) {
                                        totalLightOpTime = totalLightOpTime - fromDateInMillis;
                                        lastState = true;
                                    }
                                }
                                if (nthState == numberOfState) {
                                    if (Objects.equals(state, String.valueOf(LIGHT_LOW))) {
                                        long currentTimeInMillis = toDayDate.getTime();
                                        if (toDateInMillis <= currentTimeInMillis) totalLightOpTime = totalLightOpTime + toDateInMillis;
                                        else totalLightOpTime = totalLightOpTime + currentTimeInMillis;
                                    }
                                }

                                if (Objects.equals(state, String.valueOf(LIGHT_HIGH))) {
                                    if (lastState == true) {
                                        totalLightOpTime = totalLightOpTime + timestampInMillis;
                                        lastState = false;
                                    }
                                }
                                else {
                                    if (lastState == false) {
                                        totalLightOpTime = totalLightOpTime - timestampInMillis;
                                        lastState = true;
                                    }
                                }
                            }
                            nthState++;
                        }
                        // Convert total time to second
                        totalLightOpTime /= 1000;
//                        long avgLightOpTime = totalLightOpTime/timesOfLightInOp;
                        // Calculate hours, mins and secs
                        long numberOfHour = totalLightOpTime/3600L;
                        long numberOfMin = (totalLightOpTime - numberOfHour*3600L)/60L;
                        long numberOfSec = totalLightOpTime - numberOfHour*3600L - numberOfMin*60L;
                        String output = "";
                        if (numberOfHour > 2) output = output + numberOfHour + "hrs ";
                        else if (numberOfHour > 0) output = output + numberOfHour + "hr ";
                        if (numberOfMin > 2) output = output + numberOfMin + "mins ";
                        else if (numberOfMin > 0) output = output + numberOfMin + "min ";
                        if (numberOfSec > 2) output = output + numberOfSec + "secs";
                        else if (numberOfSec > 0) output = output + numberOfSec + "sec";
                        tv_AvgLightOpTimeVal.setText(output);
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

        if (fromYear > year || toYear > year) return DATE_ERRORS.EXCEED_CURRENTDAY;
        if ((fromYear == year && fromMonth > month) || (toYear == year && toMonth > month)) return DATE_ERRORS.EXCEED_CURRENTDAY;
        if ((fromYear == year && fromMonth == month && fromDay > day) || (toYear == year && toMonth == month && toDay > day)) return DATE_ERRORS.EXCEED_CURRENTDAY;

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