package pt.flora_on.observation_data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import pt.flora_on.homemluzula.Checklist;
import pt.flora_on.homemluzula.MainMap;

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

    public void setGpsCode(String gpsCode) {
        this.gpsCode = gpsCode;
    }

    public UUID getUuid() { return uuid;}

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
                for(TaxonObservation obs : taxa) {
                    sb.append(this.getGpsCode()).append("\t")
                            .append(this.latitude).append("\t").append(this.longitude).append("\t").append(this.day)
                            .append("/").append(this.month).append("/").append(this.year).append("\t")
                            .append(obs.getTaxon()).append("\t").append(obs.getPhenoState().toString())
                            .append("\t").append(obs.getConfidence().equals(Constants.Confidence.CERTAIN) ? "CERTAIN" : "DOUBTFUL")
                            .append("\t").append(obs.getAbundance() == null ? "" : obs.getAbundance())
                            .append("\t").append(obs.getAbundanceType() == Constants.AbundanceType.NO_DATA ? "" : obs.getAbundanceType().toString())
                            .append("\t").append(obs.getComment() == null ? "" : obs.getComment());
                    file.println(sb.toString());
                    sb.setLength(0);
                }
                break;
        }

    }

    public String concatSpecies(boolean abbreviate, Integer clipAfter) {
        StringBuilder sb = new StringBuilder();
        List<TaxonObservation> tos = getTaxa();
        int i;
        int nsp = getNumberOfSpecies();
        String tmp;
        for (i = 0; i < nsp && i < (clipAfter == null ? Integer.MAX_VALUE : clipAfter); i++) {
            if(abbreviate) {
                try {
                    tmp = Checklist.abbreviateTaxon(new Taxon(tos.get(i).getTaxon()), MainMap.checklist.getNFirst(), MainMap.checklist.getNLast());
                } catch(InvalidParameterException e) {
                    tmp = tos.get(i).getTaxon();
                }
                sb.append(tmp.substring(0, 1).toUpperCase())
                        .append(tmp.substring(1, MainMap.checklist.getNFirst() + MainMap.checklist.getNLast()).toLowerCase());
            } else {
                tmp = tos.get(i).getTaxon();
                sb.append(tmp.substring(0, 1).toUpperCase())
                        .append(tmp.substring(1, tmp.length()).toLowerCase());
            }
            if(tos.get(i).getPhenoState() == Constants.PhenologicalState.FLOWER
                    || tos.get(i).getPhenoState() == Constants.PhenologicalState.FLOWER_DISPERSION) sb.append("#");
            if(tos.get(i).getConfidence() == Constants.Confidence.UNCERTAIN) sb.append("?");
            if(!(i == nsp - 1 || i == (clipAfter == null ? Integer.MAX_VALUE : (clipAfter - 1)))) sb.append(", ");
        }
        if(clipAfter != null && i > clipAfter) sb.append(", +").append(nsp - clipAfter);

        return sb.toString();
    }
}
