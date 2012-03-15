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
    try
    {
    
    ListPreference LP = (ListPreference)findPreference("btpourobd");
    if (Class_Bluetooth.listePeriphBluetooth.isEmpty())
    {
    	Class_Bluetooth.listePeriphBluetooth.add("ee");
    	Class_Bluetooth.listePeriphBluetooth.add("zz");
    }
    CharSequence[] test = Class_Bluetooth.m_getListeBT();
    LP.setEntries(test);
    LP.setEntryValues(test);
    }
    catch (Exception e) {
		Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
	}
    
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
		Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
	}
}
 

}