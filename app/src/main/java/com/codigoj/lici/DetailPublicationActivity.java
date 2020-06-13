package com.codigoj.lici;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.codigoj.lici.dummy.DummyContent;
import com.codigoj.lici.utils.Utils;

public class DetailPublicationActivity extends AppCompatActivity implements DetailPublicationFragmentCouponsList.OnListFragmentInteractionListener {


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_detail_publication);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        //load the data for edit the publication
        String data = "";
        if ( getIntent().getExtras() != null ) {
            if( getIntent().getExtras().containsKey(Utils.KEY_ID_PUBLICATION) ) {
                data = getIntent().getExtras().getString(Utils.KEY_ID_PUBLICATION);
                Log.d("coupon-id-pub", data);
            }
        }
        if ( !data.isEmpty() ) {
            DetailPublicationFragmentCoupons fragment1 = new DetailPublicationFragmentCoupons();
            Bundle bundle = new Bundle();
            bundle.putString(Utils.KEY_ID_PUBLICATION, data);
            fragment1.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.dinamic_fragment, fragment1).commit();
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
