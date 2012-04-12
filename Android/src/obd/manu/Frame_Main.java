package obd.manu;

//http://nononux.free.fr/index.php?page=elec-brico-bluetooth-android-microcontroleur
//http://www.vogella.de/articles/Android/article.html	
//http://www.tutomobile.fr/intent-passer-dune-activity-a-une-autre-tutoriel-android-n%C2%B011/16/07/2010/import java.util.Timer;
//http://kidrek.fr/blog/android/android-gestion-des-preferences-au-sein-dune-appli/


import java.io.FileOutputStream;
import java.io.IOException;

import obd.manu.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;



public class Frame_Main extends Activity 
{

	boolean app_unique;
	private EditText text;
	private EditText liste;
	private long lastTime = 0;
	Chronometer Chrono ;
	Class_Bluetooth_OBD OBD = null;
	Class_UserPreferences mPref ;
	//Context _context;
	
	//ProgressDialog pDL;
	
	final Handler handler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            
            long t = System.currentTimeMillis();
            if(t-lastTime > 100) 
            {// Pour éviter que les messages soit coupés
				lastTime = System.currentTimeMillis();
			}
            liste.append(data);
            if (data.endsWith(">"))
            {
            	liste.append("\r\n");
            }
        }
    };
    
    final Handler handlerStatus = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            int co = msg.arg1;

            if(co == 1) {
            	liste.append("Connected\n");
            } else if(co == 2) {
            	liste.append("Disconnected\n");
            }
       }
    };
	
    public boolean isUnique() {
        try {
            app_unique = new FileOutputStream("lock").getChannel().tryLock() != null;
        } catch(IOException ie) {
            app_unique = false;
            Log.e("unique", "erreur");
        }
        return app_unique;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);    
        text = (EditText) findViewById(R.id.editTextCodeAEnvoer);
        liste = (EditText) findViewById(R.id.listReceived);
        Chrono = (Chronometer) findViewById(R.id.chronometer1);
   
    	if (!isUnique()){
    		Toast.makeText(this, "doublon", Toast.LENGTH_SHORT).show();
    		//finish();
    	}
    	
        Context ctx = this.getApplicationContext();
        //_context = this;
        
        mPref = new Class_UserPreferences(ctx);
        Log.v("ctx","ok");
        
        //Class_Notifier.startStatusbarNotifications(ctx);
        
         
        OBD= new Class_Bluetooth_OBD(mPref.m_getParam("pref_obd_name"), this, handler );
        
              
    	}
        catch (Exception e)
        {     
        	Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    
   
    public void cmdClick(View view){
		switch (view.getId()) 
		{
			case R.id.cmdEnvoyer:
				// #region cmdEnvoyer
				
				Chrono.setBase(SystemClock.elapsedRealtime());
				Chrono.setText("00:00.00");
				Chrono.start();
		
			     
			     
				String texteSaisi = text.getText().toString();
				if (texteSaisi.length() == 0) 
				{
					Toast.makeText(this, "Rien à envoyer !",
							Toast.LENGTH_SHORT).show();
					return;
				}
				liste.append(">"+texteSaisi+"\r\n");
				OBD.m_sendData(texteSaisi+"\r",1000);
				break;
				
				// #endregion
			case R.id.cmdTest:
				// #region cmdTest
				String value = mPref.m_getParam(text.getText().toString());//  m_getParam(text.getText().toString(), getApplicationContext());
				liste.append(text.getText().toString()+" .. "+value+"\r\n");
				//BT.m_setBT(value);
			    //BT.m_connect();
				break;
				// #endregion
		}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu); 
        return true;
     }

    @Override
	public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId()) 
      {
        case R.id.menuQuitter :
        	// #region MenuQuitter
        	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setMessage("Voulez vous quitter ?");
            alertbox.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                	try
                	{OBD.close();}
                	catch (Exception e) {
						// TODO: handle exception
					}
                	
                    finish();
                }
            });
            alertbox.setNegativeButton("Non", new DialogInterface.OnClickListener() {           
                public void onClick(DialogInterface arg0, int arg1) { }
            });
            alertbox.show();
            return true;
            // #endregion    
        case R.id.menuOption:
        	// #region MenuOpion
        	Intent intent = new Intent(this, Frame_Preferences.class);
        	startActivity(intent);
        	return true;
        	// #endregion
        case R.id.menuPreviewVideo:
        	// #region Preview Video
        	Intent intent1 = new Intent(this, Frame_Recorder.class);
        	startActivity(intent1);
        	return true;
        	// #endregion
     
      }
    		 
      return false;
   }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
            switch(keyCode)
            {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return false;              
            case KeyEvent.KEYCODE_BACK:
                return false;
            }
            return false;
    }

    public static  String m_getParam(String Param, Context hereContext){
		SharedPreferences sp=  PreferenceManager.getDefaultSharedPreferences(hereContext);
		return sp.getString(Param, "defaultvalue");
    }
}

