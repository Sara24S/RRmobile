package com.example.rapidrestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ViewHolder> {
    private List<String> timeList;

    public TimeListAdapter(List<String> timeList) {
        this.timeList = timeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.timeTextView.setText(time);

        holder.deleteIcon.setOnClickListener(v -> {
            timeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, timeList.size());
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        ImageView deleteIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
