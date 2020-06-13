package com.codigoj.lici;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codigoj.lici.DetailPublicationFragmentCouponsList.OnListFragmentInteractionListener;
import com.codigoj.lici.dummy.DummyContent.DummyItem;
import com.codigoj.lici.model.Coupon;
import com.codigoj.lici.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MycouponsRecyclerViewAdapter extends RecyclerView.Adapter<MycouponsRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<String[]> couponList;
    private FirebaseDatabase database;

    public MycouponsRecyclerViewAdapter(ArrayList<String[]> items) {
        couponList = items;
        //instances
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_coupons, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String[] codeName = couponList.get(position);
        holder.username.setText(codeName[0]);
        holder.couponCode.setText(codeName[1]);
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView username;
        public final TextView couponCode;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            username = (TextView) view.findViewById(R.id.coupon_user_name);
            couponCode = (TextView) view.findViewById(R.id.coupon_code);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + couponCode.getText() + "'";
        }
    }
}
