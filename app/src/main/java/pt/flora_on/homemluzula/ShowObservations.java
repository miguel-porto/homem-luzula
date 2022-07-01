package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;

import pt.flora_on.homemluzula.adapters.ObservationAdapterAbundance;
import pt.flora_on.homemluzula.adapters.ObservationAdapterPhenology;
import pt.flora_on.homemluzula.adapters.SelectSpeciesListener;
import pt.flora_on.observation_data.SpeciesList;
import pt.flora_on.observation_data.TaxonObservation;

public class ShowObservations extends AppCompatActivity {
    private boolean changed = false;
    private SpeciesList mSpeciesList;
    /**
     * Index of the specieslist in the original array
     */
    private int mIndex;
    //private ArrayList<TaxonObservation> taxonObservations;
    private ObservationAdapterAbundance adapter;
    private RecyclerView mRecyclerView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_showobservation, menu);

/*        MenuItem saveButton = menu.findItem(R.id.save_observations);
        if(saveButton != null) saveButton.setVisible(modified);
        */
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        int index = data.getIntExtra("index", -1);

        switch (requestCode) {
            case SpeciesChooser.GET_OBSERVATION:
                TaxonObservation obs = data.getParcelableExtra("observation");
                updateSpecies(obs, index);
                break;

            case MainMap.REPLACE_SPECIESLIST:
                mSpeciesList = data.getParcelableExtra("specieslist");
                adapter.replaceData(mSpeciesList);
                adapter.notifyDataSetChanged();
                //adapter.notifyItemInserted(mSpeciesList.getNumberOfSpecies() - 1);

/*                if(index < 0) new AlertDialog.Builder(this).setTitle("Error").setCancelable(false).setPositiveButton(android.R.string.yes, null).setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Erro substituindo o inventário").show();
                else
                    MainMap.allData.replaceSpeciesList(mSpeciesList, index);*/
                findViewById(R.id.save_inventario).setVisibility(View.VISIBLE);
                changed = true;
                break;
        }

        //Toast.makeText(MainMap.this, gs.toJson(sList), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_observations);
        setSupportActionBar((Toolbar) findViewById(R.id.observations_toolbar));

        Intent intent = getIntent();
        mSpeciesList = intent.getParcelableExtra("specieslist");
        mIndex = intent.getIntExtra("index", -1);

        if(mSpeciesList.getLatitude() == null || mSpeciesList.getLongitude() == null)
            setTitle(String.format(Locale.getDefault(), "Sem coordenadas %d/%d/%d", mSpeciesList.getDay(), mSpeciesList.getMonth(), mSpeciesList.getYear()));
        else
            setTitle(String.format(Locale.getDefault(), "%.4fº %.4fº %d/%d/%d", mSpeciesList.getLatitude(), mSpeciesList.getLongitude()
                    , mSpeciesList.getDay(), mSpeciesList.getMonth(), mSpeciesList.getYear()));

        /**
         * Add new species
         */
        if(intent.getBooleanExtra("showaddbutton", false)) {
            findViewById(R.id.add_species).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowObservations.this, MainKeyboard.class);
                    intent.putExtra("specieslist", mSpeciesList);
                    intent.putExtra("index", mIndex);
                    intent.putExtra("onlyadd", true);
                    startActivityForResult(intent, MainMap.REPLACE_SPECIESLIST);
                }
            });
        } else findViewById(R.id.add_species).setVisibility(View.GONE);

        findViewById(R.id.save_inventario).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View vf = getCurrentFocus();
                if(vf != null) {
                    if(vf.getId() == R.id.abundance) {
                        vf.clearFocus();
                    }
                }
                Intent data = new Intent();
                data.putExtra("specieslist", mSpeciesList);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.listofspecies);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new ObservationAdapterAbundance(mSpeciesList, new SelectSpeciesListener() {
            @Override
            public void editSpecies(TaxonObservation obs, int position) {
//                TaxonObservation to = (TaxonObservation) v.getTag();
//                Toast.makeText(ShowObservations.this, to.getTaxon() + ": "+to.getPhenoState().toString()+ " "+position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ShowObservations.this, ObservationDetails.class);
                intent.putExtra("taxon", obs);
                intent.putExtra("fill_fields", true);
                intent.putExtra("index", position);
                startActivityForResult(intent, SpeciesChooser.GET_OBSERVATION);
            }

            @Override
            public void commitSpecies(TaxonObservation obs, int position) {
                updateSpecies(obs, position);
            }

        });
        mRecyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int pos = viewHolder.getAdapterPosition();
                ObservationAdapterAbundance adapter = (ObservationAdapterAbundance) mRecyclerView.getAdapter();

                if(mSpeciesList.getTaxa() != null && adapter != null) {
                    mSpeciesList.getTaxa().remove(pos);
                    adapter.notifyItemRemoved(pos);
                    adapter.notifyDataSetChanged();
                    invalidateOptionsMenu();
                    findViewById(R.id.save_inventario).setVisibility(View.VISIBLE);
                    changed = true;
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    private void updateSpecies(TaxonObservation obs, int position) {
        if(mSpeciesList.getTaxa().size() == 0) return;
        mSpeciesList.getTaxa().set(position, obs);
        try {
            adapter.notifyItemChanged(position);
        } catch(Exception e) {
            Log.e("OOOOOOO", "Could not");
        }
        findViewById(R.id.save_inventario).setVisibility(View.VISIBLE);
        changed = true;
    }
}
