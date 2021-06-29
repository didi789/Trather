package com.colman.trather.services;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Utils {
        @TypeConverter
        public static ArrayList<String> fromString(String value) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromArrayList(ArrayList<String> list) {
            Gson gson = new Gson();
            String json = gson.toJson(list);
            return json;
        }

    public static String getLocationText(GeoPoint point) {
        return String.format("(%s,%s)", new DecimalFormat("#.##").format(point.getLatitude()), new DecimalFormat("#.##").format(point.getLongitude()));
    }
}
