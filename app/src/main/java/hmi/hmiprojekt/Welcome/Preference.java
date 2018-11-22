package hmi.hmiprojekt.Welcome;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
    SharedPreferences preference;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "hmi-project";
    private static final String IS_FIRST_TIME = "IsFirstTime";

    public Preference(Context context) {
        this.context = context;
        preference = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preference.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return preference.getBoolean(IS_FIRST_TIME, true);
    }
}
