package pt.flora_on.observation_data;

import android.os.Parcelable;

import java.security.InvalidParameterException;

import pt.flora_on.homemluzula.Checklist;
import pt.flora_on.homemluzula.MainMap;

/**
 * The same as TaxonObservation except that it sorts based on abbreviations, not full names
 */
public class AbbreviatedTaxonObservation extends TaxonObservation implements Parcelable {
    public AbbreviatedTaxonObservation(String taxon, Constants.PhenologicalState phenoState) {
        super(taxon, phenoState);
    }

    public AbbreviatedTaxonObservation(String taxon, Constants.PhenologicalState phenoState, boolean doubt) {
        super(taxon, phenoState, doubt);
    }

    public AbbreviatedTaxonObservation(TaxonObservation to) {
        super(to);
    }

    public String toAbbreviatedString() {
        String tmp;
        try {
            tmp = Checklist.abbreviateTaxon(new Taxon(getTaxon()), MainMap.checklist.getNFirst(), MainMap.checklist.getNLast());
        } catch (InvalidParameterException e) {
            tmp = getTaxon();
        }
        int howmany = Math.min(MainMap.checklist.getNFirst() + MainMap.checklist.getNLast(), tmp.length());
        tmp = tmp.substring(0, 1).toUpperCase() + tmp.substring(1, howmany).toLowerCase();
        return(tmp);
    }

    @Override
    String compareKey() {
        return(this.toAbbreviatedString().toLowerCase());
    }
}
