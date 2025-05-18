package com.example.rapidrestore;

import android.view.View;
import android.view.ViewGroup;
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
        TextView tv = new TextView(parent.getContext());
        tv.setPadding(16, 16, 16, 16);
        tv.setTextSize(18);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(timeList.get(position));
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
