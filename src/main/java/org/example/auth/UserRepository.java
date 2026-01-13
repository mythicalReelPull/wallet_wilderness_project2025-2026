package org.example.auth;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    // Remove the String-based save; use the object-based one instead
    void save(User user);
}
