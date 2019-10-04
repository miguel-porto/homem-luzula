package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;

import pt.flora_on.observation_data.Constants;
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

        // save details
        findViewById(R.id.save_obs_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observation.setConfidence( ((CheckBox) findViewById(R.id.doubt_id)).isChecked() ? Constants.Confidence.UNCERTAIN : Constants.Confidence.CERTAIN );
                observation.setPhenoState( ((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).getSelectedItem() );
                observation.setNaturalizationState( ((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).getSelectedItem() );
                observation.setAbundanceType( ((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).getSelectedItem() );
                observation.setAbundance( ((TextView) findViewById(R.id.text_abundance)).getText().toString() );
                observation.setComment( ((TextView) findViewById(R.id.text_comment)).getText().toString() );

                Intent data = new Intent();
                data.putExtra("observation", observation);
                if(getIntent().hasExtra("index"))
                    data.putExtra("index", getIntent().getIntExtra("index", -1));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        if(getIntent().hasExtra("fill_fields")) {
            // fill in the fields with given data
            ((CheckBox) findViewById(R.id.doubt_id)).setChecked(observation.getConfidence() == Constants.Confidence.UNCERTAIN);
            ((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).setSelectedItem(Arrays.binarySearch(Constants.PhenologicalState.values(), observation.getPhenoState()) );
            ((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).setSelectedItem(Arrays.binarySearch(Constants.NaturalizationState.values(), observation.getNaturalizationState()) );
            ((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).setSelectedItem(Arrays.binarySearch(Constants.AbundanceType.values(), observation.getAbundanceType()) );
            ((TextView) findViewById(R.id.text_abundance)).setText(observation.getAbundance());
            ((TextView) findViewById(R.id.text_comment)).setText(observation.getComment());
        }
    }
}
