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

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private ArrayList<NotificationItem> notificationList;

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

    public NotificationAdapter(ArrayList<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        NotificationViewHolder viewHolder = new NotificationViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem currentItem = notificationList.get(position);
        // imageView
        int imgSrc = R.drawable.ic_launcher_foreground;
        switch (currentItem.getNotificationType()) {
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
        holder.tvContent.setText(currentItem.getContent());
        // tvTime
        holder.tvTime.setText(currentItem.getStringTime());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
