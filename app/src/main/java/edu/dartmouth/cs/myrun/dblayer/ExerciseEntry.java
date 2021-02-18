package edu.dartmouth.cs.myrun.dblayer;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ExerciseEntry implements Parcelable {
    public static final String DEFAULT_NOT_SYNCED_CLOUD_KEY = "unknown";

    private long id;
    private String userEmail;
    private String cloudKey;
    private boolean boarded;
    private String inputType;
    private String activityType;
    private String dateStr;
    private String timeStr;
    private double duration;
    private double distance;
    private double climbed;
    private int calorie;
    private int heartbeat;
    private String comment;
    private ArrayList<LatLng> mLocationList;

    public ExerciseEntry() {

    }

    public ExerciseEntry (DataSnapshot snapshot) {
        HashMap<String, Object> entryHashMap = (HashMap<String, Object>)(snapshot.getValue());
        userEmail = (String) entryHashMap.get("userEmail");
        cloudKey = (String)entryHashMap.get("cloudKey");
        boarded = (Boolean)entryHashMap.get("boarded");
        inputType = (String) entryHashMap.get("inputType");
        activityType = (String) entryHashMap.get("activityType");
        dateStr = (String) entryHashMap.get("dateStr");
        timeStr = (String) entryHashMap.get("timeStr");
        duration = Double.parseDouble(entryHashMap.get("duration").toString());
        distance = Double.parseDouble(entryHashMap.get("distance").toString());
        climbed = Double.parseDouble(entryHashMap.get("climbed").toString());
        calorie = Integer.parseInt(entryHashMap.get("calorie").toString());
        heartbeat = Integer.parseInt(entryHashMap.get("heartbeat").toString());
        calorie = Integer.parseInt(entryHashMap.get("calorie").toString());
        comment = (String) entryHashMap.get("comment");
        ArrayList<HashMap> path = (ArrayList) entryHashMap.get("mLocationList");
        mLocationList = new ArrayList<>();
        if (path != null) {
            for (HashMap hm: path) {
                LatLng ll = new LatLng((Double) hm.get("latitude"), (Double) hm.get("longitude"));
                mLocationList.add(ll);
            }
        }
    }


    protected ExerciseEntry(Parcel in) {
        id = in.readLong();
        userEmail = in.readString();
        cloudKey = in.readString();
        boarded = in.readInt() != 0;
        inputType = in.readString();
        activityType = in.readString();
        dateStr = in.readString();
        timeStr = in.readString();
        duration = in.readDouble();
        distance = in.readDouble();
        climbed = in.readDouble();
        calorie = in.readInt();
        heartbeat = in.readInt();
        comment = in.readString();
        mLocationList = in.createTypedArrayList(LatLng.CREATOR);
    }

    public void setExerciseEntry(ExerciseEntry entry) {
        id = entry.getId();
        userEmail = entry.getUserEmail();
        cloudKey = entry.getCloudKey();
        boarded = entry.isBoarded();
        inputType = entry.getInputType();
        activityType = entry.getActivityType();
        dateStr = entry.getDateStr();
        timeStr = entry.getTimeStr();
        duration = entry.getDuration();
        distance = entry.getDistance();
        climbed = entry.getClimbed();
        calorie = entry.getCalorie();
        heartbeat = entry.getHeartbeat();
        comment = entry.getComment();
        mLocationList = entry.getmLocationList();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(userEmail);
        dest.writeString(cloudKey);
        dest.writeInt(boarded ? 1 : 0);
        dest.writeString(inputType);
        dest.writeString(activityType);
        dest.writeString(dateStr);
        dest.writeString(timeStr);
        dest.writeDouble(duration);
        dest.writeDouble(distance);
        dest.writeDouble(climbed);
        dest.writeInt(calorie);
        dest.writeInt(heartbeat);
        dest.writeString(comment);
        dest.writeTypedList(mLocationList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExerciseEntry> CREATOR = new Creator<ExerciseEntry>() {
        @Override
        public ExerciseEntry createFromParcel(Parcel in) {
            return new ExerciseEntry(in);
        }

        @Override
        public ExerciseEntry[] newArray(int size) {
            return new ExerciseEntry[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCloudKey() {
        return cloudKey;
    }

    public boolean isSynced() {
        return !cloudKey.equals(ExerciseEntry.DEFAULT_NOT_SYNCED_CLOUD_KEY);
    }

    public boolean isBoarded() {
        return boarded;
    }

    public String getInputType() {
        return inputType;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getDateStr() {
        return dateStr;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public double getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }

    public double getClimbed() {
        return climbed;
    }

    public int getCalorie() {
        return calorie;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public String getComment() {
        return comment;
    }


    public ArrayList<LatLng> getmLocationList() {
        return mLocationList;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setCloudKey(String cloudKey) {
        this.cloudKey = cloudKey;
    }

    public void setBoarded(boolean boarded) {
        this.boarded = boarded;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setClimbed(double climbed) {
        this.climbed = climbed;
    }


    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setmLocationList(ArrayList<LatLng> mLocationList) {
        this.mLocationList = mLocationList;
    }
}
