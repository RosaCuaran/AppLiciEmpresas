package com.codigoj.lici;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codigoj.lici.data.AppPreferences;
import com.codigoj.lici.model.Company;
import com.codigoj.lici.model.Publication;
import com.codigoj.lici.utils.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JHON on 20/02/2017.
 */

public class Fragment_Promotion extends Fragment {
    RecyclerView recyclerView;
    List<Publication> list;
    //Constant
    public static final String NAME_IMAGE_PUB = "image_publication.jpeg";
    //Constant for the references
    public static final String REF_PUBLICATIONS_BOARD_COMPANY = "publications_board_company";
    public static final String REF_COMPANIES = "companies";
    public static final String REF_PUBLICATIONS = "publications";
    public static final String REF_SEARCH = "search";
    public static final String REF_CATEGORY_PUBLICATIONS = "category_publications";

    //Local data
    private FirebaseUser user;
    private String id;
    private int category;
    private AppPreferences appPreferences;
    private Company company;
    private Publication publication;
    private ProgressDialog progressDialog;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private StorageReference storage;
    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Es la vista para cargar los eventos
        View view = inflater.inflate(R.layout.fragment_promotion, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager layout = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layout);
        //Mejora el rendimiento siempre y cuando el TAMAÑO NO CAMBIE
        //recyclerView.setHasFixedSize(true);

        //SharedPreferences instance
        appPreferences = new AppPreferences(getContext());
        //Database reference
        database = FirebaseDatabase.getInstance().getReference();
        //Storage reference
        storage = FirebaseStorage.getInstance().getReference();

        id = appPreferences.getDataString(ProfileActivity.KEY_ID, "");
        category = appPreferences.getDataint(ProfileActivity.KEY_CATEGORY, 0);

        //Utilizo el adaptador para el recyclerView
        list = new ArrayList();

        FirebaseRecyclerAdapter<Publication, Fragment_Event.PublicationViewHolder> adapter = new FirebaseRecyclerAdapter<Publication, Fragment_Event.PublicationViewHolder>(
                Publication.class,
                R.layout.cardview_pub,
                Fragment_Event.PublicationViewHolder.class,
                database.child(REF_PUBLICATIONS_BOARD_COMPANY).child(id).child(Publication.TYPE_PROMOTION)
        ) {
            @Override
            protected void populateViewHolder(Fragment_Event.PublicationViewHolder viewHolder, final Publication publication, final int position) {
                viewHolder.nombre.setText(publication.getName());
                viewHolder.description.setText(publication.getDescription());
                Picasso.with(getContext()).load(publication.getPath_image_pub()).fit().centerCrop().into(viewHolder.image);
                Typeface berlinSansFB= Typeface.createFromAsset(getActivity().getAssets(),"fonts/BRLNSR.TTF");
                viewHolder.nombre.setTypeface(berlinSansFB);
                viewHolder.description.setTypeface(berlinSansFB);
                viewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), DetailPublicationActivity.class);
                        i.putExtra(Utils.KEY_ID_PUBLICATION, publication.getId());
                        startActivity(i);
                    }
                });
                viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), NewPublication.class);
                        i.putExtra(Utils.KEY_ID_PUBLICATION, publication.getId());
                        startActivity(i);
                    }
                });
                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("INFORMACIÓN")
                                .setMessage("¿Deseas borrar la publicación?")
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                        //Ref for the image
                                        StorageReference ref = storage.child("images/" + id + "/" + publication.getId() +"/"+NAME_IMAGE_PUB );

                                        //Ref for the data in companies
                                        final DatabaseReference datac = database.child(REF_COMPANIES).child(id).child(REF_PUBLICATIONS).child(publication.getId());
                                        //Ref for the data in publications board company
                                        final DatabaseReference datapbc = database.child(REF_PUBLICATIONS_BOARD_COMPANY).child(id).child(Publication.TYPE_PROMOTION).child(publication.getId());
                                        //Ref for the data in publications
                                        final DatabaseReference datap = database.child(REF_PUBLICATIONS).child(publication.getId());
                                        //Ref for the data in category publications and associations to subcategories
                                        ArrayList<String> listSubCat = publication.getSubcategories();

                                        Map<String, Object> childUpdates = new HashMap<>();
                                        for (int i = 0; i < listSubCat.size(); i++) {
                                            childUpdates.put("/category_publications/"
                                                    + category + "/"
                                                    + Utils.REF_SUBCATEGORY + "/"
                                                    + listSubCat.get(i) + "/"
                                                    + Utils.REF_PUBLICATIONS + "/"
                                                    + publication.getId(), null);
                                        }
                                        final Map<String, Object> deleteSubCategories = childUpdates;
                                        final DatabaseReference datacp = database.child(REF_CATEGORY_PUBLICATIONS).child(String.valueOf(category)).child(publication.getId());
                                        //Ref for the data in search
                                        final DatabaseReference datas = database.child(REF_SEARCH).child(publication.getId());
                                        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                datac.removeValue();
                                                datapbc.removeValue();
                                                datap.removeValue();
                                                datacp.removeValue();
                                                datas.removeValue();
                                                database.updateChildren(deleteSubCategories);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "Error al eliminar, verifique su conexión", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton("CANCELAR",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Permanece en la activiy
                                    }
                                });
                        builder.create();
                        builder.show();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
        return view;
    }

    public static class PublicationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nombre;
        ImageView image;
        TextView description;
        ImageButton edit;
        ImageButton delete;

        public PublicationViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card);
            nombre = (TextView) itemView.findViewById(R.id.cardNombre);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
            description = (TextView) itemView.findViewById(R.id.cardDescription);
            edit = (ImageButton) itemView.findViewById(R.id.btn_edit_publication);
            delete = (ImageButton) itemView.findViewById(R.id.btn_delete_publication);
        }
    }
}