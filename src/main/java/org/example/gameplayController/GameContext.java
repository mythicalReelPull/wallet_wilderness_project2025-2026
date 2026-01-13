package org.example.gameplayController; // <--- FIX: Match the folder name in your screenshot

import org.example.GameState;
import org.example.Player;
import org.example.auth.User;
import org.example.profile.Profile;

public class GameContext {

    public Profile currentProfile;
    public Player player;
    public GameState currentState;
    public boolean sessionSaved;
    public User currentUser;

    // <--- ADD THIS to fix the "cannot find symbol" error
    public long browsingProfileId;
}