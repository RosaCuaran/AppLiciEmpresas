package com.codigoj.lici;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codigoj.lici.model.Coupon;
import com.codigoj.lici.model.Publication;
import com.codigoj.lici.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailPublicationFragmentCoupons extends Fragment {


    //Element GUI
    private TextView titleNamePublication;
    private TextView tvQuantityCoupons;
    private Button btn_reserved_coupons;
    private Button btn_redeemed_coupons;

    //Constant for the references
    public static final String COUPONS = "coupons";
    private FirebaseDatabase database;
    //Variables
    private String id_publication;

    public DetailPublicationFragmentCoupons() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail_publication, container, false);
        titleNamePublication = (TextView) v.findViewById(R.id.title_name_publication);
        tvQuantityCoupons = (TextView) v.findViewById(R.id.tvQuantityCoupons);
        btn_reserved_coupons = (Button) v.findViewById(R.id.btn_reserved_coupons);
        btn_redeemed_coupons = (Button) v.findViewById(R.id.btn_redeemed_coupons);
        //instances
        database = FirebaseDatabase.getInstance();

        //Load the data from activity
        if(this.getArguments().containsKey(Utils.KEY_ID_PUBLICATION)) {
            id_publication = this.getArguments().getString(Utils.KEY_ID_PUBLICATION);
            Log.d("publication-det", "id:" + id_publication);
            loadPublicationSaved(id_publication);
        }
        final Bundle args = new Bundle();
        args.putString(Utils.KEY_ID_PUBLICATION, id_publication);

        //Create the listener for the buttons
        btn_reserved_coupons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                DetailPublicationFragmentCouponsList fragment = new DetailPublicationFragmentCouponsList();
                args.putBoolean(Coupon.RESERVED, true);
                fragment.setArguments(args);
                transaction.replace(R.id.dinamic_fragment, fragment);
                transaction.commit();
            }
        });
        btn_redeemed_coupons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                DetailPublicationFragmentCouponsList fragment = new DetailPublicationFragmentCouponsList();
                args.putBoolean(Coupon.RESERVED, false);
                fragment.setArguments(args);
                transaction.replace(R.id.dinamic_fragment, fragment);
                transaction.commit();
            }
        });
        return v;
    }


    /**
     * Method that load the data for the publication sent like parameter
     * @param id_pub
     */
    private void loadPublicationSaved(String id_pub) {
        Query ref = database.getReference().child(Utils.REF_PUBLICATIONS)
                .child(id_pub);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //we need the name of publication, coupons number, reserved coupons and redeemed coupons for show in the layout
                final Publication promotionLoaded = dataSnapshot.getValue(Publication.class);
                DataSnapshot coupons = dataSnapshot.child(Utils.REF_COUPONS);
                for ( DataSnapshot coupon : coupons.getChildren()){
                    Coupon c = coupon.getValue(Coupon.class);
                    promotionLoaded.addCoupon(c);
                }
                //Load the info coupons
                titleNamePublication.setText(promotionLoaded.getName() );
                tvQuantityCoupons.setText("Cupones: " + promotionLoaded.getCountList() );
                btn_reserved_coupons.setText("Cupones reservados: " + promotionLoaded.getCountListReserved() );
                btn_redeemed_coupons.setText("Cupones redimidos: " + promotionLoaded.getCountListRedeemed() );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                btn_reserved_coupons.setOnClickListener(null);
                btn_redeemed_coupons.setOnClickListener(null);
            }
        });
    }
}
