package hmi.hmiprojekt.MemoryAccess;

import java.io.File;

import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.TripComponents.Waypoint;

public class TripWriter {

    public static void createTripDir(Trip trip) throws Exception {
        File dir = generateTripDir(trip);
        if (dir.exists() || !dir.mkdirs()) {
            throw new Exception("unable to create directory\n"+dir.getAbsolutePath());
        } else {
            trip.setDir(dir);
        }
    }

    private static File generateTripDir(Trip trip) {
        if (trip.getDir() != null) {
            return trip.getDir();
        }
        String dirName = Config.tripdf.format(trip.getStart()) + "_" +trip.getName();
        return new File(Config.tripFolder+ "/" +dirName);
    }

    public static boolean deleteTrip(Trip trip) {
        File tripDir = trip.getDir();
        File[] files = tripDir.listFiles();
        for (File curr: files) {
            if (curr != null) curr.delete();
        }
        return tripDir.delete();
    }

    public static boolean deleteWaypoint(Waypoint waypoint) {
        File toDelete = waypoint.getImg();
        return toDelete.delete();
    }
}
