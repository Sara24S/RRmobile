package com.example.rapidrestore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProductViewHolder> {


    //this context we will use to inflate the layout
    private String homeownerId;
    private Context mCtx;

    //we are storing all the products in a list
    private List<Provider> providerList;

    //getting the context and product list with constructor
    public ProviderAdapter(Context mCtx, List<Provider> providerList, String homeownerId) {
        this.mCtx = mCtx;
        this.providerList = providerList;
        this.homeownerId = homeownerId;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.provider_list_layout, null);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        //getting the provider of the specified position
        Provider provider = providerList.get(position);
        if (provider == null) return;

        //binding the data with the viewholder views
        holder.textViewName.setText(provider.getName());
        holder.textViewProfession.setText(provider.getProfession());
        holder.textViewRating.setText(String.valueOf(provider.getRating()));
        holder.textViewPrice.setText(String.valueOf(provider.getPrice()));
        holder.textViewRegion.setText(provider.getRegion());
//loading the image
        Glide.with(mCtx).load(provider.getImage()).into(holder.imageView);

    }


    @Override
    public int getItemCount() {
        return providerList.size();
    }


    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewProfession, textViewRating, textViewPrice, textViewRegion;
        ImageView imageView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textViewName);
            textViewProfession = itemView.findViewById(R.id.textViewProfession);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewRegion = itemView.findViewById(R.id.textViewRegion);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Provider clickedUser = providerList.get(position);

                    Intent intent = new Intent(mCtx, RepairRequestForm.class);//temp
                    intent.putExtra("providerId", clickedUser.getId()); // or document ID
                    intent.putExtra("homeownerId", homeownerId);
                    mCtx.startActivity(intent);
                }
            });
        }
    }
}

