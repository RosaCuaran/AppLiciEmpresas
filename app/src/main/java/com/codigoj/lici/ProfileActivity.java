package com.codigoj.lici;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codigoj.lici.data.AppPreferences;
import com.codigoj.lici.model.Company;
import com.codigoj.lici.model.Publication;
import com.codigoj.lici.utils.DeleteTokenService;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements Validator.ValidationListener{

    //Constantes de clase
    private static final String TAG = Activity.class.getName();
    public static final String KEY_ID = "ID";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_PATH_IMAGE_LOCAL = "PATH_IMAGE_LOCAL";
    public static final String KEY_PATH_IMAGE_REMOTE = "PATH_IMAGE_REMOTE";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_DESCRIPTION = "DESCRIPTION";
    public static final String KEY_DIRECTION = "DIRECTION";
    public static final String KEY_LATITUD = "LATITUD";
    public static final String KEY_LONGITUD = "LONGITUD";
    public static final String KEY_CATEGORY = "CATEGORY";

    //Constant for the references
    public static final String COMPANIES = "companies";
    public static final String ADMINISTRATOR = "administrator";
    public static final String IMAGES = "images";
    public static final String PATH_IMAGE_REMOTE ="path_image_remote";
    public static final String PUBLICATIONS = "publications";

    //Variables de clase
    private LinearLayout view_profile;
    private CircleImageView photo_button;
    private Button saveData;
    private Button direction;
    private Spinner spCategory;
    @NotEmpty(messageResId = R.string.txtValidateNameCompany1)
    @Length(min = 2, max = 20, messageResId = R.string.txtValidateNameCompany2)
    private EditText companyName;
    @Length(min = 0, max = 250, messageResId = R.string.txtValidateDescriptionCompany)
    private EditText companyDescription;
    private TextView myprofile;
    //Magical camera
    private MagicalCamera magicalCamera;
    private int RESIZE_PHOTO_PIXELS_PERCENTAGE = 30;
    private PermissionGranted permissionGranted;
    private String path = "";
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference storage;
    private FirebaseDatabase database;
    //Local data
    private FirebaseUser user;
    //Is the id from user and the id from company
    private String id;
    private AppPreferences appPreferences;
    private Company company;
    private ProgressDialog progressDialog;
    // Validator
    private Validator validator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        permissionGranted = new PermissionGranted(this);
        myprofile = (TextView) findViewById(R.id.myprofile);
        direction = (Button) findViewById(R.id.btn_direction);
        photo_button = (CircleImageView) findViewById(R.id.photo_button);
        saveData = (Button) findViewById(R.id.btn_save_profile_info);
        view_profile = (LinearLayout) findViewById(R.id.view_profile);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        companyName = (EditText) findViewById(R.id.input_name);
        companyDescription = (EditText) findViewById(R.id.input_description);

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
            Log.d("devolver","Se devolvio, no encontro usuario");
            goLoginScreen();
        }else {
            user = firebaseAuth.getCurrentUser();
            id = user.getUid();
        }
        //SharedPreferences instance
        appPreferences = new AppPreferences(getApplicationContext());
        //Database reference
        database = FirebaseDatabase.getInstance();
        //Storage reference
        storage = FirebaseStorage.getInstance().getReference();
        //For the admin the images and the camera data
        magicalCamera = new MagicalCamera(this, RESIZE_PHOTO_PIXELS_PERCENTAGE, permissionGranted);
        //Cargar tipos de letra
        loadFont();
        //Verify that the server contains the info about this account
        loadProfile();
        progressDialog = new ProgressDialog(this);
        company = new Company();
        //Listener of picture, photo
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        //Listener for the auth
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null){
                    finish();
                    //Create a new token for the next user session
                    Intent intent = new Intent(ProfileActivity.this, DeleteTokenService.class);
                    startService(intent);
                    //Clean all preferences
                    appPreferences.cleanPreferences();
                    goLoginScreen();
                }
            }
        };

        //Listener for save the profile data
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLocateSaved())
                    validator.validate();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private boolean checkLocateSaved() {
        String direction = appPreferences.getDataString(KEY_DIRECTION, "No registra");
        double latitud = Double.parseDouble(appPreferences.getDataString(KEY_LATITUD, "0"));
        double longitud = Double.parseDouble(appPreferences.getDataString(KEY_LONGITUD, "0"));
        if (!direction.equals("No registra") && latitud != 0 && longitud != 0)
            return true;
        else
            Toast.makeText(this, "Debes registrar una ubicación de la empresa", Toast.LENGTH_SHORT).show();
            return false;
    }

    private void loadOptionsCamera() {
        final CharSequence[] options = {"Tomar foto", "Elegir de galeria"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Elige una opción");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    magicalCamera.takePhoto();
                }
                if (i == 1){
                    magicalCamera.selectedPicture("Continuar con...");
                }
                else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }


    private void uploadImage() {
        //Upload the profile image
        try {
            progressDialog.setMessage("Subiendo imagen...");
            progressDialog.setTitle("Sincronizando imagen");
            path = magicalCamera.savePhotoInMemoryDevice(magicalCamera.getPhoto(), "ProfilePhoto", MagicalCamera.JPEG, false);
            Uri uri = Uri.fromFile(new File(path));
            //LOCATE IN THE SERVER
            StorageReference server = storage.child(IMAGES).child(id).child(uri.getLastPathSegment());
            //Fill the company
            company.setPath_image_local(path);
            //Save the path_image_local in local data for to use later
            appPreferences.saveDataString(KEY_PATH_IMAGE_LOCAL, company.getPath_image_local());
            //Start the upload task
            server.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    //Save the path_image_remote in local data for to use later
                    appPreferences.saveDataString(KEY_PATH_IMAGE_REMOTE, downloadUri.toString());
                    company.setPath_image_remote(downloadUri.toString());
                    DatabaseReference myRef = database.getReference().child(COMPANIES).child(id);
                    myRef.child(PATH_IMAGE_REMOTE).setValue(company.getPath_image_remote());
                    Toast.makeText(ProfileActivity.this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.setMessage( (int)(taskSnapshot.getBytesTransferred() * 100 / taskSnapshot.getTotalByteCount())+"% enviados " + String.valueOf(taskSnapshot.getBytesTransferred()/1000)+" Kb de "+ taskSnapshot.getTotalByteCount()/1000 +" Kb" );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.setMessage(e.getMessage());
                    Toast.makeText(getApplicationContext(), getString(R.string.error_conection), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
            Log.d(TAG, "Error");
        }
    }

    public void uploadProfile() {
        try{
            DatabaseReference myRef = database.getReference().child(COMPANIES);
            Map<String, Object> values = company.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/"+id,values);
            myRef.updateChildren(childUpdates);
            progressDialog.dismiss();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Hubo un error compruebe su conexión", Toast.LENGTH_LONG);
            Log.d(TAG, e.getMessage());
        }
    }

    private void goLoginScreen() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", path);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        path = savedInstanceState.getString("file_path");
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
        RoundedBitmapDrawable roundedDrawable = null;
        if (resultCode != RESULT_OK){
            //Snackbar.make(view_profile, getString(R.string.not_selected),Snackbar.LENGTH_SHORT).show();
        }
        else if (magicalCamera.getPhoto()!= null) {
            //ONLY WORK WITH THE BACK CAMERA.
            if (requestCode == 0 && resultCode == RESULT_OK){
                // TAKE_PHOTO
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.ORIENTATION_ROTATE_90));
                    //Log.d(TAG, "entro en 23 para abajo");
                }
                else{
                    magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.ORIENTATION_ROTATE_NORMAL));
                    //Log.d(TAG, "entro en 24 o superior");
                }
            }
            else if (requestCode == 1 && resultCode == RESULT_OK)
            {
                //Code for SELECT_PHOTO
                //magicalCamera.setPhoto(magicalCamera.rotatePicture(magicalCamera.getPhoto(), MagicalCamera.NORMAL_CAMERA));
            }
            Bitmap originalBitmap =  magicalCamera.getPhoto();
            photo_button.setImageBitmap(originalBitmap);
        }
        else if (path != null && resultCode == RESULT_CANCELED) {
            Bitmap imagen = BitmapFactory.decodeFile(path);
            magicalCamera.setPhoto(magicalCamera.rotatePicture(imagen, MagicalCamera.NORMAL_CAMERA));
            //creamos el drawable redondeado
            roundedDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(), imagen);
            //asignamos el CornerRadius
            roundedDrawable.setCornerRadius(imagen.getHeight());
            photo_button.setBackground(null);
            photo_button.setImageDrawable(roundedDrawable);
        }
        else {
            photo_button.setBackgroundResource(R.drawable.profile_image);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //CALL THIS METHOD EVER IN THIS OVERRIDE FOR ACTIVATE PERMISSIONS
        magicalCamera.permissionGrant(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cerrar_sesion) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("INFORMACIÓN")
                    .setMessage("¿Deseas cerrar sesión?")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firebaseAuth.signOut();
                                    finish();
                                    appPreferences.cleanPreferences();
                                    startActivity(new Intent(ProfileActivity.this, IntroActivity.class));
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //--------------------
    //CODIGO A PROBAR
    //---------------------------
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().hasExtra("activity_preview")){
            String className = getIntent().getStringExtra("activity_preview");
            finish();
            try {
                startActivity(new Intent(this, Class.forName(className)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLocate(View v) {
        startActivity(new Intent(this, MapsActivity.class));
    }

    /**
     * Load de info registrered previously by the app in the device
     */
    public void loadProfile(){
        //Search the company
        DatabaseReference refcompany = database.getReference().child(COMPANIES).child(id);
        refcompany.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("datos", "los datos son:"+dataSnapshot.toString());
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() >= 10){
                    company = dataSnapshot.getValue(Company.class);
                    Log.d("company-validity", String.valueOf(company.isValidity()));
                    // Fill the data in the layout
                    Picasso.with(getApplicationContext()).load(company.getPath_image_remote()).memoryPolicy(MemoryPolicy.NO_CACHE).fit().centerCrop().into(photo_button);
                    companyName.setText(company.getName());
                    companyDescription.setText(company.getDescription());
                    spCategory.setSelection(company.getCategory());
                    for (DataSnapshot attribute : dataSnapshot.getChildren()){
                        Log.d("company", "attribute"+attribute.toString());
                        if (attribute.getKey().equals(PUBLICATIONS)){
                            for (DataSnapshot publication : attribute.getChildren()) {
                                Log.d("company", "publicacion"+publication.toString());
                                company.addPublication(publication.getValue(Publication.class));
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadFont() {
        Typeface berlinSansFB= Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        myprofile.setTypeface(berlinSansFB);
        companyName.setTypeface(berlinSansFB);
        companyDescription.setTypeface(berlinSansFB);
        direction.setTypeface(berlinSansFB);
        saveData.setTypeface(berlinSansFB);
    }

    //Validator method
    @Override
    public void onValidationSucceeded() {
        //Request the permission for the save
        permissionGranted.checkReadExternalPermission();
        permissionGranted.checkWriteExternalPermission();
        progressDialog.setMessage(getString(R.string.uploadingToServer));
        progressDialog.show();
        try {
            //fill the company
            String name = companyName.getText().toString().toUpperCase();
            String description = companyDescription.getText().toString();
            String email = firebaseAuth.getCurrentUser().getEmail();
            int cat = spCategory.getSelectedItemPosition();
            //Get the location from shared preferences.
            String direction = appPreferences.getDataString(KEY_DIRECTION, "No registra");
            double latitud = Double.parseDouble(appPreferences.getDataString(KEY_LATITUD, "0"));
            double longitud = Double.parseDouble(appPreferences.getDataString(KEY_LONGITUD, "0"));
            //Complete the data of entity
            company.setId(id);
            company.setEmail(email);
            company.setPath_image_local(path);
            company.setName(name);
            company.setDescription(description);
            company.setDirection(direction);
            company.setLatitud(latitud);
            company.setLongitud(longitud);
            company.setCategory(cat);
            appPreferences.saveDataString(KEY_ID, id);
            appPreferences.saveDataString(KEY_EMAIL, email);
            appPreferences.saveDataString(KEY_PATH_IMAGE_LOCAL, path);
            appPreferences.saveDataString(KEY_NAME, name);
            appPreferences.saveDataString(KEY_DESCRIPTION, description);
            appPreferences.saveDataInt(KEY_CATEGORY, cat);
            //Upload the data to server
            progressDialog.setMessage(getString(R.string.savingToServer));
            if(magicalCamera.getPhoto()!= null){
                uploadImage();
                uploadProfile();
            }
            else{
                uploadProfile();
            }
        }catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        //Get the new activity
        startActivity(new Intent(ProfileActivity.this, TabsPublication.class));
        finish();
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


    //----GETTER AND SETTERS------
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

}
