package pt.flora_on.homemluzula.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import pt.flora_on.homemluzula.R;
import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.TaxonObservation;

public class UpdateSpeciesViewHolder extends RecyclerView.ViewHolder {
    public final View main_view;
    public final View speciesname;
    public final View phenostate;
    public final View ordem;
    public final EditText abundance;

    public UpdateSpeciesViewHolder(@NonNull View itemView) {
        super(itemView);
        main_view = itemView;
        speciesname = itemView.findViewById(R.id.specbutlab);
        phenostate = itemView.findViewById(R.id.specphenostate);
        ordem = itemView.findViewById(R.id.ordem);
        abundance = itemView.findViewById(R.id.abundance);
    }

    public void setSpecies(TaxonObservation species, final int position, final SelectSpeciesListener selectSpeciesListener) {
        String text = String.format("%s%s%s", species.getTaxon(), species.getPhenoState().isFlowering() ? "#" : ""
                , species.getConfidence() == Constants.Confidence.CERTAIN ? "" : "?");
        ((TextView) speciesname).setText(text);
        speciesname.setTag(species);
/*
        if(species.getPhenoState() != Constants.PhenologicalState.NULL) {
            ((TextView) phenostate).setText(species.getPhenoState().getLabel());

        } else
            ((TextView) phenostate).setText("");
*/

        abundance.setText(species.getCover());

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaxonObservation to = (TaxonObservation) v.getTag();
                selectSpeciesListener.editSpecies(to, position);
                abundance.clearFocus();
            }
        };

        speciesname.setOnClickListener(clickListener);
//        ordem.setOnClickListener(selectSpeciesListener);

        abundance.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
//                    species.setAbundance(textView.getText().toString());
//                    selectSpeciesListener.commitSpecies(species, position);
                    textView.clearFocus();
                    return false;
                }
                return false;
            }
        });

        ((TextView) ordem).setText(String.format(Locale.ENGLISH, "%d", ((Integer) (position + 1))));
//        ((TextView) ordem).setText(String.format(Locale.ENGLISH, "%d", species.getOrder()));
    }

}
