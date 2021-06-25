package com.colman.trather.movdelviews;

import android.app.Application;
import android.util.Patterns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.R;
import com.colman.trather.models.LoginFormState;
import com.colman.trather.repositories.RegisterRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterViewModel extends AndroidViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final RegisterRepository registerRepository;

    public RegisterViewModel(Application application) {
        super(application);
        this.registerRepository = new RegisterRepository();
        userMutableLiveData = registerRepository.getUser();
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return userMutableLiveData;
    }

    public void register(String username, String password, String fullName) {
        registerRepository.register(username, password, fullName);
    }

    public void registerDataChanged(String username, String password, String fullname) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null, null));
        } else if (!isFullnameValid(fullname)) {
            loginFormState.setValue(new LoginFormState(null, null, R.string.invalid_name));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password, null));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isFullnameValid(String fullname) {
        return fullname != null && fullname.length() >= 2 && fullname.matches("^[a-zA-Z ]*$");
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public boolean isLoggedIn() {
        return registerRepository.isLoggedIn();
    }
}