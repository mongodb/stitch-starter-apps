package com.mongodb.stitch.starter_app.model.objects;

import com.mongodb.stitch.android.core.auth.StitchUser;

public class User {
    private final StitchUser _stitchUser;

    public User(StitchUser stitchUser){
        _stitchUser = stitchUser;
    }

    public String getEmail(){
        return _stitchUser.getProfile().getEmail();
    }

    public String getProviderName(){
        return _stitchUser.getLoggedInProviderName();
    }
}
