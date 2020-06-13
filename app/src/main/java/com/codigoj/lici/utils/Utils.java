package com.codigoj.lici.utils;

/**
 * Created by Jhon on 23/06/17.
 * This class is used for the order the global constantes
 */
public class Utils {

    //References of first level of the database in firebase
    public static final String REF_CATEGORY_PUBLICATIONS = "category_publications";
    public static final String REF_COMPANIES = "companies";
    public static final String REF_PUBLICATIONS = "publications";
    public static final String REF_USERS = "users";
    public static final String REF_NOTIFICATION_REQUEST_COMPANY = "notification_request_company";
    //References of second and third level of the database in firebase
    public static final String REF_NAME = "name";
    public static final String REF_TOKEN = "token";
    public static final String REF_NAME_COMPANY = "name_company";
    public static final String REF_CATEGORY = "category";
    public static final String REF_SUBCATEGORY = "subcategory";
    public static final String REF_VALIDITY = "validity";
    public static final String REF_COUPONS = "coupons";
    public static final String REF_TYPE = "type";
    public static final String REF_ID_USER = "id_user";

    //References of static values
    public static final String REF_AVAILABLE = "available";
    public static final String REF_RESERVED = "reserved";

    //Keys for the Shared preferences
    public static final String KEY_ID_USER = "id_user";
    public static final String KEY_ID_PUBLICATION = "id_publication";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_SUBCATEGORY = "subcategory";

    //State of button
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_INACTIVE = 0;

}