package com.example.lib;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import jdk.nashorn.internal.parser.JSONParser;

public class MainGET {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://sleepy-taiga-08133.herokuapp.com/rtcToken");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // Response code
            int responseCode = conn.getResponseCode();
            if(responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                InputStream in = url.openStream(); //Read from a file, or a HttpRequest, or whatever.

                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                Token token = new Gson().fromJson(reader, Token.class);
                System.out.println("Token Id: " + token.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Token {
    String id;
}
