package hmi.hmiprojekt.Welcome;

/**
 * @author Chi Nguyen
 * This class defines the Application with its Preference Settings
 */
public class Application extends android.app.Application {
    private Preference preference;
    private static Application app;

    /**
     * Sole Constructor
     */
    public void onCreate() {
        super.onCreate();
        app = this;
        preference = new Preference(this);
    }

    /**
     * Getter for app
     * @return return this app
     */
    public static Application getApp() {
        return app;
    }

    /**
     * Getter for preference
     * @return return preference
     */
    public Preference getPreference() {
        return preference;
    }

    /**
     * Setter for preference
     * @param preference value to be set
     */
    public void setPreference(Preference preference) {
        this.preference = preference;
    }

}