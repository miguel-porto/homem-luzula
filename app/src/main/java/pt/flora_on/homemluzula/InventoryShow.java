package pt.flora_on.homemluzula;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.flora_on.homemluzula.adapters.InventoryAdapter;
import pt.flora_on.observation_data.Inventories;
import pt.flora_on.observation_data.SpeciesList;

public class InventoryShow extends AppCompatActivity {
    private int clickedSpeciesList;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventories);

        mRecyclerView = (RecyclerView) findViewById(R.id.inventorylist);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        InventoryAdapter adapter = new InventoryAdapter(this, DataManager.allData, new RecyclerViewItemListener() {
            @Override
            public void onClick(View v, int position) {
/*
                Intent intent = new Intent(InventoryShow.this, MainKeyboard.class);
                intent.putExtra("specieslist", MainMap.allData.getSpeciesLists().get(position));
                intent.putExtra("index", position);
                startActivityForResult(intent, MainMap.REPLACE_SPECIESLIST);
*/

                Intent intent = new Intent(InventoryShow.this, ShowObservations.class);
                clickedSpeciesList = position;
                intent.putExtra("specieslist", DataManager.allData.getSpeciesList(position));
                intent.putExtra("index", position);
                intent.putExtra("showaddbutton", true);
                startActivityForResult(intent, MainKeyboard.UPDATE_OBSERVATIONS);
                //Toast.makeText(InventoryShow.this, String.format("%d", position), Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(adapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tv;
        if (resultCode != RESULT_OK) return;
        String uuid = DataManager.allData.getSpeciesList(clickedSpeciesList).getUuid().toString();
//        File invdir = new File(System.getenv("EXTERNAL_STORAGE"), "homemluzula");
//        File repinv = new File(invdir, uuid + ".json");
        switch(requestCode) {
            case MainKeyboard.UPDATE_OBSERVATIONS:
                SpeciesList updated = data.getParcelableExtra("specieslist");

                if(Inventories.saveInventoryToDisk(updated, uuid))
                    Toast.makeText(getApplicationContext(), "Saved inventory.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Some error occurred while saving.", Toast.LENGTH_SHORT).show();

/*
                repinv.delete();
                try {
                    repinv.createNewFile();
                    FileWriter fw = new FileWriter(repinv);
                    fw.append(new Gson().toJson(updated));
                    fw.close();
                    Toast.makeText(getApplicationContext(), "Saved inventory.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
*/
                DataManager.allData.replaceSpeciesList(updated, clickedSpeciesList);
                //MainMap.allData.getSpeciesList(clickedSpeciesList).replaceTaxa(updated);
                mRecyclerView.getAdapter().notifyItemChanged(clickedSpeciesList);
                break;
        }
    }

}
