package com.codigoj.lici.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.codigoj.lici.ProfileActivity;

/**
 * Created by JHON on 27/02/2017.
 */

public class AppPreferences {

    public static final String TEMPORAL = "TEMPORAL";
    private static final String APP_SHARED_PREFERENCES = AppPreferences.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AppPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(APP_SHARED_PREFERENCES, Activity.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    /**
     * Saved data type String in the SharedPreferences
     * @param clave this is the key
     * @param valor this is the value to save
     */
    public void saveDataString(String clave, String valor){
        editor.putString(clave, valor);
        editor.commit();
    }

    /**
     * Saved data type Integer in the SharedPreferences
     * @param clave this is the key
     * @param valor this is the value to save
     */
    public void saveDataInt(String clave, int valor){
        editor.putInt(clave, valor);
        editor.commit();
    }

    /**
     * Get the data saved in the SharedPreferences
     * @param clave this is the key
     * @param valor this is the value default if the value doesn't exist
     * @return the value in String saved in the SharedPreferences
     */
    public String getDataString(String clave, String valor){
        return sharedPreferences.getString(clave, valor);
    }

    /**
     * Get the data saved in the SharedPreferences
     * @param clave this is the key
     * @param valor this is the value default if the value doesn't exist
     * @return the value in Integer saved in the SharedPreferences
     */
    public int getDataint(String clave, int valor){
        return sharedPreferences.getInt(clave, valor);
    }

    /**
     * Verify that the data is stored. NOT VERIFY KEY_DESCRIPTION, KEY_ID and KEY_EMAIL, KEY_CATEGORY
     * @return a String with the value to missing or "true" if all is ok
     */
    public String verifyData() {
        if (getDataString(ProfileActivity.KEY_PATH_IMAGE_LOCAL, "") == "" || getDataString(ProfileActivity.KEY_PATH_IMAGE_LOCAL, "") == null) {
            return "Debes usar una imágen de perfil";
        } else if (getDataString(ProfileActivity.KEY_PATH_IMAGE_REMOTE, "") == "" || getDataString(ProfileActivity.KEY_PATH_IMAGE_REMOTE, "") == null) {
            return "Debes usar una imágen de perfil2";
        } else if (getDataString(ProfileActivity.KEY_NAME, "") == "" || getDataString(ProfileActivity.KEY_NAME, "") == null) {
            return "Debes usar una nombre de empresa";
        } else if (getDataString(ProfileActivity.KEY_DIRECTION, "") == "" || getDataString(ProfileActivity.KEY_DIRECTION, "") == null) {
            return "Debes llenar una dirección de empresa";
        } else if (getDataString(ProfileActivity.KEY_LATITUD, "") == "" || getDataString(ProfileActivity.KEY_LATITUD, "") == null) {
            return "Por favor crea un marcador en el mapa la dirección de la empresa.LT";
        } else if (getDataString(ProfileActivity.KEY_LONGITUD, "") == "" || getDataString(ProfileActivity.KEY_LONGITUD, "") == null) {
            return "Por favor llena nuevamente la dirección de la empresa.LG";
        } else {
            return "true";
        }
    }

    /**
     * Verify that the data is stored. NOT VERIFY KEY_DESCRIPTION, KEY_ID and KEY_EMAIL, KEY_CATEGORY
     * @return a String with the value to missing or "true" if all is ok
     */
    public boolean existDataStored () {
        if (sharedPreferences.contains(ProfileActivity.KEY_PATH_IMAGE_LOCAL) ||
                sharedPreferences.contains(ProfileActivity.KEY_NAME) ||
                sharedPreferences.contains(ProfileActivity.KEY_DESCRIPTION) ||
                sharedPreferences.contains(ProfileActivity.KEY_CATEGORY) ||
                sharedPreferences.contains(ProfileActivity.KEY_DIRECTION) ||
                sharedPreferences.contains(ProfileActivity.KEY_LATITUD) ||
                sharedPreferences.contains(ProfileActivity.KEY_LONGITUD) ){
            return true;
        } else{
            return false;
        }
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
    public void cleanPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}
