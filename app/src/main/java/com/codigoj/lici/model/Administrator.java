package com.codigoj.lici.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.codigoj.lici.LoginActivity;
import com.codigoj.lici.ProfileActivity;
import com.codigoj.lici.R;
import com.codigoj.lici.TabsPublication;
import com.codigoj.lici.data.AppPreferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by JHON on 06/04/2017.
 */

public class Administrator {

    private static final String TAG = Activity.class.getName();
    //Constant of admin
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_DATE_VALIDITY = "date_validity";
    public static final String KEY_NUM_PUBLICATIONS = "num_Publications";

    //Administrator
    private String date_validity;
    private int num_Publications;
    private long timestamp;
    private String id;
    private Context context;
    //Save data
    AppPreferences appPreferences;
    //Progress dialog
    private ProgressDialog progressDialog;
    //Firebase
    private FirebaseDatabase database;

    //Verify the valid
    public Administrator(String id, Context context) {
        progressDialog = new ProgressDialog(context);
        database = FirebaseDatabase.getInstance();
        this.id = id;
        this.context = context;
        //SharedPreferences instance
        appPreferences = new AppPreferences(context);
    }

    //Methods
    public void loadServerData(boolean start) {
        //Database reference
        final boolean startTabs = start;
        final DatabaseReference ref = database.getReference().child("administrator").child(id);
        progressDialog.setMessage(context.getResources().getString(R.string.loading_data));
        progressDialog.show();

        HashMap<String, Object> result = new HashMap<>();
        result.put("/timestamp", ServerValue.TIMESTAMP);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot attributeAdmin : dataSnapshot.getChildren()) { //Take one attributeAdmin from the list
                    if (attributeAdmin.getKey().equals(KEY_TIMESTAMP)) {
                        timestamp = (Long) attributeAdmin.getValue();
                        appPreferences.saveDataString(Administrator.KEY_TIMESTAMP, String.valueOf(timestamp));
                    } else if (attributeAdmin.getKey().equals(KEY_DATE_VALIDITY)) {
                        date_validity = attributeAdmin.getValue().toString();
                        appPreferences.saveDataString(Administrator.KEY_DATE_VALIDITY, date_validity);
                    } else if (attributeAdmin.getKey().equals(KEY_NUM_PUBLICATIONS)) {
                        num_Publications = Integer.parseInt((String) attributeAdmin.getValue());
                        appPreferences.saveDataInt(Administrator.KEY_NUM_PUBLICATIONS, num_Publications);
                    }
                }
                progressDialog.dismiss();
                Toast.makeText(context, "Tienes para crear " + num_Publications + " publicaciones", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(context, "Error: "+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ref.updateChildren(result, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (startTabs){
                    ((LoginActivity)context).finish();
                    Intent tabsPub = new Intent(context, TabsPublication.class);
                    tabsPub.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(tabsPub);
                } else {
                    ((LoginActivity)context).finish();
                    Intent profile = new Intent(((LoginActivity)context), ProfileActivity.class);
                    profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(profile);
                }
            }
        });
    }

    //--------------------------
    //GETTERS AND SETTERS
    //--------------------------

    public Date getDate_validity_Date() {
        return new Date(Long.parseLong(date_validity));
    }

    public Date getTimestamp_Date() { return new Date(timestamp); }

    public String getDate_validity() { return date_validity; }

    public Long getTimestamp() { return timestamp; }

    public int getNum_Publications() {
        return num_Publications;
    }

    public void setNum_Publications(int num_Publications) { this.num_Publications = num_Publications; }
}
