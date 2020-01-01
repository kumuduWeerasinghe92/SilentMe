package com.planit.planit.utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.author;
import static android.R.attr.required;
import static com.planit.planit.R.string.location;

/**
 * Created by HP on 26-Jun-17.
 */

public class Event {

    @Exclude
    private String key;
    private String name;
    private String date;
    private String enddate;
    private String time;
    private String endtime;
    private String location;
    private String about;
    private String latitude;
    private String longitude;
    private String area;
    private String ispublic;
    private String issilent;
    private Map<String, Message> chat;
    private Map<String, Item> foodAndDrinks;
    private Map<String, Item> equipment;
    private Map<String, Item> playlist;
    private HashMap<String, Boolean> invited;
    private HashMap<String, Boolean> hosted;

    public Event(){
    // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Event(String name, String date,String enddate, String time,String endtime, String location, String about, String userCreator, String latitude, String longitude, String area, String ispublic, String issilent){
        this.name = name;
        this.date = date;
        this.enddate = enddate;
        this.time = time;
        this.endtime = endtime;
        this.location = location;
        this.about = about;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.ispublic = ispublic;
        this.issilent = issilent;

        this.chat = new HashMap<>();;
        this.foodAndDrinks = new HashMap<>();
        this.equipment = new HashMap<>();
        this.playlist = new HashMap<>();
        this.invited = new HashMap<>();
        this.hosted = new HashMap<>();
        this.hosted.put(userCreator, true);
    }

    public void setEvent(Event other)
    {
        this.name = other.name;
        this.location = other.location;
        this.date = other.date;
        this.enddate = other.enddate;
        this.time = other.time;
        this.endtime = other.endtime;
        this.about = other.about;
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.area = other.area;
        this.ispublic = other.ispublic;
        this.issilent = other.issilent;

        this.chat = other.chat;;
        this.foodAndDrinks = other.foodAndDrinks;
        this.equipment = other.equipment;
        this.playlist = other.playlist;
        this.invited = other.invited;
        this.hosted = other.hosted;
    }

    public void addHost(User user)
    {
        if (hosted == null)
        {
            this.hosted = new HashMap<>();
        }
        this.hosted.put(user.getPhoneNumber(), true);
    }

    public void addInvited(User user)
    {
        if (invited == null)
        {
            this.invited = new HashMap<>();
        }
        this.invited.put(user.getPhoneNumber(), true);
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String newKey) { this.key = newKey; }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getendDate() {
        return enddate;
    }

    public String getTime() {
        return time;
    }

    public String getendTime() {
        return endtime;
    }

    public String getLocation() {
        return location;
    }

    public String getAbout() {
        return about;
    }

    public String getLatitude() {
        return latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public String getArea() {
        return area;
    }

    public String getIspublic() {
        return ispublic;
    }

    public String getIssilent() {
        return issilent;
    }
    public Map<String, Message> getChat() {
        return chat;
    }

    public Map<String, Item> getFoodAndDrinks() {
        return foodAndDrinks;
    }

    public Map<String, Item> getEquipment() {
        return equipment;
    }

    public Map<String, Item> getPlaylist() {
        return playlist;
    }

    public HashMap<String, Boolean> getInvited() {
        return invited;
    }

    public void setInvited(HashMap<String, Boolean> newInvited)
    {
        this.invited = newInvited;
    }

    public HashMap<String, Boolean> getHosted() {
        return hosted;
    }

    public void setHosted(HashMap<String, Boolean> newHosted)
    {
        this.hosted = newHosted;
    }

    public Map<String, Object> toMapBaseEventInfoTable() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("date", this.date);
        result.put("enddate", this.enddate);
        result.put("time", this.time);
        result.put("endtime", this.endtime);
        result.put("location", this.location);
        result.put("about", this.about);
        result.put("latitude", this.latitude);
        result.put("longitude", this.longitude);
        result.put("area", this.area);
        result.put("ispublic", this.ispublic);
        result.put("issilent", this.issilent);

        result.put("chat", this.chat);
        result.put("foodAndDrinks", this.foodAndDrinks);
        result.put("equipment", this.equipment);
        result.put("playlist", this.playlist);

        return result;
    }

    public Map<String, Object> toMapBaseEventUsers() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("invited", this.invited);
        result.put("hosted", this.hosted);

        return result;
    }

}
