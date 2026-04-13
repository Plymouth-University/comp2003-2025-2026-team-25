package com.example.qtrobot.data.remote.dto;

// a wrapper for the response

/* The outer object { "child": ... } is the wrapper.
Retrofit needs a Java class that matches that outer shape*/

import com.google.gson.annotations.SerializedName;
// @SerializedName -> must match the name in the JSON response and AWS tables

public class GetChildResponse {
    @SerializedName("child")
    public ChildDto child;


    @SerializedName("qr_string")
    public String qr_string;
}
