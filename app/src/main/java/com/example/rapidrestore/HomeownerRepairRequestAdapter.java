package com.example.rapidrestore;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class HomeownerRepairRequestAdapter extends RecyclerView.Adapter<HomeownerRepairRequestAdapter.ViewHolder> {
    private static List<HomeownerRepairRequest> homeownerRepairRequest;

    public HomeownerRepairRequestAdapter(List<HomeownerRepairRequest> repairRequests) {
        this.homeownerRepairRequest = repairRequests;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProvider, textDate, textStatus, textLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            textProvider = itemView.findViewById(R.id.textProviderName);
            textDate = itemView.findViewById(R.id.textDate);
            textStatus = itemView.findViewById(R.id.textStatus);
            textLocation = itemView.findViewById(R.id.textLocation);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    HomeownerRepairRequest clickedUser = homeownerRepairRequest.get(position);
                    Intent intent = new Intent(itemView.getContext(), RequestDetailsActivity.class);
                    intent.putExtra("requestId", clickedUser.getId()); // or document ID
                    intent.putExtra("isHomeowner", true);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_homeowner_repair_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HomeownerRepairRequest request = homeownerRepairRequest.get(position);
        holder.textProvider.setText("Provider: " + request.getProviderName());
        holder.textDate.setText("Date: " + request.getDate());
        holder.textStatus.setText("Status: " + request.getStatus());
        holder.textLocation.setText("Location: " + request.getLocation());
    }

    @Override
    public int getItemCount() {
        return homeownerRepairRequest.size();
    }
}
