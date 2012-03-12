package obd.manu;

import android.os.Bundle;
import android.preference.PreferenceActivity;
public class prefs extends PreferenceActivity{
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference);
  }
}