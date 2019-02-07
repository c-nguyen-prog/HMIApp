package hmi.hmiprojekt.Welcome;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Chi Nguyen
 * This class defines the Preferences used for this app using SharedPreference API
 */
public class Preference {
    SharedPreferences preference;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "hmi-project";
    private static final String IS_FIRST_TIME = "IsFirstTime";
    private static final String MA_FIRST_TIME = "MAFirstTime";

    /**
     * Constructor for the Preference
     * @param context App Context p
     */
    public Preference(Context context) {
        this.context = context;
        preference = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preference.edit();
    }

    /**
     * Setter for first time launch, used for the welcoming slides
     * @param isFirstTime boolean variable to be set
     */
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.commit();
    }

    /**
     * Setter for MainActivity first show, used for walkthrough tutorial
     * @param mainFirstTime boolean variable to be set
     */
    public void setMAFirstTimeLaunch(boolean mainFirstTime) {
        editor.putBoolean(MA_FIRST_TIME, mainFirstTime);
        editor.commit();
    }

    /**
     * Getter for isFirstTimeLaunch
     * @return return true or false
     */
    public boolean isFirstTimeLaunch() {
        return preference.getBoolean(IS_FIRST_TIME, true);
    }

    /**
     * Getter for isMAFirstTimeLaunch
     * @return return true or false
     */
    public boolean isMAFirstTimeLaunch() {
        return preference.getBoolean(MA_FIRST_TIME, true);
    }
}
