package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.colman.trather.R;
import com.colman.trather.movdelviews.LoginViewModel;
import com.google.firebase.auth.FirebaseUser;

public class Login extends Fragment {
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (loginViewModel.isLoggedIn()) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.main_screen_list);
        }

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final EditText usernameEditText = view.findViewById(R.id.username);
        final EditText passwordEditText = view.findViewById(R.id.password);
        final Button loginButton = view.findViewById(R.id.login);
        final Button registerButton = view.findViewById(R.id.register);
        final ProgressBar loadingProgressBar = view.findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            loadingProgressBar.setVisibility(View.GONE);

            if (firebaseUser == null) {
                showLoginFailed(getString(R.string.failed_to_login));
            } else {
                updateUiWithUser(firebaseUser);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
/*            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build();
//                 ,new AuthUI.IdpConfig.FacebookBuilder().build(),
//                  new AuthUI.IdpConfig.TwitterBuilder().build())

            signInResultLauncher.launch(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 5));
            */
            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });

        registerButton.setOnClickListener(v -> Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_register));
        return view;
    }

/*
    ActivityResultLauncher<Intent> signInResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        doSomeOperations();
                    }
                }
            });
*/

    private void updateUiWithUser(FirebaseUser firebaseUser) {
        String welcome = getString(R.string.welcome) + firebaseUser.getEmail();
        Toast.makeText(getContext(), welcome, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.main_screen_list);
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(getContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
