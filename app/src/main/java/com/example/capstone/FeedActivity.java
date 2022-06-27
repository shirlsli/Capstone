package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.capstone.fragments.GenerateFragment;
import com.example.capstone.fragments.PoemDetailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;

public class FeedActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        // two of three fragments currently commented out because they haven't been made yet
//        final Fragment fragment1 = new FeedFragment();
        final Fragment generateFragment = new GenerateFragment();
//        final Fragment fragment3 = new ThirdFragment();
        final Fragment poemDetailsFragment = new PoemDetailsFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_feed:
//                        fragment = fragment1;
                        break;
                    case R.id.action_generate:
                        fragment = generateFragment;
                        break;
                    case R.id.action_archive:
//                        fragment = fragment3;
                        break;
                }
                // Temporary default fragment
                fragment = generateFragment;
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_generate);
        bottomNavigationView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));

    }
}