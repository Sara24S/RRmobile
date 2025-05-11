package com.example.rapidrestore;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private ArrayList<String> reviewsList;

    public ReviewsAdapter(ArrayList<String> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.reviewText.setText(reviewsList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewText;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewText = itemView.findViewById(android.R.id.text1); // Must use android.R.id.text1
        }
    }
}