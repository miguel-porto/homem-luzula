package pt.flora_on.homemluzula.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pt.flora_on.homemluzula.R;
import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.TaxonObservation;

public class AddSpeciesViewHolder extends RecyclerView.ViewHolder {
    public final View main_view;
    public final View speciesname;
    public final View speciessensu;
    public final View but_flor;
    public final View but_veget;
    public final View but_dispers;
    public final View but_flordispers;
    public final View but_dorm;
    public final View but_details;
    public final View but_imatfruit;
    public final View but_bud;
    public final CheckBox doubt;

    public void clearSelectedButtons() {
        if(but_flor != null) but_flor.setBackgroundResource(0);
        if(but_veget != null) but_veget.setBackgroundResource(0);
        if(but_dorm != null) but_dorm.setBackgroundResource(0);
        if(but_dispers != null) but_dispers.setBackgroundResource(0);
        if(but_imatfruit != null) but_imatfruit.setBackgroundResource(0);
        if(but_bud != null) but_bud.setBackgroundResource(0);
        if(but_flordispers != null) but_flordispers.setBackgroundResource(0);
    }

    public AddSpeciesViewHolder(@NonNull View itemView) {
        super(itemView);
        main_view = itemView;
        speciesname = itemView.findViewById(R.id.specbutlab);
        speciessensu = itemView.findViewById(R.id.specbutsensu);
        but_flor = itemView.findViewById(R.id.but_flor);
        but_dorm = itemView.findViewById(R.id.but_dorm);
        but_veget = itemView.findViewById(R.id.but_veget);
        but_dispers = itemView.findViewById(R.id.but_dispers);
        but_imatfruit = itemView.findViewById(R.id.but_imatfruit);
        but_bud = itemView.findViewById(R.id.but_bud);
        but_flordispers = itemView.findViewById(R.id.but_flordisp);
        but_details = itemView.findViewById(R.id.but_details);
        doubt = (CheckBox) itemView.findViewById(R.id.check_doubt);
    }


    public void setSelectListeners(View.OnClickListener clickListener) {
        speciesname.setOnClickListener(clickListener);
//        if(phenostate != null) phenostate.setOnClickListener(clickListener);

        if(but_flor != null) {
            but_flor.setOnClickListener(clickListener);
            but_dorm.setOnClickListener(clickListener);
            but_veget.setOnClickListener(clickListener);
            but_dispers.setOnClickListener(clickListener);
            but_imatfruit.setOnClickListener(clickListener);
            but_bud.setOnClickListener(clickListener);
            but_flordispers.setOnClickListener(clickListener);
        }
    }

    public void setSpeciesName(String name) {
        String[] sensu = name.split("sensu");
        if(sensu.length > 1) {
            name = sensu[0];
            ((TextView) speciessensu).setText("sensu " + sensu[1]);
            speciessensu.setVisibility(View.VISIBLE);
        } else
            speciessensu.setVisibility(View.GONE);
        ((TextView) speciesname).setText(name);
        speciesname.setTag(new TaxonObservation(name, Constants.PhenologicalState.NULL));
        but_flor.setTag(new TaxonObservation(name, Constants.PhenologicalState.FLOWER));
        but_dorm.setTag(new TaxonObservation(name, Constants.PhenologicalState.RESTING));
        but_veget.setTag(new TaxonObservation(name, Constants.PhenologicalState.VEGETATIVE));
        but_dispers.setTag(new TaxonObservation(name, Constants.PhenologicalState.DISPERSION));
        but_imatfruit.setTag(new TaxonObservation(name, Constants.PhenologicalState.FRUIT));
        but_bud.setTag(new TaxonObservation(name, Constants.PhenologicalState.BUD));
        but_flordispers.setTag(new TaxonObservation(name, Constants.PhenologicalState.FLOWER_DISPERSION));
    }
}
