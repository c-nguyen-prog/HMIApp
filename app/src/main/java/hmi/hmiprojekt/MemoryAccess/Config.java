package hmi.hmiprojekt.MemoryAccess;

import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// has configuration constants and methods used for general memory access.
public class Config {

    static final File tripFolder =
            new File(Environment.getExternalStorageDirectory() + "/roadbook/");
    // DateFormat used for saving tips
    static final DateFormat tripdf = new SimpleDateFormat("yyyyMMdd");
    static final DateFormat waypointdf = new SimpleDateFormat("yyyyMMdd:HHmmss");


    static List<File> listDirs(File dir) {
        File[] files = dir.listFiles();
        List<File> res = new ArrayList<>();

        for (File file: files) {
            if (file.isDirectory()) res.add(file);
        }
        return res;
    }


    static List<File> listFiles(File dir) {
        File[] files = dir.listFiles();
        List<File> res = new ArrayList<>();

        for (File file: files) {
            if (file.isFile()) res.add(file);
        }
        return res;
    }


    public static boolean createTripFolder() {
        return(tripFolder.mkdir());
    }
}
