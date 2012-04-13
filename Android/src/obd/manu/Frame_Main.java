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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;



public class Frame_Main extends Activity 
{

	boolean debug;
	private EditText text_a_envoyer;
	private long lastTime = 0;
	Class_Bluetooth_OBD OBD = null;
	Class_UserPreferences mPref ;
	//Context _context;
	
	//ProgressDialog pDL;
	
/*	final Handler handler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            
            long t = System.currentTimeMillis();
            if(t-lastTime > 100) 
            {// Pour �viter que les messages soit coup�s
				lastTime = System.currentTimeMillis();
			}
            liste.append(data);
            if (data.endsWith(">"))
            {
            	liste.append("\r\n");
            }
        }
    };
  */  
    final Handler handlerMAJValues = new Handler() {
        @Override
		public void handleMessage(Message msg) {
        /*    int co = msg.arg1;

            if(co == 1) {
            	liste.append("Connected\n");
            } else if(co == 2) {
            	liste.append("Disconnected\n");
            }  */
       }
    };

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try{
	        super.onCreate(savedInstanceState);
	        //fullscreen
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        setContentView(R.layout.main);    
	        text_a_envoyer = (EditText) findViewById(R.id.editTextAEnvoyer);
	        
	//#region initialise
	        Context ctx = this.getApplicationContext();
	        mPref = new Class_UserPreferences(ctx);        
	        //Class_Notifier.startStatusbarNotifications(ctx);
	        Log.v("initialise OBD", "true" );
	        if (OBD==null){
	        	OBD= new Class_Bluetooth_OBD(mPref.m_getParam("pref_obd_name"), this, handlerMAJValues );
	        }
	        debug = (mPref.m_getParam("pref_debug")=="true");
	        m_adjustLinearLayouts(debug);	        	
	 //#endregion       
	        //evite extinction ecran
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}
        catch (Exception e){     
        	Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    


	private void m_adjustLinearLayouts(boolean debug) {
		try {
			LinearLayout lL;
			LayoutParams lParam;
			int height = 100;
			lL =  (LinearLayout)findViewById(R.id.linearLayoutDebug); 
	        if (debug){	
	        	lL.setVisibility(View.VISIBLE);//visible
	        }
	        else{
	        	lL.setVisibility(View.INVISIBLE);//invisible
	        	height+=30;
	        }
	        
	        lL =  (LinearLayout)findViewById(R.id.linearLayout1);
	        lParam = lL.getLayoutParams();
	        lParam.height=height;
	        lL.setLayoutParams(lParam);
	        lL =  (LinearLayout)findViewById(R.id.linearLayout2);
	        lParam = lL.getLayoutParams();
	        lParam.height=height;
	        lL.setLayoutParams(lParam);
	        lL =  (LinearLayout)findViewById(R.id.linearLayout3);
	        lParam = lL.getLayoutParams();
	        lParam.height=height*2;
	        lL.setLayoutParams(lParam);
		} catch (Exception e) {
			
		}
	}



	public void cmdClick(View view){
		switch (view.getId()) 
		{
			case R.id.cmdEnvoyer:
				// #region cmdEnvoyer			     
				String texteSaisi = text_a_envoyer.getText().toString();
				if (texteSaisi.length() == 0) 
				{
					Toast.makeText(this, "Rien � envoyer !",
							Toast.LENGTH_SHORT).show();
					return;
				}
				OBD.m_sendData(texteSaisi+"\r",1000);
				break;
				
				// #endregion
	/*		case R.id.cmdTest:
				// #region cmdTest
				String value = mPref.m_getParam(text.getText().toString());//  m_getParam(text.getText().toString(), getApplicationContext());
				liste.append(text.getText().toString()+" .. "+value+"\r\n");
				//BT.m_setBT(value);
			    //BT.m_connect();
				break;
				// #endregion
				 
				 */
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
      /*  case R.id.menuPreviewVideo:
        	// #region Preview Video
        	Intent intent1 = new Intent(this, Frame_Recorder.class);
        	startActivity(intent1);
        	return true;
        	// #endregion  */
     
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

