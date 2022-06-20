package com.example.capstone;

import com.parse.Parse;
import android.app.Application;

public class CapstoneApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("xuknWkvt9Z69KS72mu4ZoGt3AzBQnPwL52eQSiQ0")
                .clientKey("tvnLxlXEsslN8S65zmcA4SiScJlOwhLA4kwTza7F")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}