package be.thomasmore.rott.data;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by koenv on 11-12-2016.
 */
public class People implements Serializable {

    private long id;

    private String name;
    private String birthYear;
    private String eyeColor;
    private String gender;
    private String hairColor;
    private String height;
    private String mass;
    private String skinColor;
    private String url;
    private String edited;
    private String species;

    private long homeworldId;
    private Planet homeworld;

    List<Long> speciesIds;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(String skinColor) {
        this.skinColor = skinColor;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEdited() {
        return edited;
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    public long getHomeworldId() {
        return homeworldId;
    }

    public void setHomeworldId(long homeworldId) {
        this.homeworldId = homeworldId;
    }

    public Planet getHomeworld() {
        return homeworld;
    }

    public void setHomeworld(Planet homeworld) {
        this.homeworld = homeworld;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<Long> getSpeciesIds() {
        if (speciesIds == null) {
            speciesIds = new ArrayList<>();

            try {
                JSONArray speciesJSON = new JSONArray(species);

                for (int i = 0; i < speciesJSON.length(); i++) {
                    String r = speciesJSON.getString(i);

                    try {
                        String[] urlparts = r.split("/");
                        speciesIds.add(Long.parseLong(urlparts[urlparts.length - 1]));
                    } catch (Exception e) {}
                }
            } catch (JSONException e) {}
        }

        return speciesIds;
    }

    @Override
    public String toString() {
        return name;
    }
}
