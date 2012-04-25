package obd.manu;

//http://nononux.free.fr/index.php?page=elec-brico-bluetooth-android-microcontroleur
//http://www.vogella.de/articles/Android/article.html	
//http://www.tutomobile.fr/intent-passer-dune-activity-a-une-autre-tutoriel-android-n%C2%B011/16/07/2010/import java.util.Timer;
//http://kidrek.fr/blog/android/android-gestion-des-preferences-au-sein-dune-appli/


import obd.manu.R;
import obd.manu.R.string;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class Frame_Main extends Activity {

	//#region declarations
	boolean debug;
	private EditText text_a_envoyer;
	private long lastTime = 0;
	Class_Bluetooth_OBD OBD = null;
	Class_UserPreferences mPref ;
	Context ctx;
	TextView tvRPM;
	TextView tvSpeed;
	TextView tvOilTemp;
	TextView tvWaterTemp;
	TextView tvEngagedGear;
	//#endregion
	

    final Handler handlerMAJValues = new Handler() {
        @Override
		public void handleMessage(Message msg) {
        	msg.
        	int[] values = OBD.getValues();// {log*5,log*10,log*100,log*10};//OBD.getValues(); //water, oil, rpm, speed;
        	tvWaterTemp.setText(String.format("%3d", values[0]));
        	tvOilTemp.setText(String.format("%3d", values[1]));
        	tvRPM.setText(String.format("%5d", values[2]));
        	tvSpeed.setText(String.format("%4d", values[3]));
        	/*
        	 * engaged gear
        	 */
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
	        ctx = this.getApplicationContext();
	        mPref = new Class_UserPreferences(ctx);   
	        tvRPM = (TextView) findViewById(R.id.textViewRPMVALUE);
	        tvSpeed = (TextView) findViewById(R.id.textViewVITESSEVALUE);
	        tvOilTemp = (TextView) findViewById(R.id.textViewHUILETEMPVALUE);
	        tvWaterTemp = (TextView) findViewById(R.id.textViewEAUTEMPVALUE);
	        tvEngagedGear = (TextView) findViewById(R.id.textViewGEARENGAGED);
	        //Class_Notifier.startStatusbarNotifications(ctx);
	        Log.v("initialise OBD", "true" );
	        if (OBD==null){
	        	OBD= new Class_Bluetooth_OBD(mPref.m_getParam("pref_obd_name"), this, handlerMAJValues, ">" );
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
				if (texteSaisi.length() == 0) {
					Toast.makeText(this, "Rien à envoyer !",
							Toast.LENGTH_SHORT).show();
					return;
				}
				try{
					String test = OBD.m_sendData(texteSaisi+"\r",2000);					
    	            Log.v(texteSaisi,test);
    	            Toast.makeText(this,"Retour cmdEnvoyer = " + test,Toast.LENGTH_SHORT).show();
				}
				catch (Exception e) {Toast.makeText(this, "OBD non initialisé",Toast.LENGTH_SHORT).show();}
				break;
				
				// #endregion
		}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu); 
        return true;
     }

    @Override
	public boolean onOptionsItemSelected(MenuItem item)  {
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
        case R.id.menuLancerLog:
        	try{
	        	String title = item.getTitle().toString();
	        	if (title.equals(getResources().getString(R.string.menuLancerLog))){
	        		if (OBD.StartLog()) {
	        			item.setTitle(string.menuArreterLog);
	        		}
	        		else{
	        			Toast.makeText(this, "Echec au lancement du LOG",Toast.LENGTH_SHORT).show();
	        		}
	        	}
	        	else {
					OBD.StopLog();
					item.setTitle(string.menuLancerLog);
				}
        	}
        	catch (Exception e) {
        		Toast.makeText(this, "Pas d'OBD trouvé",Toast.LENGTH_SHORT).show();
			}
        	return true;
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

