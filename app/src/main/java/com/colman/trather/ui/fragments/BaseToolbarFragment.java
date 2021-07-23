package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.colman.trather.R;

public abstract class BaseToolbarFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setElevation(4 * this.getResources().getDisplayMetrics().density);
        activity.getSupportActionBar().setTitle(getTitleResourceId());
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (shouldAddSettingIcon())
            inflater.inflate(R.menu.main_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    protected boolean shouldAddSettingIcon() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(getSettingsActionId());
                return true;

            case android.R.id.home:
                requireActivity().onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract int getTitleResourceId();

    protected abstract int getSettingsActionId();

    protected abstract int getLayoutId();

}
