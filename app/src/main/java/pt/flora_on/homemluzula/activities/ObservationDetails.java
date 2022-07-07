package pt.flora_on.homemluzula.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import pt.flora_on.homemluzula.ClickToSelectEditText;
import pt.flora_on.homemluzula.HomemLuzulaApp;
import pt.flora_on.homemluzula.R;
import pt.flora_on.observation_data.Constants;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.TaxonObservation;

public class ObservationDetails extends AppCompatActivity implements View.OnClickListener {
    private TaxonObservation observation;
    private Uri imageUri;
    private String uuid;
    private boolean hasPhoto = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("hasPhoto", hasPhoto);
        outState.putParcelable("uri", imageUri);
        super.onSaveInstanceState(outState);
    }
    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.observation_details);
        setSupportActionBar((Toolbar) findViewById(R.id.details_toolbar));

        if (savedInstanceState != null) {
            hasPhoto = savedInstanceState.getBoolean("hasPhoto");
            imageUri = savedInstanceState.getParcelable("uri");
        }

        if(hasPhoto)
            findViewById(R.id.take_photo_species).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BFFF00")));

        ((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).setItems(Constants.NaturalizationState.getLabels());
        ((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).setItems(Constants.AbundanceType.getLabels());
        ((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).setItems(Constants.PhenologicalState.getLabels());

        observation = getIntent().getParcelableExtra("taxon");
        uuid = getIntent().getStringExtra("uuid");
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
            findViewById(R.id.delete_taxon).setOnClickListener(this);
        } else
            findViewById(R.id.delete_taxon).setVisibility(View.INVISIBLE);

        findViewById(R.id.pin_taxon).setOnClickListener(this);
        findViewById(R.id.details_toolbar).setOnClickListener(this);
        // save details
        findViewById(R.id.save_obs_details).setOnClickListener(this);
        if(uuid != null)
            findViewById(R.id.take_photo_species).setOnClickListener(this);
        else
            findViewById(R.id.take_photo_species).setVisibility(View.GONE);

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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case MainKeyboard.UPDATE_OBSERVATIONS:
                SpeciesList sl = data.getParcelableExtra("specieslist");
                if(sl != null) {
                    observation.setTaxon(sl.getTaxa().get(0).getTaxonCapital());
                    setTitle(observation.getTaxonCapital());
                }
                break;

            case MainKeyboard.TAKE_PHOTO:
                hasPhoto = true;
                findViewById(R.id.take_photo_species).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BFFF00")));
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.details_toolbar) {
            Intent intent = new Intent(this, MainKeyboard.class);
            intent.putExtra("selectSpecies", true);
            startActivityForResult(intent, MainKeyboard.UPDATE_OBSERVATIONS);
        }

        if(view.getId() == R.id.save_obs_details) {
            observation.setConfidence(((CheckBox) findViewById(R.id.doubt_id)).isChecked() ? Constants.Confidence.UNCERTAIN : Constants.Confidence.CERTAIN);
            observation.setPhenoState(((ClickToSelectEditText<Constants.PhenologicalState>) findViewById(R.id.spi_estadofenologico)).getSelectedItem());
            observation.setNaturalizationState(((ClickToSelectEditText<Constants.NaturalizationState>) findViewById(R.id.spi_naturstate)).getSelectedItem());
            observation.setAbundanceType(((ClickToSelectEditText<Constants.AbundanceType>) findViewById(R.id.spi_abundancetype)).getSelectedItem());
            observation.setAbundance(((TextView) findViewById(R.id.text_abundance)).getText().toString());
            observation.setCover(((TextView) findViewById(R.id.text_cover)).getText().toString());
            observation.setComment(((TextView) findViewById(R.id.text_comment)).getText().toString());

            Intent data = new Intent();
            data.putExtra("observation", observation);
            data.putExtra("hasPhoto", hasPhoto);
            if(hasPhoto)
                MainKeyboard.savePhoto(imageUri, this, uuid, observation.getTaxonCapital());

            if (getIntent().hasExtra("index"))
                data.putExtra("index", getIntent().getIntExtra("index", -1));
            setResult(Activity.RESULT_OK, data);
            finish();
        }

        if(view.getId() == R.id.delete_taxon) {
            Intent data = new Intent();
            data.putExtra("delete", true);
            if (getIntent().hasExtra("index"))
                data.putExtra("index", getIntent().getIntExtra("index", -1));
            setResult(Activity.RESULT_OK, data);
            finish();
        }

        if(view.getId() == R.id.pin_taxon) {
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

        if(view.getId() == R.id.take_photo_species) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, MainKeyboard.TAKE_PHOTO);
            }
        }
    }
}