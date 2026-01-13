package org.example;

import org.example.profile.Profile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    void testProfileConstructorAndGetters() {
        // Arrange
        long expectedId = 100L;
        long expectedUserId = 50L;
        String expectedName = "Explorer";

        // Act
        Profile profile = new Profile(expectedId, expectedUserId, expectedName);

        // Assert
        assertEquals(expectedId, profile.getId(), "Profile ID should match constructor input");
        assertEquals(expectedUserId, profile.getUserId(), "User ID should match constructor input");
        assertEquals(expectedName, profile.getName(), "Profile name should match constructor input");
    }

    @Test
    void testSetName() {
        // Arrange
        Profile profile = new Profile(1, 1, "OldName");

        // Act
        profile.setName("NewName");

        // Assert
        assertEquals("NewName", profile.getName(), "Name should be updated after calling setName");
    }
}