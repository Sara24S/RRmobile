package com.example.rapidrestore;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class AvailableTimesAdapter extends RecyclerView.Adapter<AvailableTimesAdapter.ViewHolder> {
    private List<String> timeList;
    private OnTimeClickListener listener;
    private Set<String> bookedTimes;
    private String selectedTime = null;

    public interface OnTimeClickListener {
        void onTimeClick(String time);
    }


    public AvailableTimesAdapter(List<String> timeList, Set<String> bookedTimes, OnTimeClickListener listener) {
        this.timeList = timeList;
        this.bookedTimes = bookedTimes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(20, 20, 20, 20);
        tv.setTextSize(18);
        tv.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.time_slot_bg));
        return new ViewHolder(tv);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String time = timeList.get(position);
        holder.textView.setText(time);

        if (bookedTimes.contains(time)) {
            holder.textView.setTextColor(Color.GRAY);  // Booked - gray
            holder.textView.setOnClickListener(null);
            holder.textView.setAlpha(0.5f);
            holder.textView.setBackground(null);
        } else {
            if (time.equals(selectedTime)) {
                holder.textView.setTextColor(Color.WHITE);
                holder.textView.setBackgroundColor(Color.parseColor("#4CAF50")); // green background
            } else {
                holder.textView.setTextColor(Color.BLACK);
                holder.textView.setBackground(null);
            }
            holder.textView.setAlpha(1f);
            holder.textView.setOnClickListener(v -> {
                // Update selected time and refresh the list to update UI
                selectedTime = time;
                notifyDataSetChanged();
                listener.onTimeClick(time);
            });
        }
    }

    public void clearSelection() {
        selectedTime = null;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return timeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View view) {
            super(view);
            textView = (TextView) view;
        }
    }
}

