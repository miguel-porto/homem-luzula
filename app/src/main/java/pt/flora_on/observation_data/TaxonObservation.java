package pt.flora_on.observation_data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by miguel on 04-10-2016.
 */

public class TaxonObservation implements Parcelable, Comparable<TaxonObservation> {
    private final String taxon;
    private String comment;
    private String abundance;
    private Constants.PhenologicalState phenoState;
    private Constants.NaturalizationState naturalizationState = Constants.NaturalizationState.WILD;
    private Constants.Confidence confidence = Constants.Confidence.CERTAIN;
    private Constants.AbundanceType abundanceType = Constants.AbundanceType.NO_DATA;
    private Integer order;
    private String cover;

    public TaxonObservation(String taxon, Constants.PhenologicalState phenoState) {
        this.taxon = taxon;
        this.phenoState = phenoState;
    }

    public TaxonObservation(String taxon, Constants.PhenologicalState phenoState, boolean doubt) {
        this.taxon = taxon;
        this.phenoState = phenoState;
        this.confidence = doubt ? Constants.Confidence.UNCERTAIN : Constants.Confidence.CERTAIN;
    }

    public TaxonObservation(TaxonObservation to) {
        this.taxon = to.taxon;
        this.comment = to.comment;
        this.abundance = to.abundance;
        this.phenoState = to.phenoState;
        this.naturalizationState = to.naturalizationState;
        this.confidence = to.confidence;
        this.abundanceType = to.abundanceType;
        this.cover = to.cover;
    }

    private TaxonObservation(Parcel in) {
        taxon = in.readString();
        comment = in.readString();
        abundance = in.readString();
        phenoState = Constants.PhenologicalState.valueOf(in.readString());
        naturalizationState = Constants.NaturalizationState.valueOf(in.readString());
        confidence = Constants.Confidence.valueOf(in.readString());
        abundanceType = Constants.AbundanceType.valueOf(in.readString());
        order = in.readInt();
        cover = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taxon);
        dest.writeString(comment);
        dest.writeString(abundance);
        dest.writeString(phenoState == null ? Constants.PhenologicalState.NULL.toString() : phenoState.toString());
        dest.writeString(naturalizationState.toString());
        dest.writeString(confidence.toString());
        dest.writeString(abundanceType == null ? Constants.AbundanceType.NO_DATA.toString() : abundanceType.toString());
        dest.writeInt(this.getOrder());
        dest.writeString(cover);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setConfidence(Constants.Confidence conf) {
        confidence = (conf == null ? Constants.Confidence.CERTAIN : conf);
    }

    public void setPhenoState(Constants.PhenologicalState ps) {
        phenoState = (ps == null ? Constants.PhenologicalState.NULL : ps);
    }

    public void setNaturalizationState(Constants.NaturalizationState ns) {
        naturalizationState = (ns == null ? Constants.NaturalizationState.WILD : ns);
    }

    public void setAbundanceType(Constants.AbundanceType at) {
        abundanceType = (at == null ? Constants.AbundanceType.NO_DATA : at);
    }

    public void setComment(String comment) {
        if(comment != null)
            this.comment = (comment.trim().length() == 0 ? null : comment.trim());
        else
            this.comment = null;
    }

    public void setAbundance(String abundance) {
        if(abundance != null)
            this.abundance = (abundance.trim().length() == 0 ? null : abundance.trim());
        else
            this.abundance = null;
    }

    public void setCover(String cover) {
        if(cover != null)
            this.cover = (cover.trim().length() == 0 ? null : cover.trim());
        else
            this.cover = null;
    }

    public static final Creator<TaxonObservation> CREATOR = new Creator<TaxonObservation>() {
        @Override
        public TaxonObservation createFromParcel(Parcel in) {
            return new TaxonObservation(in);
        }

        @Override
        public TaxonObservation[] newArray(int size) {
            return new TaxonObservation[size];
        }
    };

    public String getTaxonCapital() { return (getTaxon().substring(0, 1)).toUpperCase() + getTaxon().substring(1, getTaxon().length()).toLowerCase();}
    public String getTaxon() { return taxon; }

    public String getComment() {
        return comment;
    }

    public String getAbundance() {
        return abundance;
    }

    public String getCover() {
        return cover;
    }

    public Constants.NaturalizationState getNaturalizationState() {
        return naturalizationState;
    }

    public Constants.Confidence getConfidence() {
        return confidence;
    }

    public Constants.AbundanceType getAbundanceType() {
        return abundanceType == null ? Constants.AbundanceType.NO_DATA : abundanceType;
    }

    public Constants.PhenologicalState getPhenoState() { return phenoState == null ? Constants.PhenologicalState.NULL : phenoState;}

    public Integer getOrder() {
        return order == null ? 1 : order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int compareTo(TaxonObservation o) {
        return this.compareKey().compareTo(o.compareKey());
    }

    String compareKey() {
        return(this.getTaxon().toLowerCase());
    }
}
