package com.codigoj.lici;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.codigoj.lici.data.AppPreferences;
import com.codigoj.lici.model.Company;
import com.codigoj.lici.model.Coupon;
import com.codigoj.lici.model.Publication;
import com.codigoj.lici.utils.Utils;
import com.frosquivel.magicalcamera.Functionallities.PermissionGranted;
import com.frosquivel.magicalcamera.MagicalCamera;
import com.frosquivel.magicalcamera.Objects.PermissionGrantedObject;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Future;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewPublication extends AppCompatActivity implements Validator.ValidationListener {

    //Constant
    final static int PROMOTION = 0;
    final static int EVENT = 1;
    private static final String TAG = Activity.class.getName();
    private final Calendar calendar = Calendar.getInstance();
    //Constant for the references
    public static final String IMAGES = "images";
    public static final String PUBLICATIONS = "publications";


    //Attribute of class
    private NumberPicker numberPicker;
    //State mode if state_edit is true the mode edition is enable, else is disable
    private Boolean state_edit;
    //This mode let to know if the gallery or camera have been opened
    private Boolean modeGalleryCamera;
    @NotEmpty(messageResId = R.string.txtValidateNameCompany1)
    @Length(min = 2, max = 20, messageResId = R.string.txtValidateNameCompany2)
    private EditText name_pub;
    @Length(min = 0, max = 50, messageResId = R.string.txtValidateDescriptionPublication)
    private EditText description_pub;
    @NotEmpty
    @Future(messageResId = R.string.txtValidateDateEnd, dateFormatResId = R.string.formatDate)
    private EditText date_end;
    private CheckBox checkBoxM;
    private CheckBox checkBoxF;
    private ImageView imagePub;
    private Spinner spType;
    private LinearLayout layout_cupos;
    private DatePickerDialog datePickerDialog;
    private Toolbar toolbar;
    private ListView listview;

    //Magical camera
    private MagicalCamera magicalCamera;
    private int RESIZE_PHOTO_PIXELS_PERCENTAGE = 30;
    private PermissionGranted permissionGranted;
    private String path = "";
    //Firebase
    private FirebaseAuth firebaseAuth;
    private StorageReference storage;
    private FirebaseDatabase database;
    //Local data
    private FirebaseUser user;
    private String id_company;
    private int cantidad_de_pub;
    private AppPreferences appPreferences;
    private Company company;
    private Publication publication;
    private ProgressDialog progressDialog;
    // Validator
    private Validator validator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_publication);
        permissionGranted = new PermissionGranted(this);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        imagePub = (ImageView) findViewById(R.id.image_new_pub);
        name_pub = (EditText) findViewById(R.id.name_new_pub);
        date_end = (EditText) findViewById(R.id.datePicker);
        description_pub = (EditText) findViewById(R.id.description_new_pub);
        checkBoxM = (CheckBox) findViewById(R.id.checkboxM);
        checkBoxF = (CheckBox) findViewById(R.id.checkboxF);
        spType = (Spinner) findViewById(R.id.spinner_new_pub);
        layout_cupos = (LinearLayout) findViewById(R.id.layout_cupos);
        toolbar = (Toolbar) findViewById(R.id.toolbar_new_pub);
        listview = (ListView) findViewById(R.id.list_subcat);
        //Management of permission
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            permissionGranted.checkAllMagicalCameraPermission();
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //Management of permission
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            permissionGranted.checkAllMagicalCameraPermission();
        }
        //Load firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //Verify the user count
        if (firebaseAuth.getCurrentUser() == null){
            //Comienza con la Actividad TabsPublicaciones
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }else {
            user = firebaseAuth.getCurrentUser();
            id_company = user.getUid();
        }
        //SharedPreferences instance
        appPreferences = new AppPreferences(getApplicationContext());

        //Database reference
        database = FirebaseDatabase.getInstance();
        //Storage reference
        storage = FirebaseStorage.getInstance().getReference();

        validator = new Validator(this);
        validator.setValidationListener(this);
        //Initial values
        numberPicker.setMaxValue(50);
        numberPicker.setMinValue(0);
        numberPicker.setValue(1);
        numberPicker.setWrapSelectorWheel(false);
        progressDialog = new ProgressDialog(this);
        company = new Company();
        publication = new Publication();
        magicalCamera = new MagicalCamera(this, RESIZE_PHOTO_PIXELS_PERCENTAGE, permissionGranted);
        imagePub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23){
                    permissionGranted.checkCameraPermission();
                    permissionGranted.checkReadExternalPermission();
                    permissionGranted.checkWriteExternalPermission();
                    PermissionGrantedObject p = permissionGranted.getPermissionGrantedObject();
                    if (p.isCameraPermission() && p.isReadExternalStoragePermission() && p.isWriteExternalStoragePermission()){
                        loadOptionsCamera();
                    }
                } else{
                    loadOptionsCamera();
                }
            }
        });
        //Spinner control the visibility of layout
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    layout_cupos.setVisibility(View.VISIBLE);
                }
                else {
                    layout_cupos.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                spType.setPrompt("Seleccione ...");
            }
        });
        //Datepicker option
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };
        date_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(NewPublication.this,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //Desactivate the state of edition from publication
        state_edit = false;
        modeGalleryCamera = false;

        //Cargar tipos de letra
        loadFonts();
        //Load the subcategory
        int catIndex = appPreferences.getDataint(ProfileActivity.KEY_CATEGORY, 0);
        String name = appPreferences.getDataString(ProfileActivity.KEY_NAME, "");
        company.setCategory(catIndex);
        company.setName(name);
        TypedArray array_subcategory = getResources().obtainTypedArray(R.array.array_subcategory);
        CharSequence[] subcategory = array_subcategory.getTextArray(catIndex);
        //modify the size of listview
        array_subcategory.recycle();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_checked, subcategory);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setAdapter(adapter);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        //Value for test, it can be wrong
        int height = 160;
        LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams((int)(width),(int)(height * subcategory.length));
        listview.setLayoutParams(mParam);
        //load the data for edit the publication
        if (getIntent().getExtras() != null){
            if(getIntent().getExtras().containsKey("id_publication")){
                state_edit = true;
                String data = getIntent().getExtras().getString("id_publication");
                loadPublicationSaved(data);
            }
            if(getIntent().getExtras().containsKey("cantidad_pub")){
                cantidad_de_pub = getIntent().getExtras().getInt("cantidad_pub");
            }
        }
    }

    private void loadOptionsCamera() {
        final CharSequence[] options = {"Tomar foto", "Elegir de galeria"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(NewPublication.this);
        builder.setTitle("Elige una opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    magicalCamera.takePhoto();
                    modeGalleryCamera = true;
                }
                if (i == 1){
                    magicalCamera.selectedPicture("Continuar con...");
                    modeGalleryCamera = true;
                }
                else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void loadFonts() {
        Typeface berlinSansFB= Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        name_pub.setTypeface(berlinSansFB);
        date_end.setTypeface(berlinSansFB);
        description_pub.setTypeface(berlinSansFB);
        checkBoxF.setTypeface(berlinSansFB);
        checkBoxM.setTypeface(berlinSansFB);
        description_pub.setTypeface(berlinSansFB);
        description_pub.setTypeface(berlinSansFB);
    }

    /**
     * Method that load the data for the publication sent like parameter
     * @param id_pub
     */
    private void loadPublicationSaved(String id_pub) {
        Query ref = database.getReference().child(PUBLICATIONS)
                .child(id_pub);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Publication pubLoaded = dataSnapshot.getValue(Publication.class);
                Picasso.with(getApplicationContext()).load(pubLoaded.getPath_image_pub()).memoryPolicy(MemoryPolicy.NO_CACHE).fit().centerCrop().into(imagePub);
                path = pubLoaded.getPath_image_pub();
                name_pub.setText(pubLoaded.getName());
                spType.setEnabled(false);
                if (pubLoaded.getType_publication().equals(Publication.TYPE_PROMOTION)){
                    spType.setSelection(0);
                } else{
                    spType.setSelection(1);
                }
                //Load the coupons
                if (dataSnapshot.hasChild(Utils.REF_COUPONS)){
                    for (DataSnapshot attributes: dataSnapshot.getChildren()){
                        if (attributes.getKey().equals(Utils.REF_COUPONS)){
                            for (DataSnapshot coupons : attributes.getChildren()){
                                Coupon c = coupons.getValue(Coupon.class);
                                pubLoaded.addCoupon(c);
                            }
                            break;
                        }
                    }
                }
                date_end.setText(pubLoaded.getDate_end());
                description_pub.setText(pubLoaded.getDescription());
                checkBoxF.setChecked(pubLoaded.isPoblationF());
                checkBoxM.setChecked(pubLoaded.isPoblationM());
                numberPicker.setEnabled(false);
                numberPicker.setValue(pubLoaded.getNumCupos());
                if (pubLoaded.getSubcategory().length() > 1){
                    String sub[] = pubLoaded.getSubcategory().split(",");
                    for (int i=0;i<sub.length;i++){
                        listview.setItemChecked(Integer.parseInt(sub[i]),true);
                    }
                }
                else{
                    listview.setItemChecked(Integer.parseInt(pubLoaded.getSubcategory()),true);
                }
                publication = pubLoaded;
                Log.d("pubload", "subcat:"+publication.getSubcategory());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        magicalCamera.resultPhoto(requestCode, resultCode, data);
        //Toast.makeText(this, " Requestcode is: "+ requestCode +"ResultCode is : "+resultCode, Toast.LENGTH_LONG).show();
        if (resultCode != RESULT_OK){
            //Snackbar.make(card, getString(R.string.not_selected),Snackbar.LENGTH_SHORT).show();
        }
        else if (magicalCamera.getPhoto()!= null) {
            //ONLY WORK WITH THE BACK CAMERA.
            if (requestCode == 0 && resultCode == RESULT_OK){
                magicalCamera.initImageInformation();
                if( magicalCamera.getPrivateInformation().getOrientation().contentEquals("1")) {
                    //Landscape
                    magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.ORIENTATION_ROTATE_NORMAL ));
                }
                else if (magicalCamera.getPrivateInformation().getOrientation().contentEquals("6")) {
                    //Portrait
                    // TAKE_PHOTO
                    if (android.os.Build.VERSION.SDK_INT < 23) {
                        magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.ORIENTATION_ROTATE_90));
                    }
                    else{
                        magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.ORIENTATION_ROTATE_NORMAL));
                    }
                }
            }
            else if (requestCode == 1 && resultCode == RESULT_OK) {
                //Code for SELECT_PHOTO
                magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.NORMAL_CAMERA));
            }
            permissionGranted.checkWriteExternalPermission();
            permissionGranted.checkReadExternalPermission();
            if (path != null){
                Picasso.with(getApplicationContext()).invalidate(path);
                Log.d(TAG, "Path es:"+path);
            }
            path = magicalCamera.savePhotoInMemoryDevice(magicalCamera.getPhoto(), "image_publication", MagicalCamera.JPEG, false);
            Picasso.with(getApplicationContext()).load(new File(path)).memoryPolicy(MemoryPolicy.NO_CACHE).fit().centerCrop().into(imagePub);
        }
        else if (path != null && resultCode == RESULT_CANCELED) {
            /*code by Jhon Martinez email: jhonmacbilly@gmail.com*/
            Picasso.with(getApplicationContext()).load(new File(path)).fit().centerCrop().into(imagePub);
        }
        else {
            imagePub.setBackgroundResource(R.drawable.profile_image);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path_pub", path);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        path = savedInstanceState.getString("file_path_pub");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //When the activity is in pause and edition mode , this activity get out.
        if (state_edit && !modeGalleryCamera){
            finish();
        }
    }

    private void uploadAll() {
        //Upload the profile image and data
        progressDialog.setMessage("Subiendo imagen...");
        progressDialog.setTitle("Guardando publicación");
        progressDialog.show();

        // Reference to info in server
        DatabaseReference myRef = database.getReference();

        // Check the status, if it is new, the data is stored normally, the system creates and stored cuopons,
        // finally the number of publications is reduced in 1,
        // otherwise the data is saved taking into account the modification made
        if (!state_edit) {
            storeInDatabase(myRef);
        } else {
            // path is the variable what save the info about image localization in internal storage
            // When is a edition of publication the data is stored as follows:
            // if path contains "https" or "token" means the user didn't select a image or didn't take other image, therefore, only the other data will be saved,
            // but if the user selected or took another image, save normally.
            // Both ways, the system does not create and store coupons, nor the number of publications is reduced
            if (path.contains("https") || path.contains("token")){
                //Fill the company
                //All data in POJO publication
                Log.d("variable","Entro1");
                Map<String, Object> values = publication.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/companies/" + id_company + "/publications/" + publication.getId(), values);
                childUpdates.put("/publications_board_company/" + id_company + "/" + publication.getType_publication() + "/" + publication.getId(), values);
                //Save the subcategories associate to publication
                ArrayList<String> listSubCat = publication.getSubcategories();
                TypedArray array = getResources().obtainTypedArray(R.array.pref_subcategories);
                for (int i = 0; i < listSubCat.size(); i++) {
                    childUpdates.put("/category_publications/"
                            + company.getCategory() + "/"
                            + Utils.REF_SUBCATEGORY + "/"
                            + listSubCat.get(i) + "/"
                            + Utils.REF_PUBLICATIONS + "/"
                            + publication.getId(), true);

                    //The next lines update or create the subcategory name
                    int idarray = array.getResourceId(company.getCategory(),Integer.parseInt(listSubCat.get(i)));
                    childUpdates.put("/category_publications/"
                            + company.getCategory() + "/"
                            + Utils.REF_SUBCATEGORY + "/"
                            + listSubCat.get(i) + "/name",getResources().getStringArray(idarray)[Integer.parseInt(listSubCat.get(i))]);
                }
                array.recycle();
                childUpdates.put("/publications/" + publication.getId(), values);
                HashMap<String, Object> search = new HashMap<>();
                search.put("name_publication", publication.getName());
                search.put("name_category", getResources().getStringArray(R.array.pref_category_profile_company_titles)[company.getCategory()]);
                search.put("name_company", company.getName());
                search.put("id_company", id_company);
                childUpdates.put("/search/" + publication.getId(), search);
                //Update the publication info
                final DatabaseReference ref = myRef;
                myRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        //Save the same info for the coupons
                        Map<String, Object> childCoupons = new HashMap<>();
                        for (Coupon c : publication.getListCoupons()){
                            childCoupons.put("/publications/" + publication.getId() + "/coupons/" + c.getId_coupon(), c.toMap());
                        }
                        ref.updateChildren(childCoupons);
                    }
                });
                progressDialog.dismiss();
                Toast.makeText(NewPublication.this, "Datos guardados", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d("variable","Entro2");
                storeInDatabase(myRef);
            }
        }

    }

    private void storeInDatabase(final DatabaseReference myRef){
        if (!state_edit){
            publication.setId(myRef.push().getKey());
        }
        Uri uri = Uri.fromFile(new File(path));
        //LOCATE IN THE SERVER
        StorageReference server = storage.child(IMAGES).child(id_company).child(publication.getId()).child(uri.getLastPathSegment());
        //Start the upload task
        server.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                //Fill the company
                publication.setPath_image_pub(downloadUri.toString());
                //All data in POJO publication
                Map<String, Object> values = publication.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/companies/" + id_company + "/publications/" + publication.getId(), values);
                childUpdates.put("/publications_board_company/" + id_company + "/" + publication.getType_publication() + "/" + publication.getId(), values);
                //Save the subcategories associate to publication
                ArrayList<String> listSubCat = publication.getSubcategories();
                for (int i = 0; i < listSubCat.size(); i++) {
                    childUpdates.put("/category_publications/"
                            + company.getCategory() + "/"
                            + Utils.REF_SUBCATEGORY + "/"
                            + listSubCat.get(i) + "/"
                            + Utils.REF_PUBLICATIONS + "/"
                            + publication.getId(), true);
                    //The next lines update or create the subcategory name
                    TypedArray array = getResources().obtainTypedArray(R.array.pref_subcategories);
                    int idarray = array.getResourceId(company.getCategory(), Integer.parseInt(listSubCat.get(i)));
                    array.recycle();
                    childUpdates.put("/category_publications/"
                            + company.getCategory() + "/"
                            + Utils.REF_SUBCATEGORY + "/"
                            + listSubCat.get(i) + "/name",getResources().getStringArray(idarray)[Integer.parseInt(listSubCat.get(i))]);
                }
                //The next line update or create the category name
                childUpdates.put("/category_publications/" + company.getCategory()+ "/name", getResources().getStringArray(R.array.pref_category)[company.getCategory()]);
                childUpdates.put("/publications/" + publication.getId(), values);
                if (!state_edit) {
                    childUpdates.put("/administrator/" + id_company + "/num_Publications", String.valueOf(cantidad_de_pub - 1));
                }
                HashMap<String, Object> search = new HashMap<>();
                search.put("name_publication", publication.getName());
                search.put("name_category", getResources().getStringArray(R.array.pref_category_profile_company_titles)[company.getCategory()]);
                search.put("name_company", company.getName());
                search.put("id_company", id_company);
                childUpdates.put("/search/" + publication.getId(), search);
                //Update the publication info
                myRef.updateChildren(childUpdates);
                if (!state_edit) {
                    createCuopons();
                } else {
                    //Save the same info for the coupons
                    Map<String, Object> childCoupons = new HashMap<>();
                    for (Coupon c : publication.getListCoupons()){
                        childCoupons.put("/publications/" + publication.getId() + "/coupons/" + c.getId_coupon(), c.toMap());
                    }
                    myRef.updateChildren(childCoupons);
                }
                //Finished the publication creation, then activate notification to users
                sendNotification();
                Toast.makeText(NewPublication.this, "Datos guardados", Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage((int) (taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount()) + "% enviados " + String.valueOf(taskSnapshot.getBytesTransferred() / 1000) + " Kb de " + taskSnapshot.getTotalByteCount() / 1000 + " Kb");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.error_conection), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method publish a new notification request, the server take the request and send the notifications
     * to all users with notifications activated for the company selected
     */
    private void sendNotification() {
        DatabaseReference myRef = database.getReference().child(Utils.REF_NOTIFICATION_REQUEST_COMPANY);
        String idNotification = myRef.push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        HashMap<String, Object> notification = new HashMap<>();
        //Data for the notification
        notification.put("id_company",id_company);
        notification.put("message","Hay una nueva publicación que puede interesarte.");
        notification.put("title","Lici Manager");
        childUpdates.put("/"+idNotification, notification);
        myRef.updateChildren(childUpdates);
    }

    /**
     * Fill the list of coupons
     */
    private void createCuopons() {
        if (publication.getNumCupos()!=0){
            DatabaseReference myRef = database.getReference();
            Map<String, Object> childUpdates = new HashMap<>();
            for (int i = 1; i <= publication.getNumCupos();i++){
                String idCoupon = myRef.push().getKey();
                //Fill the coupons like available, without user
                Coupon c = new Coupon();
                c.setType(Coupon.AVAILABLE);
                c.setId_coupon(idCoupon);
                Map<String, Object> values = c.toMap();
                childUpdates.put("/publications/" + publication.getId() + "/coupons/" + c.getId_coupon(), values);
                publication.addCoupon(c);
            }
            //Update the publication info
            myRef.updateChildren(childUpdates);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //CALL THIS METHOD EVER IN THIS OVERRIDE FOR ACTIVATE PERMISSIONS
        magicalCamera.permissionGrant(requestCode, permissions, grantResults);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        date_end.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_pub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int option = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (option == R.id.save_new_pub) {
            //Validations
            if (!path.isEmpty())
                validator.validate();   
            else
                Toast.makeText(this, "La publicación debe tener una imagen", Toast.LENGTH_SHORT).show();
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onValidationSucceeded() {
        String name = name_pub.getText().toString().toUpperCase();
        String date = date_end.getText().toString();
        String description = description_pub.getText().toString();
        boolean man = checkBoxM.isChecked();
        boolean women = checkBoxF.isChecked();
        SparseBooleanArray checked = listview.getCheckedItemPositions();
        String subcategorySelected = "";
        for (int i = 0; i < checked.size(); i++) {
            if(checked.valueAt(i) == true) {
                subcategorySelected += checked.keyAt(i)+",";
                //Log.i("xxxx", i + " " + listview.getItemAtPosition(checked.keyAt(i)).toString());
            }
        }
        //Log.i("xxxx"," " + subcategorySelected);
        if (subcategorySelected.length() > 0){
            subcategorySelected = subcategorySelected.substring(0,subcategorySelected.length()-1);
        }
        //Fill the publication
        //si son iguales significa que no hay cambios
        //si no son iguales significa que el usuario cambio la imagen
        if (state_edit){
            if (!publication.getPath_image_pub().equals(path)){
                publication.setPath_image_pub(path);
            }
            Log.d("subcategory-old", publication.getSubcategory());
            Log.d("subcategory-new", subcategorySelected);
            if (!publication.getSubcategory().equals(subcategorySelected)){
                ArrayList<String> listSubCat = publication.getSubcategories();
                // Reference to info in server
                DatabaseReference myRef = database.getReference();
                Map<String, Object> childUpdates = new HashMap<>();
                for (int i = 0; i < listSubCat.size(); i++) {
                    childUpdates.put("/category_publications/"
                            + company.getCategory() + "/"
                            + Utils.REF_SUBCATEGORY + "/"
                            + listSubCat.get(i) + "/"
                            + Utils.REF_PUBLICATIONS + "/"
                            + publication.getId(), null);
                }
                myRef.updateChildren(childUpdates);
            }
        }else{
            publication.setPath_image_pub(path);
        }
        publication.setName(name);
        publication.setDescription(description);
        publication.setDate_end(date);
        publication.setName_company(company.getName());
        publication.setId_company(company.getId());
        publication.setPoblationM(man);
        publication.setPoblationF(women);
        publication.setSubcategory(subcategorySelected);

        if (spType.getSelectedItemPosition() == PROMOTION){
            //Connect with the server create the promotion
            int cupo = numberPicker.getValue();
            publication.setNumCupos(cupo);
            publication.setType_publication(Publication.TYPE_PROMOTION);
            //if cupo == 0
            //Indeterminate coupons, fill the list from the the users.
        }
        if (spType.getSelectedItemPosition() == EVENT){
            //Connect with the server create the event
            publication.setType_publication(Publication.TYPE_EVENT);
        }

        //Update the image and preview data
        uploadAll( );
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }

    }
}
