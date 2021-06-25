package com.colman.trather.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Objects;

@Entity(primaryKeys = {"uid"}, indices = {
        @Index(value = "uid", unique = true)
})
public class User {

    @NonNull
    @ColumnInfo(name = "uid")
    String uid;
    @NonNull
    @ColumnInfo(name = "email")
    String email;
    @ColumnInfo(name = "imageUrl")
    String imageUrl;
    @ColumnInfo(name = "bio")
    String bio;
    @ColumnInfo(name = "fullname")
    String fullname;

    public User(String uid, String imageUrl, String bio, String fullname, String email) {
        this.uid = uid;
        this.imageUrl = imageUrl;
        this.bio = bio;
        this.fullname = fullname;
        this.email = email;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(imageUrl, user.imageUrl) &&
                Objects.equals(bio, user.bio) &&
                Objects.equals(fullname, user.fullname) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl, bio, fullname, email);
    }
}
