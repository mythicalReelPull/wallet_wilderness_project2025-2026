package org.example;

import org.example.auth.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void testUserConstructor() {
        User user = new User(1, "testUser", "hashedPass");
        assertEquals("testUser", user.getUsername());
        assertEquals(1, user.getId());
    }
}