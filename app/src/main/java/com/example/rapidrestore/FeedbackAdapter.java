package com.example.rapidrestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {
    private List<Feedback> feedbackList;

    public FeedbackAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.tvUserType.setText(feedback.getUserType());
        holder.tvComment.setText(feedback.getComment());
        holder.tvTimestamp.setText(feedback.getTimestamp().toDate().toString());
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserType, tvComment, tvTimestamp;

        FeedbackViewHolder(View itemView) {
            super(itemView);
            tvUserType = itemView.findViewById(R.id.tvUserType);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
