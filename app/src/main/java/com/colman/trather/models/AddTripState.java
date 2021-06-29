package com.colman.trather.models;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

public class AddTripState {
    @IdRes
    @Nullable
    private final Integer titleError;
    @IdRes
    @Nullable
    private final Integer locationError;
    @IdRes
    @Nullable
    private final Integer aboutError;
    @Nullable
    @IdRes
    private final Integer imageError;

    public AddTripState(@Nullable Integer titleError, @Nullable Integer locationError, @Nullable Integer aboutError, @Nullable Integer imageError) {
        this.titleError = titleError;
        this.locationError = locationError;
        this.aboutError = aboutError;
        this.imageError = imageError;
    }

    @Nullable
    public Integer getTitleError() {
        return titleError;
    }

    @Nullable
    public Integer getLocationError() {
        return locationError;
    }

    @Nullable
    public Integer getAboutError() {
        return aboutError;
    }

    @Nullable
    public Integer getImageError() {
        return imageError;
    }
}
