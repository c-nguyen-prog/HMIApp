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
            trips[0] = readTrip(dirs.get(0));
        }
        return trips;
    }

    public static Trip readTrip(File tripDir) throws ParseException {
        String[] tmp = tripDir.getName().split("__");
        Date start = Config.tripdf.parse(tmp[0]);
        return new Trip(tmp[1], start, tripDir);
    }
}
