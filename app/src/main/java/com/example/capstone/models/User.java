package com.example.capstone.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("_User")
public class User extends ParseUser {
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_PROFILE_PIC = "profilePic";
    public static final String KEY_USERNAME = "username";

    public List<User> getFriends() {
        return getList(KEY_FRIENDS);
    }

    public void addFriends(ParseUser user) { add(KEY_FRIENDS, user); }

    public ParseFile getProfilePic() {
        return getParseFile(KEY_PROFILE_PIC);
    }

    public void setProfilePic(ParseFile profilePic) { put(KEY_PROFILE_PIC, profilePic); }

    public String getUsername() { return getString(KEY_USERNAME); }
}
