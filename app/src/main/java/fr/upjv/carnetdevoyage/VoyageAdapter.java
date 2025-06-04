package fr.upjv.carnetdevoyage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter <VoyageViewHolder> {

    private List<Voyage> lesVoyages;

    public VoyageAdapter(List<Voyage> lesVoyages){
        this.lesVoyages=lesVoyages;
    }

    public List<Voyage> getLesVoyages() {
        return lesVoyages;
    }

    public void setLesVoyages(List<Voyage> lesVoyages) {
        this.lesVoyages = lesVoyages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VoyageViewHolder leVoyageViewHolder;
        LayoutInflater monLayoutInflater;
        monLayoutInflater=LayoutInflater.from(parent.getContext());
        View view=monLayoutInflater.inflate(R.layout.layout_voyage_item,parent,false);
        leVoyageViewHolder= new VoyageViewHolder(view);
        return leVoyageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        holder.mettreAjourLigne(lesVoyages.get(position));

    }

    @Override
    public int getItemCount() {
        return lesVoyages.size();
    }
}
