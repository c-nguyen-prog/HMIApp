package hmi.hmiprojekt.MemoryAccess;

import java.io.File;

import hmi.hmiprojekt.TripComponents.Trip;

public class TripWriter {

    public static void createTripDir(Trip trip) throws Exception {
        File dir = generateTripDir(trip);
        if (!dir.mkdirs()) {
            throw new Exception("unable to create directory\n"+dir.getAbsolutePath());
        }
    }

    private static File generateTripDir(Trip trip) {
        if (trip.getDir() != null) {
            return trip.getDir();
        }
        String dirName = Config.tripdf.format(trip.getStart()) + "_" +trip.getName();
        return new File(Config.tripFolder+ "/" +dirName);
    }
}
