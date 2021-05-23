package com.example.smarthome.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.smarthome.Activity.LoginActivity;
import com.example.smarthome.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;

import java.util.*;

public class AccountFragment extends Fragment {
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private String email, lastLogin, createdAccount;
    private TextView tvNameAccount;
    private TextView tvPhoneAccount;
    private TextView tvEmailAccount;
    private TextView tvLastLoginAccount;
    private TextView tvCreatedAccount;

    public AccountFragment() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveAccountInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(com.example.smarthome.R.layout.fragment_account, container, false);
        tvNameAccount = view.findViewById(com.example.smarthome.R.id.tvNameAccount);
        tvEmailAccount = view.findViewById(com.example.smarthome.R.id.tvEmailAccount);
        tvPhoneAccount = view.findViewById(com.example.smarthome.R.id.tvPhoneAccount);
        tvLastLoginAccount = view.findViewById(com.example.smarthome.R.id.tvLastLoginAccount);
        tvCreatedAccount = view.findViewById(com.example.smarthome.R.id.tvCreatedAccount);
        // button LOG OUT
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void retrieveAccountInfo() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Email
            email = user.getEmail();
            // Last login
            lastLogin = convertTimeWithTimeZone(Objects.requireNonNull(user.getMetadata()).getLastSignInTimestamp());
            // Created date
            createdAccount = convertTimeWithTimeZone(user.getMetadata().getCreationTimestamp());

            db.collection("users")
                    .whereEqualTo("email", email)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.w("QUERY_TAG", "Listen failed.", error);
                                return;
                            }

                            for (DocumentSnapshot document: querySnapshot) {
                                tvNameAccount.setText(document.getString("fullname"));
                                tvPhoneAccount.setText(document.getString("phone"));
                                tvEmailAccount.setText(email);
                                tvLastLoginAccount.setText(lastLogin);
                                tvCreatedAccount.setText(createdAccount);
                            }
                        }
                    });
        }
    }

    public String convertTimeWithTimeZone(long time){
        String months[] = {
                "Jan", "Feb", "Mar", "Apr",
                "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"};
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Saigon"));
        cal.setTimeInMillis(time);
        return (cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE) + ":"
                + cal.get(Calendar.SECOND) + ", "
                + months[cal.get(Calendar.MONTH)] + " "
                + cal.get(Calendar.DAY_OF_MONTH) + ", "
                + cal.get(Calendar.YEAR));
    }
}