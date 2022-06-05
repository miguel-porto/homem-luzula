package pt.flora_on.homemluzula;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.flora_on.observation_data.Taxon;

/**
 * Created by miguel on 02-10-2016.
 */

public class Checklist {
    private final Map<Taxon, Integer> checklist = new LinkedHashMap<Taxon, Integer>();
    private final List<String> errors = new ArrayList<>();
    /**
     * nFirst: number of letter of the genus to be input
     * nLast: number of letters of the last infrarank to be input
     */
    private int nFirst = 1, nLast = 3;

    public Checklist(File checklistFile) throws IOException {
        this(new FileInputStream(checklistFile));
    }

    public Checklist(InputStream checklistFile) throws IOException, InvalidParameterException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(checklistFile));
        Taxon taxon;
        Set<Taxon> subsp = new HashSet<>();
        List<Taxon> toAdd = new ArrayList<>();

        while((line = br.readLine()) != null) {
            if(line.trim().length() == 0) continue;
            try {
                this.checklist.put(taxon = new Taxon(line), 0);
                if(taxon.hasInfraTaxa()) {
                    if(subsp.contains(taxon.getSpeciesTaxon())) // only add species if there is more than one infrataxon
                        toAdd.add(taxon.getSpeciesTaxon());
                    else
                        subsp.add(taxon.getSpeciesTaxon());
                }
                for(Taxon t : toAdd)
                    this.checklist.put(t, 0);
            } catch (InvalidParameterException e) {
                Log.w("CHK", e.getMessage());
                errors.add(line);
            }
        }
        br.close();
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public int size() {
        return this.checklist.size();
    }

    public Taxon getTaxon(int i) {
        int j = 0;
        for(Taxon t : checklist.keySet()) {
            if(i == j) return t;
            j ++;
        }
        return null;
    }

    public int getNFilterCharacters() {
        return this.nFirst + this.nLast;
    }

    public String[] getTaxonNameFromAbbreviation(String abbrev) {
        List<String> out = new ArrayList<>();
        for(Taxon t : getTaxonFromAbbreviation(abbrev)) {
            out.add(t.toString());
        }
        return out.toArray(new String[out.size()]);
    }

    public static String[] getTaxonNameFromTaxonList(Taxon[] taxonList) {
        List<String> out = new ArrayList<>();
        for(Taxon t : taxonList) {
            out.add(t.toString());
        }
        return out.toArray(new String[out.size()]);
    }

    /**
     * Returns the genera starting with prefix
     * @param prefix
     * @return
     */
    public String[] getGeneraFromPrefix(String prefix) {
        final Set<String> possibleGenera = new HashSet<>();
        final Iterator<Taxon> it = checklist.keySet().iterator();
        prefix = prefix.toLowerCase();
        String gen;
        while (it.hasNext()) {
            gen = it.next().getGenus().toLowerCase();
            if(gen.startsWith(prefix)) possibleGenera.add(gen);
        }
        return possibleGenera.toArray(new String[possibleGenera.size()]);
    }

    public Taxon[] getTaxaFromGenus(String genusPrefix) {
        Map<Taxon, Integer> tmpList = new LinkedHashMap<>();
        Taxon tax;
        Iterator<Taxon> it = checklist.keySet().iterator();
        while(it.hasNext()) {
            tax = it.next();
            if(tax.getGenus().toLowerCase().startsWith(genusPrefix))
                tmpList.put(tax, checklist.get(tax));
        }

        List<Taxon> out = new ArrayList<>();
        for(Map.Entry<Taxon, Integer> e : tmpList.entrySet()) {
            out.add(e.getKey());
        }
        return out.toArray(new Taxon[out.size()]);
    }

    public Taxon[] getTaxonFromAbbreviation(String abbrev) {
        Map<Taxon, Integer> tmpList = new LinkedHashMap<>(checklist);

        if(abbrev != null && abbrev.length() == 0) return tmpList.keySet().toArray(new Taxon[tmpList.size()]);

        Iterator<Taxon> it = tmpList.keySet().iterator();
        Taxon tax;
        final String first = abbrev.toLowerCase().substring(0, Math.min(this.nFirst, abbrev.length()));

        final String last = this.nFirst > abbrev.length() ? null : abbrev.toLowerCase().substring(this.nFirst, Math.min(this.nLast + this.nFirst, abbrev.length()));
        // first remove those that don't match genus or last infra name
        while(it.hasNext()) {
            tax = it.next();
            if(!tax.getGenus().toLowerCase().startsWith(first)) {
                it.remove();
                continue;
            }
            if(last != null && !tax.getSpecies().toLowerCase().startsWith(last)
                    && !tax.getLastInfraname().toLowerCase().startsWith(last))
                it.remove();
        }

        // now remove those that don't match specific epithet
/*        it = tmpList.keySet().iterator();
        while(it.hasNext()) {
            tax = it.next();
            if(!tax.getSpecies().toLowerCase().startsWith(last)
                    && !tax.getLastInfraname().toLowerCase().startsWith(last))
                it.remove();
        }*/

        // sort results by their frequency
        List<Map.Entry<Taxon, Integer>> entries = new ArrayList<Map.Entry<Taxon, Integer>>(tmpList.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Taxon, Integer>>() {
            public int compare(Map.Entry<Taxon, Integer> a, Map.Entry<Taxon, Integer> b){
                return a.getValue().compareTo(b.getValue());
            }
        });

        List<Taxon> out = new ArrayList<>();
        for(Map.Entry<Taxon, Integer> e : entries) {
            out.add(e.getKey());
        }
        return out.toArray(new Taxon[out.size()]);
    }

    public int getNFirst() { return nFirst; }
    public int getNLast() { return nLast; }
    public void setNFirst(int nf) { nFirst = nf;}
    public void setNLast(int nl) { nLast = nl;}

    public static String abbreviateTaxon(Taxon t, int nFirst, int nLast) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getGenus().substring(0, nFirst))
           .append(t.getLastInfraname().substring(0, nLast ));
        return sb.toString();
    }

    public List<Integer> getPossibleLetters(String abbrev) {
        return Checklist.getPossibleLetters(getTaxonFromAbbreviation(abbrev), this.nFirst, this.nLast, abbrev);
    }

    public List<Integer> getPossibleLettersFromGenus(String genusPrefix) {
        return Checklist.getPossibleLetters(getTaxaFromGenus(genusPrefix), genusPrefix);
    }

    /**
     * Get possible letters from abbreviation
     * @param possibilities
     * @param nFirst
     * @param nLast
     * @param abbrev
     * @return
     */
    public static List<Integer> getPossibleLetters(Taxon[] possibilities, int nFirst, int nLast, String abbrev) {
        if(possibilities.length == 0) return Collections.emptyList();
        int abl = abbrev.length();
        if(abl >= nFirst + nLast) return Collections.emptyList();

        Set<Character> possibleLetters = new HashSet<>();

        for(Taxon t : possibilities) {
            if(abbreviateTaxon(t, nFirst, nLast).startsWith(abbrev))
                possibleLetters.add(abbreviateTaxon(t, nFirst, nLast).toUpperCase().charAt(abl));
        }

        List<Integer> out = new ArrayList<>();
        for(Character c : possibleLetters) {
            out.add((int) c.charValue());
        }

        return out;
    }

    /**
     * Get possible letters from genus prefix
     * @param possibilities
     * @return
     */
    public static List<Integer> getPossibleLetters(Taxon[] possibilities, String genusPrefix) {
        if(possibilities.length == 0) return Collections.emptyList();
        int abl = genusPrefix.length();
        Set<Character> possibleLetters = new HashSet<>();

        for(Taxon t : possibilities) {
            if(t.getGenus().startsWith(genusPrefix)) {
                if(abl < t.getGenus().length()) // TODO genus acer is not accessible
                    possibleLetters.add(t.getGenus().toUpperCase().charAt(abl));
            }
        }

        List<Integer> out = new ArrayList<>();
        for(Character c : possibleLetters) {
            out.add((int) c.charValue());
        }

        return out;
    }

    public void resetSpeciesFrequencies() {
        for(Map.Entry<Taxon, Integer> el : checklist.entrySet()) {
            el.setValue(0);
        }
    }

    public void setSpeciesFrequencies(Map<String, Integer> freqs) {
        Iterator<Taxon> it = checklist.keySet().iterator();
        Taxon tmp;
        Integer freq;
        while(it.hasNext()) {
            tmp = it.next();
            freq = freqs.get(tmp.toString());
            if(freq != null) checklist.put(tmp, freq);
        }
    }
}
