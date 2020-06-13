package com.codigoj.lici;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvVersion;
    private TextView tvNameEcreations;
    private TextView tvAllRightsReserved;
    private TextView tvTitleCreditsDevelopment;
    private TextView tvNamesCreditsDevelopment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvNameEcreations = (TextView) findViewById(R.id.tvNameEcreations);
        tvAllRightsReserved = (TextView) findViewById(R.id.tvAllRightsReserved);
        tvTitleCreditsDevelopment = (TextView) findViewById(R.id.tvTitleCreditsDevelopment);
        tvNamesCreditsDevelopment = (TextView) findViewById(R.id.tvNamesCreditsDevelopment);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadFonts();
    }

    private void loadFonts() {
        Typeface berlinSansFB = Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        tvVersion.setTypeface(berlinSansFB);
        tvNameEcreations.setTypeface(berlinSansFB);
        tvAllRightsReserved.setTypeface(berlinSansFB);
        tvTitleCreditsDevelopment.setTypeface(berlinSansFB);
        tvNamesCreditsDevelopment.setTypeface(berlinSansFB);
    }
}
