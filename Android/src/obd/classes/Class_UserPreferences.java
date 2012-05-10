package obd.classes;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Class_UserPreferences {
    private Context _context;
 
    //@Inject
    public Class_UserPreferences(Context context) {
        this._context = context;
    }
 
    SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(_context.getApplicationContext());
    }
 
    public String m_getParam(String Key) {
    	try{
	    	Map<String, ?> _liste = getDefaultSharedPreferences().getAll();
	        for (Map.Entry<String, ?>  couple : _liste.entrySet()) {
	        	//Toast.makeText(_context, couple.getKey() , Toast.LENGTH_LONG).show();
	        	if (Key.equals(couple.getKey()))
	        	{
	        		//Toast.makeText(_context, couple.getValue().toString() , Toast.LENGTH_LONG).show();
	        		Log.v("m_getParam " + Key,couple.getValue().toString());
	        		return (couple.getValue().toString());    		
	        	}  
	     	}
    	}
    	finally {} 
        return "default value";
    }
 
}