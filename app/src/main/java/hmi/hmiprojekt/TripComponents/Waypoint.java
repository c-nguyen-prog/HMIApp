package hmi.hmiprojekt.TripComponents;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;

public class Waypoint implements Comparable {

    private File img;
    private String name;
    private String desc;
    private Date timestamp;
    private long latitude;
    private long longitude;

    public Waypoint(File img
            , String name
            , String desc
            , Date timestamp
            , long latitude
            , long longtitude) {
        this.img = img;
        this.name = name;
        this.desc = desc;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longtitude;
    }

    // getters
    public File getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (!(o instanceof Waypoint)) return 0;
        Waypoint w = (Waypoint) o;
        return w.getTimestamp().compareTo(timestamp);
    }
}
