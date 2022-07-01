package pt.flora_on.homemluzula.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import pt.flora_on.homemluzula.ObservationDetails;
import pt.flora_on.homemluzula.R;
import pt.flora_on.homemluzula.SpeciesChooser;
import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.TaxonObservation;

/**
 * The list item for selecting new species into the inventory, with shortcuts to phenology
 * Created by miguel on 06-10-2016.
 */

public class ObservationAdapterPhenology extends RecyclerView.Adapter<AddSpeciesViewHolder> {
//    private SpeciesList mDataset;
    private final String[] mNames;
    private SelectSpeciesListener mOnClick;
    private final int mLayoutResource = R.layout.species_button;
    private Activity mParentActivity;

/*
    public ObservationAdapterPhenology(int layoutResource, SpeciesList myDataset) {
        mDataset = myDataset;
        mNames = null;
        mLayoutResource = layoutResource;
    }

    public ObservationAdapterPhenology(int layoutResource, SpeciesList myDataset, SelectSpeciesListener onClick) {
        mDataset = myDataset;
        mNames = null;
        mLayoutResource = layoutResource;
        mOnClick = onClick;
    }
*/

    public ObservationAdapterPhenology(String[] myDataset, SelectSpeciesListener onClick, Activity activity) {
//        mDataset = null;
        mNames = myDataset;
        mOnClick = onClick;
        mParentActivity = activity;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public AddSpeciesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResource, parent, false);
        return new AddSpeciesViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AddSpeciesViewHolder holder, int position) {
        final int thisPosition = position;
        String text;
        text = mNames[position];

        holder.setSpeciesName((text.substring(0, 1)).toUpperCase() + text.substring(1).toLowerCase());

        holder.setSelectListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaxonObservation to = (TaxonObservation) v.getTag();
                to.setConfidence(holder.doubt.isChecked() ? Constants.Confidence.UNCERTAIN : Constants.Confidence.CERTAIN);
                mOnClick.editSpecies(to, thisPosition);
            }
        });

        // more fields
        holder.but_details.setTag(new TaxonObservation(TaxonObservation.capitalize(text), Constants.PhenologicalState.NULL));
        holder.but_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ObservationDetails.class);
                TaxonObservation to = (TaxonObservation) v.getTag();
                intent.putExtra("taxon", to);

                mParentActivity.startActivityForResult(intent, SpeciesChooser.GET_OBSERVATION);
            }
        });

/*
        if(mDataset != null) {
            View selectedButton = null;
            holder.clearSelectedButtons();
            switch (mDataset.getTaxa().get(position).getPhenoState()) {
                case FLOWER:
                    selectedButton = holder.but_flor;
                    break;
                case VEGETATIVE:
                    selectedButton = holder.but_veget;
                    break;
                case DISPERSION:
                    selectedButton = holder.but_dispers;
                    break;
                case FLOWER_DISPERSION:
                    selectedButton = holder.but_flordispers;
                    break;
*/
/*
                case RESTING:
                    selectedButton = holder.but_dorm;
                    break;
*//*

            }
            //if(selectedButton != null) selectedButton.setBackgroundResource(R.drawable.roundrect);
            if(selectedButton != null) ((RadioButton) selectedButton).setChecked(true);
        }
*/
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mNames.length;
    }
}
