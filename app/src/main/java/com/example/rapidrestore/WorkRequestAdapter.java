package com.example.rapidrestore;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkRequestAdapter extends RecyclerView.Adapter<WorkRequestAdapter.ProvRequestViewHolder> {

    private static List<ProvRequest> userList;

    public static class ProvRequestViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, dateText;

        public ProvRequestViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            dateText = itemView.findViewById(R.id.date_text);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ProvRequest clickedUser = userList.get(position);
                    Intent intent = new Intent(itemView.getContext(), WorkRequestView.class);
                    intent.putExtra("userId", clickedUser.getId()); // or document ID
                    Toast.makeText(itemView.getContext(),  clickedUser.getId(), Toast.LENGTH_SHORT).show();
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    public WorkRequestAdapter(List<ProvRequest> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ProvRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.prov_request_item, parent, false);
        return new ProvRequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvRequestViewHolder holder, int position) {
        ProvRequest currentProvRequest = userList.get(position);
        holder.nameText.setText(currentProvRequest.getName());
        holder.dateText.setText("Date submitted: " + currentProvRequest.getDateSubmitted());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

