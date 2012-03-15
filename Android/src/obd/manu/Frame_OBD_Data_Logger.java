package obd.manu;

//http://nononux.free.fr/index.php?page=elec-brico-bluetooth-android-microcontroleur
//http://www.vogella.de/articles/Android/article.html	
//http://www.tutomobile.fr/intent-passer-dune-activity-a-une-autre-tutoriel-android-n%C2%B011/16/07/2010/import java.util.Timer;
//http://kidrek.fr/blog/android/android-gestion-des-preferences-au-sein-dune-appli/


import obd.manu.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;



public class Frame_OBD_Data_Logger extends Activity 
{
    /** Called when the activity is first created. */

	private EditText text;
	private EditText liste;
	private long lastTime = 0;
	Chronometer Chrono ;
	Class_Bluetooth BT = null;

	
	final Handler handler = new Handler() {
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
        public void handleMessage(Message msg) {
            int co = msg.arg1;

            if(co == 1) {
            	liste.append("Connected\n");
            } else if(co == 2) {
            	liste.append("Disconnected\n");
            }
       }
    };
		
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
       
        
              BT = new Class_Bluetooth(handlerStatus, handler); 
              
    	}
        catch (Exception e)
        {     
        	Toast.makeText(this, "Erreur d'initialisation du BT !", Toast.LENGTH_LONG).show();
        }
    }
    
    public void cmdClick(View view) 
    {
		switch (view.getId()) 
		{
			case R.id.cmdEnvoyer:
				// #region cmdEnvoyer
				
				Chrono.setBase(SystemClock.elapsedRealtime());
				Chrono.setText("00:00.00");
				Chrono.start();
				
			/*	SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String  value=sp.getString("btpourobd","defaultvalue");

				liste.append("Test "+value+"\r\n");  */
				
				
				String texteSaisi = text.getText().toString();
				if (texteSaisi.length() == 0) 
				{
					Toast.makeText(this, "Rien à envoyer !",
							Toast.LENGTH_LONG).show();
					return;
				}
				liste.append(">"+texteSaisi+"\r\n");
				BT.m_sendData(texteSaisi+"\r");
				break;
				
				// #endregion
			case R.id.cmdTest:
				// #region cmdTest
				SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String  value=sp.getString("btpourobd","defaultvalue");

				liste.append("Test "+value+"\r\n");
				BT.m_setBT(value);
			    BT.m_connect();
				break;
				// #endregion
		}
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu); 
        return true;
     }

    public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId()) 
      {
        case R.id.menuQuitter :
        	AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setMessage("Voulez vous quitter ?");
            alertbox.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                	try
                	{BT.close();}
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
            
        case R.id.menuOption:
        	Intent intent = new Intent(this, Frame_Preferences.class);
        	startActivity(intent);
    		}
    		 
      return false;
   }
    
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

}

