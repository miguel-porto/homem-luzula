package pt.flora_on.homemluzula.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import pt.flora_on.homemluzula.R;
import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.TaxonObservation;

public class ObservationAdapterAbundance extends RecyclerView.Adapter<UpdateSpeciesViewHolder> {
    private SpeciesList mDataset;
    private SelectSpeciesListener mOnClick;
    private RecyclerView mRecyclerView;

    public ObservationAdapterAbundance(SpeciesList speciesList, SelectSpeciesListener speciesListener) {
        mDataset = speciesList;
        mOnClick = speciesListener;
    }

    public void replaceData(SpeciesList newDataset) {
        mDataset = newDataset;
    }

    @NonNull
    @Override
    public UpdateSpeciesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.species_button_compact, viewGroup, false);
        return new UpdateSpeciesViewHolder(v);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateSpeciesViewHolder viewHolder, final int position) {
        TaxonObservation species = mDataset.getTaxa().get(position);
        viewHolder.setSpecies(species, position, mOnClick);
        viewHolder.abundance.setOnFocusChangeListener((view, b) -> {
            if(!b && view != null) {
                species.setAbundance(((EditText) view).getText().toString());
                mOnClick.commitSpecies(species, position);
            } else if(b) {
                LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                if(llm != null) {
//                    Log.e("LAST", String.format("%d %d", llm.findLastVisibleItemPosition(), position));
                    if(llm.findLastVisibleItemPosition() - position <= 2) { // scroll if arriving at the bottom
                        int end = llm.findLastVisibleItemPosition() + 2;
                        if(end > this.getItemCount() - 1) end = this.getItemCount() - 1;
                        mRecyclerView.scrollToPosition(end);
                    }
                }

                ((EditText) view).setImeOptions(position == (this.getItemCount() - 1) ?
                        EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.getNumberOfSpecies();
    }
}
