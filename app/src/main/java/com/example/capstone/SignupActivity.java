package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.capstone.models.Poem;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.parceler.Parcels;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private Button bSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        bSignup = findViewById(R.id.bSignup);

        ParseUser user = new ParseUser();
        bSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set core properties
                user.setUsername(etUsername.getText().toString());
                user.setPassword(etPassword.getText().toString());
                user.setEmail(etEmail.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i("sign_up_success", "Sign up success!");
                            Poem poem = new Poem();
                            poem.addAuthor(ParseUser.getCurrentUser());
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            intent.putExtra("poem", Parcels.wrap(poem));
                            startActivity(intent);
                            finish();
                        } else {
                            Log.i("sign_up_fail", "Sign up went wrong :(");
                        }
                    }
                });
            }
        });
    }
}