package com.colman.trather.movdelviews;

import android.app.Application;
import android.util.Patterns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.R;
import com.colman.trather.models.LoginFormState;
import com.colman.trather.repositories.LoginRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final LoginRepository loginRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData = new MutableLiveData<>();

    public LoginViewModel(Application application) {
        super(application);
        this.loginRepository = LoginRepository.getInstance();
        userMutableLiveData = loginRepository.getUser();
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return userMutableLiveData;
    }

    public void login(String username, String password) {
        loginRepository.login(username, password);
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
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
        return loginRepository.isLoggedIn();
    }
}