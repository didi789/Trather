package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.colman.trather.Consts;
import com.colman.trather.models.Business;
import com.colman.trather.models.Review;
import com.colman.trather.repositories.BusinessRepository;
import com.colman.trather.repositories.ReviewRepository;
import com.colman.trather.services.SharedPref;

import java.util.List;

public class BusinessInfoViewModel extends AndroidViewModel {
    private final LiveData<List<Review>> reviewsLiveData;
    private final LiveData<List<Business>> businessLiveData;
    private final BusinessRepository businessRepository;
    private final ReviewRepository reviewRepository;

    public BusinessInfoViewModel(@NonNull Application application) {
        super(application);
        businessRepository = new BusinessRepository(application);
        reviewRepository = new ReviewRepository(application);
        reviewsLiveData = reviewRepository.getReviewsLiveData();
        businessLiveData = businessRepository.getBusinesses();
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return reviewsLiveData;
    }

    public LiveData<List<Business>> getBusinessesLiveData() {
        return businessLiveData;
    }

    public LiveData<Business> getBusinessByIdLiveData(int businessId) {
        return Transformations.switchMap(businessLiveData, id ->
                businessRepository.getBusinessById(businessId));
    }

    public LiveData<List<Review>> getReviewsByBusinessIdLiveData(int businessId) {
        return Transformations.switchMap(reviewsLiveData, reviewList ->
                reviewRepository.getReviewsById(businessId)
        );
    }

    public void deleteReview(Review review) {
        businessRepository.getBusinessById(review.getReviewId()).observeForever(business -> reviewRepository.deleteReview(business, review));
    }

    public void addReview(Business business, String review, int stars) {
        final String currentUserEmail = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
        final Review review1 = new Review(business.getBusinessId(), currentUserEmail, review, "", stars);
        reviewRepository.addReview(business, review1);
    }
}

