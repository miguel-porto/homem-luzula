package pt.flora_on.observation_data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miguel on 02-10-2016.
 */

public class Taxon {
    private String genus, species;
    private List<String[]> infraranks = new ArrayList<String[]>();

    /**
     * Parses a full name into its parts
     * @param fullName
     */
    public Taxon(String fullName) throws InvalidParameterException {
        String[] tmpMap;
        String[] spl = fullName.split(" ");
        if(spl.length < 2) throw new InvalidParameterException("Invalid taxon name: " + fullName);
        this.genus = spl[0].toLowerCase();
        this.species = spl[1].toLowerCase();
        if(spl.length == 3) {
            tmpMap = new String[] {"subsp.", spl[2].toLowerCase()};
            this.infraranks.add(tmpMap);
        } else if(spl.length > 3) {
            for(int i=2; i<spl.length; i += 2) {
                if(i+1 >= spl.length) throw new InvalidParameterException("Invalid taxon name: " + fullName);
                tmpMap = new String[] {spl[i], spl[i+1].toLowerCase()};
                this.infraranks.add(tmpMap);
            }
        }
    }

    public Taxon(String genus, String species) {
        this.genus = genus;
        this.species = species;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.genus).append(" ").append(this.species);
        for(String[] s : this.infraranks) {
            sb.append(" ").append(s[0]).append(" ").append(s[1]);
        }
        return sb.toString();
    }

    public String getGenus() {
        return this.genus;
    }

    public String getSpecies() {
        return this.species;
    }

    public String getLastInfraname() {
        if(this.infraranks.size() > 0)
            return this.infraranks.get(this.infraranks.size()-1)[1];
        else
            return this.species;
    }
}
