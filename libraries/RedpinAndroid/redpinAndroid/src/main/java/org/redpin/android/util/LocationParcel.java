package org.redpin.android.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by louismagarshack on 12/10/14.
 */
public class LocationParcel implements Parcelable {

    private String mapName;
    private String mapURL;
    private int mapXcord;
    private int mapYcord;
    private int accuracy;
    private String symbolicID;
    private int id;

    public LocationParcel(String mapName, String mapURL,
                          int mapXcord, int mapYcord, int accuracy,
                          String symbolicID, int id) {
        this.mapName = mapName;
        this.mapURL = mapURL;
        this.mapXcord = mapXcord;
        this.mapYcord = mapYcord;
        this.accuracy = accuracy;
        this.symbolicID = symbolicID;
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    public String getMapURL() {
        return mapURL;
    }

    public int getMapXcord() {
        return mapXcord;
    }

    public int getMapYcord() {
        return mapYcord;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public String getSymbolicID() {
        return symbolicID;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mapName);
        out.writeString(mapURL);
        out.writeInt(mapXcord);
        out.writeInt(mapYcord);
        out.writeInt(accuracy);
        out.writeString(symbolicID);
        out.writeInt(id);
    }

    public static final Creator<LocationParcel> CREATOR = new Creator<LocationParcel>() {
        public LocationParcel createFromParcel(Parcel in) {
            return new LocationParcel(in);
        }

        public LocationParcel[] newArray(int size) {
            return new LocationParcel[size];
        }
    };

    private LocationParcel(Parcel in) {
        mapName = in.readString();
        mapURL = in.readString();
        mapXcord = in.readInt();
        mapYcord = in.readInt();
        accuracy = in.readInt();
        symbolicID = in.readString();
        id = in.readInt();
    }
}
