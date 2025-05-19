package com.example.rapidrestore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    Context context;
    List<PortfolioPost> portfolioList;
    String date;

    public PortfolioAdapter(Context context, List<PortfolioPost> portfolioList) {
        this.context = context;
        this.date = date;
        this.portfolioList = portfolioList != null ? portfolioList : new ArrayList<>();
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_portfolio_post, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        PortfolioPost post = portfolioList.get(position);
        holder.descriptionText.setText(post.description);

        // Set up nested horizontal image gallery
        ImageGalleryAdapter imageAdapter = new ImageGalleryAdapter(context, post.imageUrls);
        holder.recyclerImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerImages.setAdapter(imageAdapter);
        holder.dateText.setText(post.date);
    }

    @Override
    public int getItemCount() {
        return portfolioList.size();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionText;
        RecyclerView recyclerImages;
        TextView dateText;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.tvDate);
            descriptionText = itemView.findViewById(R.id.tvDescription);
            recyclerImages = itemView.findViewById(R.id.recyclerImages);
        }
    }
}



