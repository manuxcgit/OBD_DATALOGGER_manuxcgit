package obd.manu;

import java.util.Map;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;



public class Frame_Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	
	
	
  @Override
  public void onCreate(Bundle savedInstanceState){
	  

    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    
    
    // #region affiche les valeurs des preferences
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    Map<String, ?>liste =  sp.getAll();
    for (Map.Entry<String, ?>  couple : liste.entrySet()) {
    	String Key = couple.getKey();
    	try
    	{
    		Preference pref = findPreference(Key);
    		pref.setSummary(couple.getValue().toString());    		
    	}
    	catch (Exception e) {
    		Log.e("Affiche valeurs des pref", Key + "??");
		}    
 	}
    // #endregion
    // #region remplit List BT dispo
    try{    
	    ListPreference LP_obd = (ListPreference)findPreference("pref_obd_name");
	    ListPreference LP_gps = (ListPreference)findPreference("pref_gps_name");
	    CharSequence[] test = Class_Bluetooth.m_getListeBT();
	    
	    //pour test debuggage
	    Log.v("Liste OBD", String.format("%d", test.length));
	    if (test.length==0)
	    {
	    	test = new CharSequence[] {"test","debuggage"};
	    }
	    
	    LP_gps.setEntries(test);
	    LP_gps.setEntryValues(test);
	    LP_obd.setEntries(test);
	    LP_obd.setEntryValues(test);
    }
    catch (Exception e) {
    	Log.e("Remplit LP_BT dispos", "??");
	}
    // #endregion
    // #region remplit List Video Size
    try {
		ListPreference LP_Video = (ListPreference)findPreference("pref_videosize");
		LP_Video.setEntries(Class_Camera.m_getListe());
		LP_Video.setEntryValues(Class_Camera.m_getListe());
	} catch (Exception e) {
		//
	}
    // #endregion
    
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    prefs.registerOnSharedPreferenceChangeListener(Frame_Preferences.this);
  }

public void onSharedPreferenceChanged(SharedPreferences sP,
		String key) {
	try{
	Preference pref = findPreference(key);
	pref.setSummary(sP.getString(key, ""));
	}
	catch (Exception e) {
		//Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
	}
	if (key.equals("pref_videosize")){
		try{
			String[] values = sP.getString(key, "").split("X");
			Class_Camera.cameraSize = new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
		}
		catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT					).show();
		}
	}
}
 

}