package hmi.hmiprojekt.MemoryAccess;

import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

// has configuration constants and methods used for general memory access.
public class Config {

    protected static final File tripFolder =
            new File(Environment.getExternalStorageDirectory() + "/roadbook/");
    // DateFormat used for saving tips
    protected static final DateFormat tripdf = new SimpleDateFormat("yyyyMMdd");


    protected static List<File> listDirs(File dir) {
        File[] files = dir.listFiles();
        List<File> res = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) res.add(file);
        }
        return res;
    }
}
