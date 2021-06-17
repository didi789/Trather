package com.colman.trather.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "reviews", foreignKeys = {@ForeignKey(
        entity = Business.class,
        childColumns = "reviewId",
        parentColumns = "businessId",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)})
public class Review {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int reviewId;
    private String author;
    private String comment;
    private String profileImgUrl;
    private long stars;
    @Ignore()
    private boolean isMe = false;

    public Review(int reviewId, String author, String comment, String profileImgUrl, long stars) {
        this.reviewId = reviewId;
        this.author = author;
        this.comment = comment;
        this.profileImgUrl = profileImgUrl;
        this.stars = stars;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id == review.id &&
                reviewId == review.reviewId &&
                stars == review.stars &&
                Objects.equals(author, review.author) &&
                Objects.equals(comment, review.comment) &&
                Objects.equals(profileImgUrl, review.profileImgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reviewId, author, comment, profileImgUrl, stars);
    }
}
