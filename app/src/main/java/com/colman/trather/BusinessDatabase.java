package com.colman.trather;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.colman.trather.dao.BusinessDao;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.UserDao;
import com.colman.trather.models.Business;
import com.colman.trather.models.Review;
import com.colman.trather.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Business.class, Review.class, User.class}, version = 2)
public abstract class BusinessDatabase extends RoomDatabase {
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile BusinessDatabase INSTANCE;

    public static BusinessDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BusinessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BusinessDatabase.class, "business")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract BusinessDao businessDao();

    public abstract ReviewDao reviewDao();

    public abstract UserDao usersDao();
}
