package com.example.smarthome;

import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navHome:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.navControl:
                        selectedFragment = new ControlFragment();
                        break;
                    case R.id.navNotifications:
                        selectedFragment = new NotificationsFragment();
                        break;
                    case R.id.navStatistics:
                        selectedFragment = new StatisticsFragment();
                        break;
                    case R.id.navAccount:
                        selectedFragment = new AccountFragment();
                        break;
                    default:
                        break;
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();

                return true;
            }
        });
    }


}