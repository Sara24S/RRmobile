package com.example.rapidrestore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.List;

public class RepairRequestAdapter extends RecyclerView.Adapter<RepairRequestAdapter.ViewHolder> {

    private Context context;
    private List<RepairRequest> requests;

    public RepairRequestAdapter(Context context, List<RepairRequest> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_repair_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepairRequest request = requests.get(position);
        holder.textName.setText(request.getName());
        holder.textState.setText("State: " + request.getState());
        //holder.textDate.setText("Date: " + DateFormat.getDateTimeInstance().format(request.getTimestamp()));
        holder.textDate.setText(request.getDateTime());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            intent.putExtra("requestId", request.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDate, textState;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textDate = itemView.findViewById(R.id.textDate);
            textState = itemView.findViewById(R.id.textState);
        }
    }
}


