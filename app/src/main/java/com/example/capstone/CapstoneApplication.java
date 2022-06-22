package com.example.capstone;

import com.example.capstone.models.Line;
import com.example.capstone.models.Poem;
import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Application;

public class CapstoneApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Line.class);
        ParseObject.registerSubclass(Poem.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("xuknWkvt9Z69KS72mu4ZoGt3AzBQnPwL52eQSiQ0")
                .clientKey("tvnLxlXEsslN8S65zmcA4SiScJlOwhLA4kwTza7F")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}