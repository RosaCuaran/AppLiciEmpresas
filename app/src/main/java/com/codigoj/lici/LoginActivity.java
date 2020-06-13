package com.codigoj.lici;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codigoj.lici.data.AppPreferences;
import com.codigoj.lici.model.Administrator;
import com.codigoj.lici.model.Company;
import com.codigoj.lici.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private TextView title_login;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private Button btnLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    //Local data
    private Administrator administrador;
    private String id;
    AppPreferences appPreferences;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Ocultar la ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        progressDialog = new ProgressDialog(this);
        //Shared preferences
        appPreferences = new AppPreferences(this);
        //Database reference
        database = FirebaseDatabase.getInstance();
        //Load firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //Verify the state of session
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                progressDialog.setMessage(getString(R.string.verify_company));
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("EntroLogin-conuser", "El usuario existe");
                    id = user.getUid();
                    // start the administrador
                    administrador = new Administrator(id, context);
                    Log.d("user-id", user.getUid());
                    verifyDataServer(id);
                } else {
                    // User is signed out
                    appPreferences.getSharedPreferences().edit().clear().apply();
                    Log.d("EntroLogin-sinuser", "se debe remover los datos");
                }
            }
        };

        title_login = (TextView) findViewById(R.id.title_log_in);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputPassword = (EditText) findViewById(R.id.input_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        //Cargar tipos de letra
        Typeface berlinSansFB= Typeface.createFromAsset(getAssets(),"fonts/BRLNSR.TTF");
        title_login.setTypeface(berlinSansFB);
        inputEmail.setTypeface(berlinSansFB);
        inputPassword.setTypeface(berlinSansFB);
        btnLogin.setTypeface(berlinSansFB);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
    }

    /**
     * Method that verify that the company is in the database.
     * If not found the company, logout it automatically
     */
    private void verifyDataServer(String id) {
        Query ref = database.getReference().child(ProfileActivity.COMPANIES)
                .child(id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    Company c = dataSnapshot.getValue(Company.class);
                    boolean startTabs = false;
                    //Save the token for this company
                    String token = FirebaseInstanceId.getInstance().getToken();
                    Log.d("DeleteTokenServiceLogi", token);
                    dataSnapshot.getRef().child(Utils.REF_TOKEN).setValue(token);
                    //Verify the profile complete
                    if (dataSnapshot.getChildrenCount() >= 10){
                        loadProfileData(c);
                        //Start with the Activity TabsPublication
                        startTabs = true;
                        administrador.loadServerData(startTabs);

                    } else {
                        //Start with the Activity Profile
                        finish();
                        administrador.loadServerData(startTabs);
                    }
                }
                else {
                    // TODO: handle the case where the data does not yet exist
                    Toast.makeText(getApplicationContext(), "Empresa no encontrada.", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    appPreferences.cleanPreferences();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadProfileData(Company c) {
        appPreferences.saveDataString(ProfileActivity.KEY_ID, c.getId());
        appPreferences.saveDataString(ProfileActivity.KEY_PATH_IMAGE_LOCAL, c.getPath_image_local());
        appPreferences.saveDataString(ProfileActivity.KEY_PATH_IMAGE_REMOTE, c.getPath_image_remote());
        appPreferences.saveDataString(ProfileActivity.KEY_NAME, c.getName());
        appPreferences.saveDataString(ProfileActivity.KEY_DESCRIPTION, c.getDirection());
        appPreferences.saveDataString(ProfileActivity.KEY_DIRECTION, c.getDirection());
        appPreferences.saveDataString(ProfileActivity.KEY_LATITUD, String.valueOf(c.getLatitud()));
        appPreferences.saveDataString(ProfileActivity.KEY_LONGITUD, String.valueOf(c.getLongitud()));
        appPreferences.saveDataInt(ProfileActivity.KEY_CATEGORY, c.getCategory());
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * Validating form
     */
    private void submitForm() {


        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        //Toast.makeText(getApplicationContext(), "Thank You!", Toast.LENGTH_SHORT).show();
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(inputEmail.getText().toString().trim(), inputPassword.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful())
                        {
                            if (task.getException().getMessage().contains("A network error")){
                                Toast.makeText(getApplicationContext(), "Verifique su conexion a internet", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Datos invalidos", Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    }
                });
    }


    /**
     * Validar el correo ingresado
     * @return
     */
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validar el password ingresado
     * @return
     */
    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }
}
