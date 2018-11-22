package hmi.hmiprojekt.MemoryAccess;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import hmi.hmiprojekt.TripComponents.Trip;

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

    private static Trip readTrip(File tripDir) throws ParseException {
        String[] tmp = tripDir.getName().split("_");
        Date start = Config.tripdf.parse(tmp[0]);
        return new Trip(tmp[1], start, tripDir);
    }
}
