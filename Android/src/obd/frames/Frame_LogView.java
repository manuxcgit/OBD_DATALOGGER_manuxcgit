package obd.frames;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import android.R.drawable;
import android.R.integer;

import obd.frames.R;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

public class Frame_LogView extends Activity {
	
	Spinner spinnerFichierLog;
	ImageView imageviewLOG;
	SeekBar seekbarLOG;
	Context ctx;
	private Bitmap bitmapLOG;
	private BitmapDrawable drawableLOG;
	
	public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //fullscreen
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        setContentView(R.layout.logviewer);
	        
	        ctx = this.getApplicationContext();
	        
	        //#region remplir spinnerFichierLog
	        spinnerFichierLog = (Spinner) findViewById(R.id.spinnerFichierLog);
	        imageviewLOG = (ImageView) findViewById(R.id.imageViewFichierLog);
	        seekbarLOG = (SeekBar) findViewById(R.id.seekBarFichierLog);
			seekbarLOG.setOnSeekBarChangeListener(sbChanged);
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

	public void cmdClick(View view){
		switch (view.getId()) 
		{
			case R.id.cmdChoixFichierLog:
				m_afficherLog(100);
				break;
		}
    }
	
	OnSeekBarChangeListener sbChanged = new SeekBar.OnSeekBarChangeListener(){		   
		   public void onProgressChanged(SeekBar seekBar, int progress,
		     boolean fromUser) {
			   m_afficherLog(progress);
		   }
		  
		   public void onStartTrackingTouch(SeekBar seekBar) {
		    // TODO Auto-generated method stub
		   }
		  
		   public void onStopTrackingTouch(SeekBar seekBar) {
		    // TODO Auto-generated method stub
		   }
	};

	private void m_afficherLog(int size) {
		bitmapLOG = Bitmap.createBitmap(600, 300, Bitmap.Config.ARGB_8888);
		for (int i = 0; i < 300; i++) {
			bitmapLOG.setPixel(i, i, Color.GREEN);
		}
		drawableLOG = new BitmapDrawable(bitmapLOG);// getResources().getDrawable(R.drawable.empty_trash);
		drawableLOG.setBounds(0, 0, 600, 300);
		drawableLOG.setGravity(size);
	    Canvas canvas = new Canvas(bitmapLOG);
	    drawableLOG.draw(canvas);
	    imageviewLOG.setImageDrawable(drawableLOG);
	}
}
