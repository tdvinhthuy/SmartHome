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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter {

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

    List<NotificationItem> history;
    public NotificationAdapter(List<NotificationItem> history) {
        this.history = history;
    }


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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = history.get(position);
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
        NotificationViewHolder viewHolder = (NotificationViewHolder) holder;
        viewHolder.imageView.setImageResource(imgSrc);
        // tvContent
        viewHolder.tvContent.setText(item.getNotification());
        // tvTime
        Date date = (Date) item.getTime();
        Format formatter = new SimpleDateFormat("HH:mm, MMM dd, yyyy");
        String time = formatter.format(date);
        viewHolder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public void deleteItem(int position) {
        history.remove(position);
    }

    public void deleteAll() {
        for (int i = getItemCount() - 1; i > -1; i--) {
            deleteItem(i);
        }
    }

}
