package obd.manu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Class_UserPreferences {
    private Context context;
 
    //@Inject
    public Class_UserPreferences(Context context) {
        this.context = context;
    }
 
    SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
 
    public String m_getParam(String Key) {
        return getDefaultSharedPreferences().getString(Key, "default value");
    }
 
}