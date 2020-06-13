package com.codigoj.lici;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class PoliticsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvTitleConditions;
    private TextView tvParagraphConditions;
    private TextView tvTitleRestrict;
    private TextView tvParagraphRestrict;
    private TextView tvTitleDuration;
    private TextView tvParagraphDuration;
    private TextView tvTitlePolitics;
    private TextView tvParagraphPolitics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_politics);
        toolbar = (Toolbar) findViewById(R.id.toolbar_politics);
        tvTitleConditions = (TextView) findViewById(R.id.tvTitleConditions);
        tvParagraphConditions = (TextView) findViewById(R.id.tvParagraphConditions);
        tvTitleRestrict = (TextView) findViewById(R.id.tvTitleRestrict);
        tvParagraphRestrict = (TextView) findViewById(R.id.tvParagraphRestrict);
        tvTitleDuration = (TextView) findViewById(R.id.tvTitleDuration);
        tvParagraphDuration = (TextView) findViewById(R.id.tvParagraphDuration);
        tvTitlePolitics = (TextView) findViewById(R.id.tvTitlePolitics);
        tvParagraphPolitics = (TextView) findViewById(R.id.tvParagraphPolitics);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tvParagraphPolitics.setText(Html.fromHtml(getResources().getString(R.string.tvParagraphPolitics)));
        tvParagraphPolitics.setMovementMethod(LinkMovementMethod.getInstance());
        loadFonts();
    }

    private void loadFonts() {
        Typeface berlinSansFB= Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        tvTitleConditions.setTypeface(berlinSansFB);
        tvTitleDuration.setTypeface(berlinSansFB);
        tvTitlePolitics.setTypeface(berlinSansFB);
        tvTitleRestrict.setTypeface(berlinSansFB);
        tvParagraphConditions.setTypeface(berlinSansFB);
        tvParagraphDuration.setTypeface(berlinSansFB);
        tvParagraphPolitics.setTypeface(berlinSansFB);
        tvParagraphRestrict.setTypeface(berlinSansFB);
    }
}
