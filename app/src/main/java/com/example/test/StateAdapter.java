package com.example.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class StateAdapter extends RecyclerView.Adapter<StateAdapter.MyViewHolder> {
    private Context mConext;
    private List<StateDat> stateDatList;

    public StateAdapter(Context mConext,List<StateDat> stateDatList)
    {
        this.mConext=mConext;
        this.stateDatList=stateDatList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_fragment_state,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        StateDat stateDat = stateDatList.get(position);
        holder.countryName.setText(stateDat.getCountryName());
        holder.active.setText(String.valueOf(stateDat.getActive()));
        holder.confirmed.setText(String.valueOf(stateDat.getConfirmed()));
        holder.recovered.setText(String.valueOf(stateDat.getRecovered()));
        holder.deaths.setText(String.valueOf(stateDat.getDeaths()));
    }

    @Override
    public int getItemCount() {
        return stateDatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MaterialTextView countryName,active,confirmed,deaths,recovered;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName=itemView.findViewById(R.id.countryName);
            active=itemView.findViewById(R.id.sactive);
            confirmed=itemView.findViewById(R.id.sconfirmed);
            deaths=itemView.findViewById(R.id.sdeaths);
            recovered=itemView.findViewById(R.id.srecovered);
        }
    }
}
