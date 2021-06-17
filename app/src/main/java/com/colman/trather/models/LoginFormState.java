package com.colman.trather.models;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
public class LoginFormState {
    @Nullable
    private final Integer usernameError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer fullNameError;
    private final boolean isDataValid;

    public LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
        this(usernameError, passwordError, null);
    }

    public LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError, @Nullable Integer fullNameError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.fullNameError = fullNameError;
        this.isDataValid = false;
    }

    public LoginFormState(boolean isDataValid) {
        this.usernameError = null;
        this.passwordError = null;
        this.fullNameError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getUsernameError() {
        return usernameError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    @Nullable
    public Integer getFullNameError() {
        return fullNameError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }
}