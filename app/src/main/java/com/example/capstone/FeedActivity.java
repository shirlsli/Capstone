package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

//import com.example.capstone.fragments.ArchiveFragment;
import com.example.capstone.fragments.ArchiveFragment;
import com.example.capstone.fragments.FeedFragment;
import com.example.capstone.fragments.GenerateFragment;
import com.example.capstone.fragments.PoemDetailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;
import com.parse.ParseUser;

public class FeedActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    public boolean activateTutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        activateTutorial = getIntent().getBooleanExtra("activateTutorial", false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.feed));

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Fragment feedFragment = new FeedFragment();
        final Fragment archiveFragment = new ArchiveFragment();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        fragment = feedFragment;
                        break;
                    case R.id.action_archive:
                        fragment = archiveFragment;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack( "feed_fragment" ).commit();
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_feed);
        bottomNavigationView.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.action_logout) {
            ParseUser.logOutInBackground();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
            Intent intent = new Intent(FeedActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean getActivateTutorial() {
        return activateTutorial;
    }

}