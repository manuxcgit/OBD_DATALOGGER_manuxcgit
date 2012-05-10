package obd.classes;

import obd.classes.Class_Bluetooth.enum_typeBluetooth;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Class_Bluetooth_GPS extends Class_Bluetooth{
	
	//private ProgressDialog pDL;
	
	 public Class_Bluetooth_GPS (String nomBT, Context context, Handler toMainFrame, String receivedSplit)	{
 		super(nomBT, enum_typeBluetooth.GPS, context, toMainFrame, receivedSplit);// hstatus, h, context);
 		//receiverThread =  new ReceiverThread();
 		//Class_UserPreferences mPref = new Class_UserPreferences(_context) ;
 	/*	debug = (mPref.m_getParam("pref_debug")=="true");
 		try {
				intervalleLOG = Integer.parseInt(mPref.m_getParam("pref_periodeLOG"));
				nbrLoopTest = Integer.parseInt(mPref.m_getParam("pref_nbrtestconnection"));
			} catch (Exception e) {	}
 		if (debug){
 			Log.v("OBD","Debug on");
 			nbrLoopTest = 1;
 		}*/
 		this.m_connect();
 	}

	 public void m_connect() {
			pDL = new ProgressDialog(_context);		
			pDL.setMax(4);
			pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDL.setTitle("Initialisation OBD");
			pDL.setMessage("Essai Connection");
			pDL.show();
			IsInitialised=false;
 		//thread_InitialiseOBD.start();
 }

	@Override
	protected void m_traiteMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void m_LOGThread() {
		// TODO Auto-generated method stub
		
	}
}
