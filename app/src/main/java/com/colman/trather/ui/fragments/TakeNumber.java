package com.colman.trather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colman.trather.Consts;
import com.colman.trather.R;
import com.colman.trather.models.Business;
import com.colman.trather.viewModels.TakeNumberViewModel;
import com.colman.trather.services.Utils;
import com.colman.trather.ui.adapters.QueueRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class TakeNumber extends BaseToolbarFragment {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    QueueRecyclerViewAdapter adapter;
    ArrayList<String> queue;
    Business business;
    String userEmail = "", fullName = "";
    Button deleteMtNameButton, addMyNameButton;
    private TakeNumberViewModel takeNumberViewModel;
    private TextView businessName;
    private int businessId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessId = getArguments().getInt(Consts.BUSINESS_ID);
        takeNumberViewModel = new ViewModelProvider(requireActivity()).get(TakeNumberViewModel.class);
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        businessName = view.findViewById(R.id.businessName);
        recyclerView = view.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this.requireActivity());
        recyclerView.setLayoutManager(layoutManager);
        deleteMtNameButton = view.findViewById(R.id.deleteMyNameButton);
        addMyNameButton = view.findViewById(R.id.addMyNameButton);


        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);


        deleteMtNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (queue.contains(userEmail))
                    for (int i = 0; i < queue.size(); i++)
                        if (queue.get(i).equals(userEmail)) {
                            queue.remove(i);
                            takeNumberViewModel.updateQueue(queue, business);
                            // Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_back_business_info);
                        }
            }
        });

        addMyNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!queue.contains(userEmail)) {
                    queue.add(userEmail);
                    takeNumberViewModel.updateQueue(queue, business);
                }
            }
        });

        userEmail = getUserEmail();

        //אין לי מושג למה לא מצליח למשוך את השם המלא של היוזר, צריך לבדוק.
        /*
        takeNumberViewModel.getUserByEmailLiveData(userEmail).observe(getViewLifecycleOwner(),user -> {
            if (user == null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_back_business_info);
            Toast.makeText(requireActivity(),user.getFullname(),Toast.LENGTH_LONG).show();
            }
        });
        */

        getBusinessAndSetRecyclerView();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(requireActivity(), "business id is:" + businessId, Toast.LENGTH_LONG).show();
    }

    @Override
    protected int getTitleResourceId() {
        return R.string.take_a_number;
    }

    @Override
    protected int getActionId() {
        return R.id.take_number_to_settings;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_take_number;
    }


    private String getUserEmail() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return firebaseUser.getEmail();
        } else {
            Toast.makeText(this.requireActivity(), "error fetching user email", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void getBusinessAndSetRecyclerView() {
        takeNumberViewModel.getBusinessByIdLiveData(businessId).observe(getViewLifecycleOwner(), businessInfo -> {
            if (businessInfo == null) {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(R.id.action_back_business_info);
            }
            //if  queue day is up-to-date - return true.
            takeNumberViewModel.checkBusinessQueueDateAndResetIfNeeded(businessInfo);
            businessName.setText(businessInfo.getName());
            queue = new ArrayList<String>();
            String tempQueue = businessInfo.getQueue();
            queue = Utils.fromString(tempQueue);
            business = businessInfo;
            adapter = new QueueRecyclerViewAdapter(queue, userEmail);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            //listen for changes in queue
            takeNumberViewModel.listenForQueueChanges(businessInfo);
        });
    }

}
