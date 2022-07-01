package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.flora_on.homemluzula.adapters.ObservationAdapterPhenology;
import pt.flora_on.homemluzula.adapters.SelectSpeciesListener;
import pt.flora_on.observation_data.TaxonObservation;

public class SpeciesChooser extends AppCompatActivity {
    static public final int GET_OBSERVATION = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_chooser1);

//        View mContentView = findViewById(R.id.activity_species_chooser1);
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.specieschooser_toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String[] taxa = intent.getStringArrayExtra("taxa");

        SelectSpeciesListener btnClick = new SelectSpeciesListener() {
            @Override
            public void editSpecies(TaxonObservation obs, int position) {
                Activity act = SpeciesChooser.this;
                Intent data = new Intent();
                data.putExtra("observation", (Parcelable) obs);
                act.setResult(Activity.RESULT_OK, data);
                act.finish();
                //Toast.makeText(v.getContext(), ((TaxonObservation) v.getTag()).getTaxon() + ((TaxonObservation) v.getTag()).getPhenoState().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void commitSpecies(TaxonObservation obs, int position) {

            }
        };

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.specieslist);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new Divider(this, R.drawable.rectangle));
        mRecyclerView.setAdapter(new ObservationAdapterPhenology(taxa, btnClick, this));
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(mRecyclerView.getAdapter().getItemCount() - 1, 0);
            }
        }, 500);
    }

    public void selectSpeciesAndFinish(View v) {
        Activity act = SpeciesChooser.this;
        Intent data = new Intent();
        data.putExtra("observation", (Parcelable) v.getTag());
        act.setResult(Activity.RESULT_OK, data);
        act.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch(requestCode) {
            case GET_OBSERVATION:
                TaxonObservation tObs = data.getParcelableExtra("observation");
                Intent dataout = new Intent();
                dataout.putExtra("observation", tObs);
                setResult(Activity.RESULT_OK, dataout);
                finish();
                break;
        }
    }

}
