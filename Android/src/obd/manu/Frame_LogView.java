package obd.manu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class Frame_LogView extends Activity {
	
	Spinner spinnerFichierLog;
	
	public void onCreate(Bundle savedInstanceState) {

	        super.onCreate(savedInstanceState);
	        //fullscreen
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        setContentView(R.layout.logviewer);
	        
	        //#region remplir spinnerFichierLog
	        spinnerFichierLog = (Spinner) findViewById(R.id.spinnerFichierLog);
	       // ArrayList<String> listeFichier = new ArrayList<String>();
	       // File f = new File(dirPath); 
	      //  File[] files = f.listFiles(); 
	        File root = android.os.Environment.getExternalStorageDirectory(); 
			File dir = new File (root.getAbsolutePath() + "/OBD");
			File[] listeFichier = dir.listFiles();
			Log.v("nbr fichiers LOG", String.format("%d", listeFichier.length));
			ArrayAdapter adapter = new ArrayAdapter(this,
					android.R.layout.simple_spinner_item, listeFichier);
			spinnerFichierLog.setAdapter(adapter);
	        //#endregion
	}

}
