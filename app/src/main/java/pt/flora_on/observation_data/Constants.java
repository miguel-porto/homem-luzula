package pt.flora_on.observation_data;

import java.util.ArrayList;
import java.util.List;

import pt.flora_on.homemluzula.Listable;

/**
 * Created by miguel on 04-10-2016.
 */

public final class Constants {
    public enum PhenologicalState implements Listable {
        NULL("Não especificado")
        , VEGETATIVE("Vegetativo")
        , FLOWER("Floração")
        , DISPERSION("Em dispersão")
        , FLOWER_DISPERSION("Em floração e dispersão")
        , FRUIT("Fruto imaturo")
        , RESTING("Em dormência")
        , FLOWER_FRUIT("Flor e fruto");

        private final String verbose;

        PhenologicalState (String verbose) {
            this.verbose = verbose;
        }
        public static List<PhenologicalState> getLabels() {
            PhenologicalState[] v = values();
            List<PhenologicalState> out = new ArrayList<>(v.length);
            for(int i = 0; i < v.length; i++)
                out.add(v[i]);
            return out;
        }

        public String getLabel() {
            return this.verbose;
        }

        public boolean isFlowering() {
            return this == FLOWER || this == FLOWER_DISPERSION || this == FLOWER_FRUIT;
        }
    }

    public enum NaturalizationState implements Listable {
        WILD("Espontânea")
        , NATURALIZED("Naturalizada")
        , CULTIVATED("Cultivada");

        private final String verbose;

        NaturalizationState(String verbose) {
            this.verbose = verbose;
        }

        static public List<NaturalizationState> getLabels() {
            NaturalizationState[] v = values();
            List<NaturalizationState> out = new ArrayList<>(v.length);
            for(int i = 0; i < v.length; i++)
                out.add(v[i]);
            return out;
        }

        @Override
        public String getLabel() {
            return this.verbose;
        }
    }

    public enum Confidence {CERTAIN, UNCERTAIN}

    public enum AbundanceType implements Listable {
        NO_DATA("Não especificado")
        , EXACT_COUNT("Contagem exacta")
        , APPROXIMATE_COUNT("Estimativa numérica")
        , ROUGH_ESTIMATE("Estimativa grosseira");

        private final String verbose;

        AbundanceType(String verbose) {
            this.verbose = verbose;
        }

        @Override
        public String getLabel() {
            return this.verbose;
        }

        static public List<AbundanceType> getLabels() {
            AbundanceType[] v = values();
            List<AbundanceType> out = new ArrayList<>(v.length);
            for(int i = 0; i < v.length; i++)
                out.add(v[i]);
            return out;
        }

    }

}
