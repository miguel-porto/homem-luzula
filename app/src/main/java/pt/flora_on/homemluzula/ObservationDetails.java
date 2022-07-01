package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.TaxonObservation;

public class ObservationDetails extends AppCompatActivity {
    private TaxonObservation observation;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_details);
        setSupportActionBar((Toolbar) findViewById(R.id.details_toolbar));

//        View mContentView = findViewById(R.id.activity_observation_details);
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.obs_details_toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).setItems(Constants.NaturalizationState.getLabels());
        ((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).setItems(Constants.AbundanceType.getLabels());
        ((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).setItems(Constants.PhenologicalState.getLabels());

        observation = getIntent().getParcelableExtra("taxon");
        setTitle(observation.getTaxonCapital());

        // check if it is pinned
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());
        Set<String> pinned = preferences.getStringSet("pinnedTaxa", new HashSet<String>());
        if (pinned != null && pinned.contains(observation.getTaxon())) {
            ((FloatingActionButton) findViewById(R.id.pin_taxon)).setImageResource(R.drawable.ic_star_black_24dp);
            findViewById(R.id.pin_taxon).setTag("pinned");
        }

        if (getIntent().hasExtra("showDelete")) {
            findViewById(R.id.delete_taxon).setVisibility(View.VISIBLE);
            findViewById(R.id.delete_taxon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent data = new Intent();
                    data.putExtra("delete", true);
                    if (getIntent().hasExtra("index"))
                        data.putExtra("index", getIntent().getIntExtra("index", -1));
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
            });
        } else
            findViewById(R.id.delete_taxon).setVisibility(View.INVISIBLE);

        findViewById(R.id.pin_taxon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(HomemLuzulaApp.getAppContext());
                Set<String> pinned = new HashSet<String>(preferences.getStringSet("pinnedTaxa", new HashSet<String>()));

                if (Objects.equals(view.getTag(), "pinned")) {
                    pinned.remove(observation.getTaxon().toLowerCase());
                    ((FloatingActionButton) findViewById(R.id.pin_taxon)).setImageResource(R.drawable.ic_star_border_black_24dp);
                    view.setTag(null);
                } else {
                    if (pinned.size() == 4) {
                        Toast.makeText(ObservationDetails.this, "No more than 4 pinned species allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pinned.add(observation.getTaxon().toLowerCase());
                    ((FloatingActionButton) findViewById(R.id.pin_taxon)).setImageResource(R.drawable.ic_star_black_24dp);
                    view.setTag("pinned");
                }
                SharedPreferences.Editor editor = preferences.edit();
                editor.putStringSet("pinnedTaxa", pinned);
                editor.apply();
            }
        });

        findViewById(R.id.details_toolbar).setOnClickListener(view -> {
            Intent intent = new Intent(this, MainKeyboard.class);
            intent.putExtra("selectSpecies", true);
            startActivityForResult(intent, MainKeyboard.UPDATE_OBSERVATIONS);
        });

        // save details
        findViewById(R.id.save_obs_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observation.setConfidence(((CheckBox) findViewById(R.id.doubt_id)).isChecked() ? Constants.Confidence.UNCERTAIN : Constants.Confidence.CERTAIN);
                observation.setPhenoState(((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).getSelectedItem());
                observation.setNaturalizationState(((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).getSelectedItem());
                observation.setAbundanceType(((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).getSelectedItem());
                observation.setAbundance(((TextView) findViewById(R.id.text_abundance)).getText().toString());
                observation.setCover(((TextView) findViewById(R.id.text_cover)).getText().toString());
                observation.setComment(((TextView) findViewById(R.id.text_comment)).getText().toString());

                Intent data = new Intent();
                data.putExtra("observation", observation);
                if (getIntent().hasExtra("index"))
                    data.putExtra("index", getIntent().getIntExtra("index", -1));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        if (getIntent().hasExtra("fill_fields")) {
            // fill in the fields with given data
            ((CheckBox) findViewById(R.id.doubt_id)).setChecked(observation.getConfidence() == Constants.Confidence.UNCERTAIN);
            ((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).setSelectedItem(Arrays.binarySearch(Constants.PhenologicalState.values(), observation.getPhenoState()));
            ((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).setSelectedItem(Arrays.binarySearch(Constants.NaturalizationState.values(), observation.getNaturalizationState()));
            ((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).setSelectedItem(Arrays.binarySearch(Constants.AbundanceType.values(), observation.getAbundanceType()));
            ((TextView) findViewById(R.id.text_abundance)).setText(observation.getAbundance());
            ((TextView) findViewById(R.id.text_cover)).setText(observation.getCover());
            ((TextView) findViewById(R.id.text_comment)).setText(observation.getComment());
            if (observation.getObservationLatitude() != null && observation.getObservationLatitude() != 0)
                ((TextView) findViewById(R.id.observationCoordinates)).setText(String.format(Locale.US, "Coordinates: %.5f %.5f", observation.getObservationLatitude(), observation.getObservationLongitude()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case MainKeyboard.UPDATE_OBSERVATIONS:
                SpeciesList sl = data.getParcelableExtra("specieslist");
                if(sl != null) {
                    observation.setTaxon(sl.getTaxa().get(0).getTaxonCapital());
                    setTitle(observation.getTaxonCapital());
                }
                break;
            default:
        }
    }
}