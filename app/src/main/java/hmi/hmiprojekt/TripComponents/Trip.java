package hmi.hmiprojekt.TripComponents;

import android.os.Parcelable;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hmi.hmiprojekt.MemoryAccess.Config;
import hmi.hmiprojekt.MemoryAccess.TripReader;

public class Trip {

    private String name;
    private Date start;
    private File dir;
    private List<Waypoint> waypoints;

    // constructors
    public Trip(String name, Date start, File dir) {
        this.name = name;
        this.start = start;
        this.dir = dir;
    }

    public Trip(String name, Date start) {
        this.name = name;
        this.start = start;
    }

    // CAREFUL! Assumes current Date
    public Trip(String name) {
        this.name = name;
        this.start = Calendar.getInstance().getTime();
    }

    private boolean areWaypointsInitialized() {
        return waypoints != null;
    }


    private void readInWaypoints() {
        waypoints = TripReader.readWaypoints(this);
        Collections.sort(waypoints);
    }

    public List<Waypoint> getWaypoints() {
        if (!areWaypointsInitialized()) readInWaypoints();
        return waypoints;
    }

    public void notifyWaypointsChanged() {
        readInWaypoints();
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    // generic getters
    public Date getStart() {
        return start;
    }

    public File getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }
}
