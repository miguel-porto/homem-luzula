package pt.flora_on.homemluzula;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import pt.flora_on.observation_data.SpeciesList;

public class InventoryProperties extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private SpeciesList speciesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_properties);

        Toolbar toolbar = (Toolbar) findViewById(R.id.invdetails_toolbar);
        setSupportActionBar(toolbar);

        speciesList = this.getIntent().getParcelableExtra("speciesList");

        ((TextInputEditText) findViewById(R.id.gpsCode)).setText(speciesList.getGpsCode());
        ((TextInputEditText) findViewById(R.id.habitat)).setText(speciesList.getHabitat());
        findViewById(R.id.save_inv_details).setVisibility(View.GONE);

        ((TextInputEditText) findViewById(R.id.habitat)).addTextChangedListener(this);
        ((TextInputEditText) findViewById(R.id.gpsCode)).addTextChangedListener(this);

        findViewById(R.id.save_inv_details).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_inv_details:
                Editable tmp = ((TextInputEditText) findViewById(R.id.gpsCode)).getText();
                speciesList.setGpsCode(tmp == null ? "" : tmp.toString());
                tmp = ((TextInputEditText) findViewById(R.id.habitat)).getText();
                speciesList.setHabitat(tmp == null ? "" : tmp.toString());

                Intent data = new Intent();
                data.putExtra("specieslist", speciesList);
                setResult(Activity.RESULT_OK, data);
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        findViewById(R.id.save_inv_details).setVisibility(View.VISIBLE);
    }
}
