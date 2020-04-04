package pt.flora_on.observation_data;

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 02-10-2016.
 */

public class Taxon {
    private String genus, species, sensu;
    private List<String[]> infraranks = new ArrayList<String[]>();
    private transient static Pattern taxonNamePattern = Pattern.compile(
            "^ *(?<genus>[a-zçA-Z]+)(?: +(?<species>[a-zç-]+))" +
                    "(?<infras>(?: +((subsp)|(ssp)|(var)|(f)|(forma))\\.? +[a-z-]+)+)?" +
                    "(?: +sensu +(?<sensu>[^\\[\\]]+))? *$");

    private transient static Pattern infraTaxa = Pattern.compile(" *(?:(?<rank>subsp|var|f|ssp|subvar|forma)\\.? +)?(?<infra>[a-zç-]+)");
    /**
     * Parses a full name into its parts
     * @param fullName
     */
/*
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
*/

    /**
     * Parses a full name into its parts
     * @param fullName
     */
    public Taxon(String fullName) throws InvalidParameterException {
        Matcher m = taxonNamePattern.matcher(fullName);
        String[] tmpMap;

        if(m.find()) {
            this.genus = m.group(1).toLowerCase();
            this.species = m.group(2);
            this.sensu = m.group(10);
            String infras = m.group(3);
            if(infras != null) {
                Matcher m1 = infraTaxa.matcher(infras);
                while(m1.find()) {
                    tmpMap = new String[] {m1.group(1) + ".", m1.group(2).toLowerCase()};
                    this.infraranks.add(tmpMap);
                }
            }
        } else throw new InvalidParameterException("Invalid taxon name: " + fullName);
    }

    public Taxon(String genus, String species) throws InvalidParameterException {
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
//        if(this.sensu != null) sb.append(" sensu ").append(this.sensu);
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

    public boolean hasInfraTaxa() {
        return this.infraranks.size() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Taxon taxon = (Taxon) o;
        return genus.equals(taxon.genus) &&
                species.equals(taxon.species) &&
                infraranks.equals(taxon.infraranks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genus, species, infraranks);
    }

    /**
     * Gets the Taxon without infraranks
     * @return
     */
    public Taxon getSpeciesTaxon() {
        return(new Taxon(this.genus, this.species));
    }
}
