package hmi.hmiprojekt;

public class Application extends android.app.Application {
    private Preference preference;
    private static Application app;

    public void onCreate() {
        super.onCreate();
        app = this;
        preference = new Preference(this);
    }

    public static Application getApp() {
        return app;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

}