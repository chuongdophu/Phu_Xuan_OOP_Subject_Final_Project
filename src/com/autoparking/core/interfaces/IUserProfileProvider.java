package com.autoparking.core.interfaces;

import com.autoparking.model.UserProfile;

public interface IUserProfileProvider {
    void saveProfile(UserProfile profile);

    UserProfile loadProfile();
}