package com.smartcity.utils;

import com.google.gson.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(java.time.LocalDateTime.class,
            new com.google.gson.TypeAdapter<java.time.LocalDateTime>() {
                @Override
                public void write(com.google.gson.stream.JsonWriter out, java.time.LocalDateTime value) throws java.io.IOException {
                    out.value(value == null ? null : value.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
                @Override
                public java.time.LocalDateTime read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                    String s = in.nextString();
                    return s == null ? null : java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            })
        .create();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
