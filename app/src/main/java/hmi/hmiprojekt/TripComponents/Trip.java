package hmi.hmiprojekt.TripComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trip {

    private String name;
    private Date start;
    private File dir;
    private List<Waypoint> waypoints = new ArrayList<>();

    public Trip(String name, Date start, File dir) {
        this.name = name;
        this.start = start;
        this.dir = dir;
    }

    public boolean hasWaypoints() {
        if (waypoints.size() == 0) return false;
        return true;
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
