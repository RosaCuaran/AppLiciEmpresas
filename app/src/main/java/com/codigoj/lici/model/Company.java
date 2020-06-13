package com.codigoj.lici.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by JHON on 17/02/2017.
 */

public class Company {

    private String id;
    private String email;
    private String path_image_local;
    private String path_image_remote;
    private String name;
    private String description;
    private String direction;
    private int category;
    private double longitud;
    private double latitud;
    private List<Publication> publicationList;
    private boolean validity;


    public Company() {
        publicationList = new ArrayList<>();
    }

    public Company(String id, String email, String path_image_local, String path_image_remote, String name, String description, String direction, int category, double longitud, double latitud, ArrayList<Publication> publicationList, boolean validity) {
        this.id = id;
        this.email = email;
        this.path_image_local = path_image_local;
        this.path_image_remote = path_image_remote;
        this.name = name;
        this.description = description;
        this.direction = direction;
        this.category = category;
        this.longitud = longitud;
        this.latitud = latitud;
        this.publicationList = new ArrayList<>();
        this.validity = validity;
    }

    public Company(String id, String email, String path_image_local, String name, String description, int category){
        this.id = id;
        this.email = email;
        this.path_image_local = path_image_local;
        this.name = name;
        this.description = description;
        this.category = category;
        this.publicationList = new ArrayList<>();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("email", email);
        result.put("path_image_local", path_image_local);
        result.put("path_image_remote", path_image_remote);
        result.put("name", name);
        result.put("description", description);
        result.put("direction", direction);
        result.put("category", category);
        result.put("longitud", longitud);
        result.put("latitud", latitud);
        result.put("publications", publicationList);

        return result;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", path_image_local='" + path_image_local + '\'' +
                ", path_image_remote='" + path_image_remote + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", direction='" + direction + '\'' +
                ", category=" + category +
                ", longitud=" + longitud +
                ", latitud=" + latitud +
                ", publicationList=" + publicationList +
                ", validity=" + validity +
                '}';
    }

    /**
     * Add the publication to the list
     * @param myPublication
     */
    public void addPublication(Publication myPublication){
        if ( publicationList == null){
            publicationList = new ArrayList<>();
        }
        publicationList.add(myPublication);
    }

    //--------------------------
    //GETTERS AND SETTERS
    //--------------------------

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPath_image_local() {
        return path_image_local;
    }

    public void setPath_image_local(String path_image_local) { this.path_image_local = path_image_local; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) { this.longitud = longitud; }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public int getCategory() { return category; }

    public void setCategory(int category) { this.category = category; }

    public String getPath_image_remote() { return path_image_remote; }

    public void setPath_image_remote(String path_image_remote) { this.path_image_remote = path_image_remote; }

    public List<Publication> getPublicationList() { return publicationList; }

    public void setPublicationList(ArrayList<Publication> publicationList) { this.publicationList = publicationList; }

    public boolean isValidity() { return validity; }

    public void setValidity(boolean validity) { this.validity = validity; }
}
