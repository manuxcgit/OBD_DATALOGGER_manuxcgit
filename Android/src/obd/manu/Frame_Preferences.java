package obd.manu;

import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;



public class Frame_Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	
	
  @Override
  public void onCreate(Bundle savedInstanceState){
	  

    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference);
    
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
			// TODO: handle exception
		}    
 	}
    // #endregion
    // #region remplit List BT dispo
    try{    
	    ListPreference LP_obd = (ListPreference)findPreference("pref_btpourobd");
	    ListPreference LP_gps = (ListPreference)findPreference("pref_btpourgps");
	    //pour test debuggage
	    if (Class_Bluetooth.listePeriphBluetooth.isEmpty())
	    {
	    	Class_Bluetooth.listePeriphBluetooth.add("ee");
	    	Class_Bluetooth.listePeriphBluetooth.add("zz");
	    }
	    CharSequence[] test = Class_Bluetooth.m_getListeBT();
	    LP_gps.setEntries(test);
	    LP_gps.setEntryValues(test);
	    LP_obd.setEntries(test);
	    LP_obd.setEntryValues(test);
    }
    catch (Exception e) {
		Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
	}
    // #endregion
    // #region remplit List Video Size
    try {
		ListPreference LP_Video = (ListPreference)findPreference("pref_videosize");
		LP_Video.setEntries(Class_Camera.m_getListe());
		LP_Video.setEntryValues(Class_Camera.m_getListe());
	} catch (Exception e) {
		// TODO: handle exception
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
}
 

}