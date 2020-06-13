package com.codigoj.lici;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codigoj.lici.model.Publication;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JHON on 21/02/2017.
 */

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.PublicationViewHolder> {

    List<Publication> publications = new ArrayList<Publication>();
    FirebaseDatabase database;
    StorageReference storage;

    public PublicationAdapter(FirebaseDatabase database, StorageReference storage, String id) {

        Query myTopPostsQuery = database.getReference().child("publications_board").child(Publication.TYPE_EVENT).child(id)
                .orderByChild("id");
        myTopPostsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    Log.i("Count ", "-----" + dataSnapshot.getChildrenCount());
                    Map<String, Object> objectMap = (HashMap<String, Object>)
                            dataSnapshot.getValue();
                    Log.i("Object Map", "-----" + objectMap);
                    for (Object obj : objectMap.values()) {
                        if (obj instanceof Map) {
                            try{
                                Map<String, Object> mapObj = (Map<String, Object>) obj;
                                Log.i("Object ", "-----" + obj);
                                Publication itemsReceived = new Publication();
                                itemsReceived.setId((String) mapObj.get(Publication.PUBLICATION_ID));
                                itemsReceived.setName((String) mapObj.get(Publication.PUBLICATION_NAME));
                                itemsReceived.setPath_image_pub((String) mapObj.get(Publication.PUBLICATION_PATH_IMAGE_PUB));
                                itemsReceived.setDescription((String) mapObj.get(Publication.PUBLICATION_DESCRIPTION));
                                itemsReceived.setType_publication((String) mapObj.get(Publication.PUBLICATION_TYPE_PUBLICATION));
                                itemsReceived.setNumCupos(Integer.parseInt((String) mapObj.get(Publication.PUBLICATION_NUMCUPOS)));
                                itemsReceived.setDate_start((String) mapObj.get(Publication.PUBLICATION_DATE_START));
                                itemsReceived.setDate_end((String) mapObj.get(Publication.PUBLICATION_DATE_END));
                                itemsReceived.setPoblationM(Boolean.parseBoolean((String) mapObj.get(Publication.PUBLICATION_POBLATIONM)));
                                itemsReceived.setPoblationF(Boolean.parseBoolean((String) mapObj.get(Publication.PUBLICATION_POBLATIONF)));
                                itemsReceived.setSubcategory((String) mapObj.get(Publication.PUBLICATION_SUBCATEGORY));

                                publications.add(itemsReceived);
                                Log.i("ITEM", "Data is" + itemsReceived.getName());
                                notifyDataSetChanged();
                            }catch (Exception e){
                                Log.i("xxxx", e.getMessage());
                            }

                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_pub, parent, false);
        PublicationViewHolder pvh = new PublicationViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PublicationViewHolder holder, int position) {
        holder.nombre.setText(publications.get(position).getName());
        //holder.image.setImageBitmap(publications.get(position).GET_IMAGEN_FROM_INTERNET);
        holder.description.setText(publications.get(position).getDescription());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    public static class PublicationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nombre;
        ImageView image;
        TextView description;

        public PublicationViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card);
            nombre = (TextView) itemView.findViewById(R.id.cardNombre);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
            description = (TextView) itemView.findViewById(R.id.cardDescription);
        }
    }
}
