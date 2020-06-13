package com.codigoj.lici;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codigoj.lici.dummy.DummyContent;
import com.codigoj.lici.dummy.DummyContent.DummyItem;
import com.codigoj.lici.model.Coupon;
import com.codigoj.lici.model.Publication;
import com.codigoj.lici.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DetailPublicationFragmentCouponsList extends Fragment {

    //Constant for the references
    public static final String COUPONS = "coupons";
    private FirebaseDatabase database;
    //Variables
    private String id_publication;
    public static LinkedHashMap<Integer, Object> userNames;
    public static LinkedHashMap<Integer, Object> codes;
    private boolean reserved;
    private OnListFragmentInteractionListener mListener;
    private View view;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailPublicationFragmentCouponsList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_coupons_list, container, false);

        //instances
        database = FirebaseDatabase.getInstance();

        //Load the data from activity
        //if the arg contains reserved == true the search will be by the reserved, but reserved == false
        //the search will be by the redeemed
        if(this.getArguments().containsKey(Utils.KEY_ID_PUBLICATION) && this.getArguments().containsKey(Coupon.RESERVED)) {
            id_publication = this.getArguments().getString(Utils.KEY_ID_PUBLICATION);
            reserved = this.getArguments().getBoolean(Coupon.RESERVED);
            loadUsersAndCoupons();
        }
        return view;
    }

    private void loadUsersAndCoupons() {
        String type = Coupon.RESERVED;
        if (!reserved)
            type = Coupon.REDEEMED;
        Query ref = database.getReference().child(Utils.REF_PUBLICATIONS)
                .child(id_publication).child(Utils.REF_COUPONS).orderByChild(Utils.REF_TYPE).equalTo(type);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Publication pub = new Publication();
                final ArrayList<String[]> data = new ArrayList<String[]>();
                //If dataSnapshot has not children mean there are not coupons, so the list is empty
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot coupon : dataSnapshot.getChildren()){
                        Coupon myCoupon = coupon.getValue(Coupon.class);
                        pub.addCoupon(myCoupon);
                    }
                    final ArrayList<Coupon> couponArrayList = pub.getListCoupons();
                    for (int i=0; i<couponArrayList.size(); i++) {
                        final Coupon myCoupon = couponArrayList.get(i);
                        Query ref = database.getReference().child(Utils.REF_USERS).child(couponArrayList.get(i).getId_user()).child(Utils.REF_NAME);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String code = myCoupon.getId_coupon().substring(myCoupon.getId_coupon().length()-8,myCoupon.getId_coupon().length());
                                String[] d = new String[2];
                                d[0] = code;
                                d[1] = dataSnapshot.getValue().toString();
                                data.add(d);
                                // Set the adapter
                                if (view instanceof RecyclerView) {
                                    Context context = view.getContext();
                                    RecyclerView recyclerView = (RecyclerView) view;
                                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                    recyclerView.setAdapter(new MycouponsRecyclerViewAdapter(data));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
