package com.in.cafe.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

// import com.google.gson.Gson;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;
// import com.google.gson.reflect.TypeToken;

public class CafeUtils {

    private CafeUtils() {

    }

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return new ResponseEntity<String>("{\"message\":\"" + responseMessage + "\"}", httpStatus);
    }

    public static String getUUID() {
        Date date = new Date();
        long time = date.getTime();
        return "Bill-" + time;
    }

    public static JSONArray getJsonArrayFromString(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        return jsonArray;
    }

    // public static Map<String, Object> getMapFromJson(String data) {
    // if (!Strings.isNullOrEmpty(data)) {
    // return new Gson().fromJson(data, new TypeToken<Map<String, Object>>() {
    // }.getType());
    // }
    // return new HashMap<>();
    // }

    public static Map<String, Object> getMapFromJson(String data) {
        if (data != null && !data.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper instance
                // Convert JSON string to Map
                return objectMapper.readValue(data, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                return new HashMap<>(); // Return empty map on error
            }
        }
        return new HashMap<>(); // Return empty map if data is null or empty
    }
}
