package com.example.qtrobot.data.remote.dto;

/* Note: DTO stands for Data Transfer Object.
It's a data class with no logic — just fields that match the shape of data coming from or going to an external system
Is a lass to "catch" JSON from AWS and hold it temporarily.*/

public class ChildDto {
    // Maps to the AWS Dynamo DB table fields
    public String childId;
    public String parentId;
    public String preferred_name;
    // public String date_of_birth;
    public String avatar_uri;
    public int score;
    public String settings_favourite_song;
    public String settings_preferred_greeting;
    public String settings_volume_level;
    public long created_at;
    public long updated_at;
    public boolean is_deleted;
}
