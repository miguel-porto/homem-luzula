package pt.flora_on.observation_data;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pt.flora_on.homemluzula.activities.MainMap;

/**
 * Created by miguel on 04-10-2016.
 */

public class SpeciesList implements Parcelable {
    private Float latitude, longitude;
    private Integer precision = 10;   // meters
    private Integer year, month, day, hour, minute;
    private boolean complete = false;
    private Integer area;
    private Integer serialNumber;
    private String gpsCode;
    private String habitat, pubNotes, privNotes;
    private List<String> authors = new ArrayList<>();
    private ArrayList<TaxonObservation> taxa = new ArrayList<>();
    private UUID uuid;
    private boolean singleSpecies = false;
    private boolean hasTaxonCoordinates = true;
    private transient Integer maxOrder;

    public SpeciesList() {
        this.uuid = UUID.randomUUID();
    }

    public SpeciesList(SpeciesList sl) {
        this.latitude = sl.latitude;
        this.longitude = sl.longitude;
        this.precision = sl.precision;
        this.year = sl.year;
        this.month = sl.month;
        this.day = sl.day;
        this.hour = sl.hour;
        this.minute = sl.minute;
        this.complete = sl.complete;
        this.area = sl.area;
        this.serialNumber = sl.serialNumber;
        this.gpsCode = sl.gpsCode;
        this.habitat = sl.habitat;
        this.pubNotes = sl.pubNotes;
        this.privNotes = sl.privNotes;
        this.uuid = sl.uuid;
        this.authors.addAll(sl.authors);
        for(TaxonObservation to : sl.taxa)
            this.taxa.add(new TaxonObservation(to));
    }

    public Integer getDay() {
        return day;
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public Float getLatitude() { return latitude; }

    public Float getLongitude() {
        return longitude;
    }

    public Integer getSerialNumber() {
        return this.serialNumber;
    }

    public String getGpsCode() {
        return this.gpsCode == null ? "" : this.gpsCode;
    }

    public String getHabitat() {
        return this.habitat == null ? "" : this.habitat;
    }

    public void setGpsCode(String gpsCode) {
        this.gpsCode = gpsCode;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public UUID getUuid() { return uuid;}

    public boolean isSingleSpecies() {
        return singleSpecies;
    }

    public void setSingleSpecies(boolean singleSpecies) {
        this.singleSpecies = singleSpecies;
    }

    public boolean isHasTaxonCoordinates() {
        return hasTaxonCoordinates;
    }

    public void setHasTaxonCoordinates(boolean hasTaxonCoordinates) {
        this.hasTaxonCoordinates = hasTaxonCoordinates;
    }

    public void setYear(Integer year) {this.year = year;}
    public void setMonth(Integer month) {this.month = month;}
    public void setMinute(Integer minute) {this.minute = minute;}
    public void setDay(Integer day) {this.day = day;}
    public void setHour(Integer hour) {this.hour = hour;}

    private void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSerialNumber(Integer serialNumber, String prefix, int zeroPad) {
        this.serialNumber = serialNumber;
        if(zeroPad == 0)
            this.gpsCode = prefix + serialNumber;
        else
            this.gpsCode = String.format(Locale.getDefault(), "%s%0" + zeroPad + "d", prefix, serialNumber);
    }

    public void setNow() {
        Calendar cal = Calendar.getInstance();
        minute = cal.get(Calendar.MINUTE);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH) + 1;
        year = cal.get(Calendar.YEAR);
    }

    public void addObservation(TaxonObservation observation) {
        if(maxOrder == null)
            maxOrder = 0;

        observation.setOrder(maxOrder + 1);
        taxa.add(observation);
        maxOrder ++;
    }

    public int getNumberOfSpecies() {
        return taxa.size();
    }

    public ArrayList<TaxonObservation> getTaxa() {
        return taxa;
    }

    public void replaceTaxa(ArrayList<TaxonObservation> taxa) {
        this.taxa = taxa;
    }

    public SpeciesList(Parcel in) {
        String tmp;
        latitude = (tmp = in.readString()) == null ? null : Float.parseFloat(tmp);
        longitude = (tmp = in.readString()) == null ? null : Float.parseFloat(tmp);
        precision = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        year = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        month = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        day = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        hour = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        minute = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        serialNumber = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        gpsCode = in.readString();
        uuid = (tmp = in.readString()) == null ? null : UUID.fromString(tmp);
        complete = in.readByte() != 0;
        area = (tmp = in.readString()) == null ? null : Integer.parseInt(tmp);
        habitat = in.readString();
        pubNotes = in.readString();
        privNotes = in.readString();
        authors = in.createStringArrayList();
        taxa = in.createTypedArrayList(TaxonObservation.CREATOR);
        singleSpecies = in.readByte() != 0;
        hasTaxonCoordinates = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(latitude == null) dest.writeString(null); else dest.writeString(latitude.toString());
        if(longitude == null) dest.writeString(null); else dest.writeString(longitude.toString());
        if(precision == null) dest.writeString(null); else dest.writeString(precision.toString());
        if(year == null) dest.writeString(null); else dest.writeString(year.toString());
        if(month == null) dest.writeString(null); else dest.writeString(month.toString());
        if(day == null) dest.writeString(null); else dest.writeString(day.toString());
        if(hour == null) dest.writeString(null); else dest.writeString(hour.toString());
        if(minute == null) dest.writeString(null); else dest.writeString(minute.toString());
        if(serialNumber == null) dest.writeString(null); else dest.writeString(serialNumber.toString());
        dest.writeString(gpsCode);
        if(uuid == null) dest.writeString(null); else dest.writeString(uuid.toString());
        dest.writeByte((byte) (complete ? 1 : 0));
        if(area == null) dest.writeString(null); else dest.writeString(area.toString());
        dest.writeString(habitat);
        dest.writeString(pubNotes);
        dest.writeString(privNotes);
        dest.writeStringList(authors);
        dest.writeTypedList(taxa);
        dest.writeByte((byte) (singleSpecies ? 1 : 0));
        dest.writeByte((byte) (hasTaxonCoordinates ? 1 : 0));
    }

    public static final Creator<SpeciesList> CREATOR = new Creator<SpeciesList>() {
        @Override
        public SpeciesList createFromParcel(Parcel in) {
            return new SpeciesList(in);
        }

        @Override
        public SpeciesList[] newArray(int size) {
            return new SpeciesList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void setLocation(float lat, float lon) {
        latitude = lat;
        longitude = lon;
    }

    public void toCSV(PrintWriter file, String format) {
        StringBuilder sb = new StringBuilder();
        switch(format) {
            case "floraon":
                sb.append(year == null ? "" : year).append("\t")
                        .append(month == null ? "" : month).append("\t").append(day == null ? "" : day).append("\t")
                        .append(longitude).append("\t").append(latitude).append("\t\t");
                for(TaxonObservation obs : taxa) {
                    sb.append(obs.getTaxon());
                    if(obs.getConfidence() == Constants.Confidence.UNCERTAIN) sb.append("?");
                    if(obs.getPhenoState() == Constants.PhenologicalState.FLOWER
                            || obs.getPhenoState() == Constants.PhenologicalState.FLOWER_DISPERSION)
                        sb.append("#");
                    sb.append("+");
                }
                sb.append("\t\t0\t\t\t").append(this.getGpsCode());
                file.println(sb.toString());
                break;

            case "lvf":
                if(taxa.size() == 0) {
                    sb.append(this.getGpsCode()).append("\t")
                            .append(this.latitude).append("\t").append(this.longitude).append("\t")
/*
                            .append(this.day == null ? "?" : String.format("%02d", day)).append("/")
                            .append(this.month == null ? "?" : String.format("%02d", month)).append("/")
                            .append(this.year == null ? "?" : year).append("\t")
*/
                            .append(this.year == null ? "?" : year).append("/")
                            .append(this.month == null ? "?" : String.format("%02d", month)).append("/")
                            .append(this.day == null ? "?" : String.format("%02d", day)).append(" ")
                            .append(this.hour == null ? "?" : String.format("%02d", hour)).append(":")
                            .append(this.minute == null ? "?" : String.format("%02d", minute))
                            .append("\t").append(this.getHabitat());
                    file.println(sb.toString());
                    sb.setLength(0);
                } else {
                    for (TaxonObservation obs : taxa) {
                        sb.append(this.getGpsCode()).append("\t")
                                .append(this.latitude).append("\t").append(this.longitude).append("\t")
/*
                                .append(this.day == null ? "?" : String.format("%02d", day)).append("/")
                                .append(this.month == null ? "?" : String.format("%02d", month)).append("/")
                                .append(this.year == null ? "?" : year).append("\t")
*/
                                .append(this.year == null ? "?" : year).append("/")
                                .append(this.month == null ? "?" : String.format("%02d", month)).append("/")
                                .append(this.day == null ? "?" : String.format("%02d", day)).append(" ")
                                .append(this.hour == null ? "?" : String.format("%02d", hour)).append(":")
                                .append(this.minute == null ? "?" : String.format("%02d", minute))
                                .append("\t").append(this.getHabitat());
                        sb.append("\t").append(obs.getTaxon())
                                .append("\t").append(obs.getPhenoState().toString())
                                .append("\t").append(obs.getConfidence().equals(Constants.Confidence.CERTAIN) ? "CERTAIN" : "DOUBTFUL")
                                .append("\t").append(obs.getAbundance() == null ? "" : obs.getAbundance())
                                .append("\t").append(obs.getAbundanceType() == Constants.AbundanceType.NO_DATA ? "" : obs.getAbundanceType().toString())
                                .append("\t").append(obs.getCover() == null ? "" : obs.getCover())
                                .append("\t").append(obs.getComment() == null ? "" : obs.getComment())
                                .append("\t").append((obs.getObservationLatitude() == null || obs.getObservationLatitude() == 0) ? "" : obs.getObservationLatitude())
                                .append("\t").append((obs.getObservationLongitude() == null || obs.getObservationLongitude() == 0) ? "" : obs.getObservationLongitude());
                        file.println(sb.toString());
                        sb.setLength(0);
                    }
                }
                break;
        }

    }

    public SpannableStringBuilder concatSpecies(boolean abbreviate, Integer clipAfter) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        StringBuilder sb = new StringBuilder();
        List<AbbreviatedTaxonObservation> tos = new ArrayList<>();
        AbbreviatedTaxonObservation ato;

        for(TaxonObservation to : getTaxa())
            tos.add(new AbbreviatedTaxonObservation(to));
        Collections.sort(tos);
        int i;
        int nsp = getNumberOfSpecies();
        String tmp;
        for (i = 0; i < nsp && i < (clipAfter == null ? Integer.MAX_VALUE : clipAfter); i++) {
            sb.setLength(0);
            ato = tos.get(i);
            if (abbreviate) {
                sb.append(ato.toAbbreviatedString());
            } else {
                tmp = ato.getTaxon();
                sb.append(tmp.substring(0, 1).toUpperCase())
                        .append(tmp.substring(1, tmp.length()).toLowerCase());
            }
            boolean wasAppended = false;
            if (ato.getConfidence() == Constants.Confidence.UNCERTAIN) {
                sb.append("?");
                wasAppended = true;
            }
            SpannableString str = new SpannableString(sb.toString());
            if(ato.isAcquiringGPS != null && ato.isAcquiringGPS != 0)
                str.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (ato.getPhenoState().isFlowering()) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoFlower), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ato.getPhenoState() == Constants.PhenologicalState.VEGETATIVE) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoVegetative), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ato.getPhenoState() == Constants.PhenologicalState.DISPERSION || ato.getPhenoState() == Constants.PhenologicalState.FLOWER_DISPERSION) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoDispersion), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ato.getPhenoState() == Constants.PhenologicalState.FRUIT) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoFruit), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ato.getPhenoState() == Constants.PhenologicalState.RESTING) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoResting), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (ato.getPhenoState() == Constants.PhenologicalState.BUD) {
                str.setSpan(new ForegroundColorSpan(MainMap.phenoBud), 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.append(str);
            if(!wasAppended) ssb.append(" ");
            if(!(i == nsp - 1 || i == (clipAfter == null ? Integer.MAX_VALUE : (clipAfter - 1)))) ssb.append(" ");
        }
        if(clipAfter != null && i > clipAfter) ssb.append(" +").append(String.valueOf(nsp - clipAfter));

        return ssb;
    }
}
