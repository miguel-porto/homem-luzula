package pt.flora_on.homemluzula.adapters;

import pt.flora_on.observation_data.TaxonObservation;

public interface SelectSpeciesListener {
    void editSpecies(TaxonObservation obs, int position);
    void commitSpecies(TaxonObservation obs, int position);
}
