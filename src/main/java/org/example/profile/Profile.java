package org.example.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Profile {
    private final long id;
    private final long userId;
    private String name;

    @JsonCreator
    public Profile(@JsonProperty("id") long id,
                   @JsonProperty("userId") long userId,
                   @JsonProperty("name") String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
}