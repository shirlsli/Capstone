package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;
import com.example.capstone.R;
import android.content.Intent;
import android.os.Bundle;
import android.se.omapi.Session;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone.MainActivity;
import com.example.capstone.models.Poem;
import com.example.capstone.models.Post;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button bLogin;
    private Button bSignup;
    private TextView tvIncorrect;
    private Poem poem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvIncorrect = findViewById(R.id.tvIncorrect);
        bLogin = findViewById(R.id.bLogin);
        bSignup = findViewById(R.id.bSignup);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("login_button_click", "Login button was clicked");
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(email, password);
            }
        });
        bSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String email, String password) {
        Log.i("log_in", "Logging user in");
        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e("login_error", "Login failed");
                    tvIncorrect.setVisibility(View.VISIBLE);
                } else {
                    goMainActivity();
                }
            }
        });
    }

    private void goMainActivity() {
        ParseQuery<ParseSession> lastUserSessions = ParseQuery.getQuery(ParseSession.class);
        lastUserSessions.whereEqualTo("user", ParseUser.getCurrentUser());
        lastUserSessions.addDescendingOrder("createdAt");
        lastUserSessions.setLimit(2);
        lastUserSessions.findInBackground(new FindCallback<ParseSession>() {
            @Override
            public void done(List<ParseSession> lastUserSessions, ParseException e) {
                if (e != null) {
                    Log.e("fetch_session_fail", "Issue with getting last user session", e);
                } else {
                    Log.i("fetch_session_succeed", "Succeeded getting last user session");
                    ParseSession curUserSession = lastUserSessions.get(0);
                    // error handling: this may be user's first session
                    if (lastUserSessions.size() > 1) {
                        ParseSession lastUserSession = lastUserSessions.get(1);
                        if (sameDay(curUserSession.getCreatedAt(), lastUserSession.getUpdatedAt())) {
                            ParseQuery<Poem> poemQuery = ParseQuery.getQuery(Poem.class);
                            poemQuery.include(Poem.KEY_AUTHORS);
                            poemQuery.whereEqualTo("user", ParseUser.getCurrentUser());
                            poemQuery.addDescendingOrder("createdAt");
                            poemQuery.setLimit(1);
                            poemQuery.findInBackground(new FindCallback<Poem>() {
                                @Override
                                public void done(List<Poem> todayPoem, ParseException e) {
                                    if (e != null) {
                                        Log.e("poem_not_fetched", "Issue with getting today's poem", e);
                                    } else {
                                        poem = todayPoem.get(0);
                                        Log.i("poem_when_login", "Poem when log in: " + poem);
                                    }
                                }
                            });
                        }
                    } else {
                        poem = new Poem();
                        poem.addAuthor(ParseUser.getCurrentUser());
                    }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("poem", Parcels.wrap(poem));
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean sameDay(Date d1, Date d2) {
        return d1.getDate() == d2.getDate() &&
                d1.getMonth() == d2.getMonth() &&
                d1.getYear() == d2.getYear();
    }

}