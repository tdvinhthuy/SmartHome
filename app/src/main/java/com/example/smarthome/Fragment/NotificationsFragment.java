package com.example.smarthome.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthome.R;
import com.example.smarthome.Utils.NotificationAdapter;
import com.example.smarthome.Utils.NotificationItem;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference notificationRef;
    private NotificationAdapter adapter;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ImageView clearNotification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        notificationRef = db.collection("notifications");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        if (adapter.getItemCount() == 0) clearNotification.setVisibility(View.GONE);

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        // recycler view
        recyclerView = view.findViewById(R.id.rvNotification);
        setupAdapter();
        // clear
        clearNotification = view.findViewById(R.id.clearNotification);
        clearNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.deleteAll();
            }
        });

        return view;
    }

    private void setupAdapter() {
        Query query = notificationRef.orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<NotificationItem> options = new FirestoreRecyclerOptions.Builder<NotificationItem>()
                .setQuery(query, NotificationItem.class)
                .build();

        adapter = new NotificationAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(0);
                clearNotification.setVisibility(View.VISIBLE);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (adapter.getItemCount() == 0) clearNotification.setVisibility(View.GONE);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }
}