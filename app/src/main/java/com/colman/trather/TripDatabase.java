package com.colman.trather;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.TripDao;
import com.colman.trather.dao.UserDao;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Trip.class, Review.class, User.class}, version = 1)
public abstract class TripDatabase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile TripDatabase INSTANCE;

    public static TripDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TripDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TripDatabase.class, "trip")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract TripDao tripDao();

    public abstract ReviewDao reviewDao();

    public abstract UserDao usersDao();
}
