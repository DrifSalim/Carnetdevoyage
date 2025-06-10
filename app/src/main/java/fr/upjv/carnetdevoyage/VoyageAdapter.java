package fr.upjv.carnetdevoyage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VoyageAdapter extends RecyclerView.Adapter<VoyageViewHolder> {

    private List<Voyage> lesVoyages;

    // interface pour gérer le clic
    public interface OnItemClickListener {
        void onItemClick(Voyage voyage);
    }

    private OnItemClickListener listener;

    // *définir le listener depuis l'activité
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public VoyageAdapter(List<Voyage> lesVoyages) {
        this.lesVoyages = lesVoyages;
    }

    public List<Voyage> getLesVoyages() {
        return lesVoyages;
    }

    public void setLesVoyages(List<Voyage> lesVoyages) {
        this.lesVoyages = lesVoyages;
        notifyDataSetChanged(); // recharger la liste
    }

    @NonNull
    @Override
    public VoyageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater monLayoutInflater = LayoutInflater.from(parent.getContext());
        View view = monLayoutInflater.inflate(R.layout.layout_voyage_item, parent, false);
        return new VoyageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoyageViewHolder holder, int position) {
        Voyage voyage = lesVoyages.get(position);
        holder.mettreAjourLigne(voyage);

        // Clique sur l'élément
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(voyage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lesVoyages.size();
    }
}
