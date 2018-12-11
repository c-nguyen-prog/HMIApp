package hmi.hmiprojekt.MemoryAccess;

import android.media.ExifInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hmi.hmiprojekt.TripComponents.Trip;
import hmi.hmiprojekt.TripComponents.Waypoint;

public class TripReader {

    public static Trip[] readTrips() throws ParseException {
        List<File> dirs = Config.listDirs(Config.tripFolder);
        Trip[] trips = new Trip[dirs.size()];
        // creates Trip object out of each dir
        for(int i = 0; i < trips.length; i++) {
            trips[i] = readTrip(dirs.get(i));
        }
        return trips;
    }

    public static List<Waypoint> readWaypoints(Trip t) {
        List<Waypoint> waypoints = new ArrayList<>();
        List<File> waypointFiles = Config.listFiles(t.getDir());

        for (File waypointFile: waypointFiles) {
            try {
                waypoints.add(readWaypoint(waypointFile));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return waypoints;
    }

    protected static Waypoint readWaypoint(File img) throws
            ParseException
            , IOException {

        if (!img.exists()) throw new FileNotFoundException(img.getAbsolutePath() + "not found");
        String[] tmp = img.getName().split("_");
        if (tmp.length != 2) throw new RuntimeException(img.getAbsolutePath() + "not a waypoint");

        Date date = Config.waypointdf.parse(tmp[0]);
        String name = tmp[1];
        ExifInterface exifInterface = new ExifInterface(img.getAbsolutePath());
        String description = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        long longi = Long.parseLong(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        long lati = Long.parseLong(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));

        return new Waypoint(img, name, description, date, longi, lati);
    }

    private static Trip readTrip(File tripDir) throws ParseException {
        String[] tmp = tripDir.getName().split("_");
        Date start = Config.tripdf.parse(tmp[0]);
        return new Trip(tmp[1], start, tripDir);
    }
}
