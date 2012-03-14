package obd.manu;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
public class Frame_Preferences extends PreferenceActivity{
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference);
    
    ListPreference LP = (ListPreference)findPreference("userName");
    
   // Class_Bluetooth.listePeriphBluetooth.add("ee");
    LP.setEntries((CharSequence[]) Class_Bluetooth.listePeriphBluetooth.toArray());
    LP.setValueIndex(R.array.listValues);
  }
}