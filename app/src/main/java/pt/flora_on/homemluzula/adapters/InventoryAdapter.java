package pt.flora_on.homemluzula.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import pt.flora_on.homemluzula.R;
import pt.flora_on.homemluzula.RecyclerViewItemListener;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;

/**
 * Created by miguel on 19-10-2016.
 */

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
    private final Inventories mData;
    private final Context mContext;
    private final RecyclerViewItemListener mOnClick;
    private final String[] Months = new String[] {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    public InventoryAdapter(Context context, Inventories data, RecyclerViewItemListener clickListener) {
        mContext = context;
        mData = data;
        mOnClick = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.inventory_view, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int maxspp = 10;
        final int thisPosition = position;
        SpeciesList sl = mData.getSpeciesList(position);

        if(mOnClick != null) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClick.onClick(v, thisPosition);
                }
            };

            holder.itemView.setOnClickListener(clickListener);
        }

        holder.order.setText("".equals(sl.getGpsCode()) ? "*" : sl.getGpsCode());
        if(sl.getLatitude() == null || sl.getLongitude() == null)
            holder.order.setBackgroundResource(R.color.colorPrimary);
        else
            holder.order.setBackgroundResource(R.color.colorAccent);

        if(sl.getDay() == null && sl.getMonth() == null && sl.getYear() == null)
            holder.latitude.setText("sem data");
        else {
            if(sl.getDay() == null && sl.getMonth() != null)
                holder.latitude.setText(String.format(Locale.getDefault(), "%s", Months[sl.getMonth() - 1]));
            else {
                if(sl.getDay() != null && sl.getMonth() == null)
                    holder.latitude.setText(String.format(Locale.getDefault(), "%d/-", sl.getDay()));
                else
                    holder.latitude.setText(String.format(Locale.getDefault(), "%d/%s", sl.getDay(), Months[sl.getMonth() - 1]));
            }

            if(sl.getYear() == null)
                holder.longitude.setText("-");
            else
                holder.longitude.setText(String.format(Locale.getDefault(), "%d", sl.getYear()));
        }

        holder.speciessummary.setText(sl.concatSpecies(true, maxspp));
    }

    @Override
    public int getItemCount() {
        return mData.size();
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
