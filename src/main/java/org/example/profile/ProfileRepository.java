package org.example.profile;

import java.util.List;

public interface ProfileRepository {
    List<Profile> findByUserId(long userId);
    void save(Profile profile); // <--- Add this
}