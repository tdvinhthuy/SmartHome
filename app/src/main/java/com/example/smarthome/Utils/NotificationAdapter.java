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

import java.util.ArrayList;

public class NotificationAdapter extends FirestoreRecyclerAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder> {

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
        switch (item.getNotificationType()) {
            case LIGHT_ON:
                imgSrc = R.drawable.light_on;
                break;
            case LIGHT_OFF:
                imgSrc = R.drawable.light_off;
                break;
            case FAN_LOW:
            case FAN_MEDIUM:
            case FAN_HIGH:
                imgSrc = R.drawable.fan_on;
                break;
            case FAN_OFF:
                imgSrc = R.drawable.fan_off;
                break;
        }
        holder.imageView.setImageResource(imgSrc);
        // tvContent
        holder.tvContent.setText(item.getContent());
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
