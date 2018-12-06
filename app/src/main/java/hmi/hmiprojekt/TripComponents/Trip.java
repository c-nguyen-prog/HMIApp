package hmi.hmiprojekt.TripComponents;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Trip {

    private String name;
    private Date start;
    private File dir;
    private List<Waypoint> waypoints;

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
        //TODO READ WAYPOINTS
    }

    public List<Waypoint> getWaypoints() {
        if (!areWaypointsInitialized()) readInWaypoints();
        return waypoints;
    }

    public void addWaypoints(List<Waypoint> waypoints) {
        this.waypoints.addAll(waypoints);
    }

    public void addWayPoint(Waypoint w) {
        waypoints.add(w);
    }

    public void removeWayPoint(Waypoint w) {
        waypoints.remove(w);
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
