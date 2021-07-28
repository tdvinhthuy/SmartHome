package com.example.smarthome.Utils;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthome.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationAdapter extends FirestoreRecyclerAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder> {

    final String LIGHT = "13";
    final String TEMP_HUMID = "7";
    final String LED = "1";
    final String FAN = "10";
    final int LIGHT_HIGH = 0;
    final int LIGHT_LOW = 1;
    final int TEMP_XLOW = 2;
    final int TEMP_LOW = 3;
    final int TEMP_MEDIUM = 4;
    final int TEMP_HIGH = 5;

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView tvContent;
        private TextView tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgNotificationItem);
            tvContent = itemView.findViewById(R.id.tvNotificationContent);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }

    public NotificationAdapter(@NonNull FirestoreRecyclerOptions<NotificationItem> options) {
        super(options);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position, @NonNull NotificationItem item) {
        // imageView
        int imgSrc = R.drawable.ic_launcher_foreground;
        switch (item.getState()) {
            case LIGHT_LOW:
                imgSrc = R.drawable.light_on;
                break;
            case LIGHT_HIGH:
                imgSrc = R.drawable.light_off;
                break;
            case TEMP_LOW:
            case TEMP_MEDIUM:
            case TEMP_HIGH:
                imgSrc = R.drawable.fan_on;
                break;
            case TEMP_XLOW:
                imgSrc = R.drawable.fan_off;
                break;
        }
        holder.imageView.setImageResource(imgSrc);
        // tvContent
        holder.tvContent.setText(item.getNotification());
        // tvTime
        holder.tvTime.setText(item.getStringTime());
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void deleteAll() {
        for (int i = getItemCount() - 1; i > -1; i--) {
            deleteItem(i);
        }
    }
}