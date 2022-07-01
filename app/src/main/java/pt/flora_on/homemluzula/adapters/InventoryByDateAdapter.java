package pt.flora_on.homemluzula.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import pt.flora_on.homemluzula.R;

/**
 * Created by miguel on 12-05-2018.
 */

public class InventoryByDateAdapter extends RecyclerView.Adapter<InventoryByDateAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView latitude;
        public final TextView longitude;
        public final TextView order;
        public final TextView speciessummary;

        public ViewHolder(View v) {
            super(v);
            latitude = (TextView) v.findViewById(R.id.latitude);
            longitude = (TextView) v.findViewById(R.id.longitude);
            order = (TextView) v.findViewById(R.id.ordem);
            speciessummary = (TextView) v.findViewById(R.id.speciessummary);
        }

    }
}
