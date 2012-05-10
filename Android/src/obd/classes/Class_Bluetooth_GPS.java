package obd.classes;

import java.io.File;
import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.sax.StartElementListener;
import android.text.format.Time;
import android.util.Log;

public class Class_Bluetooth_GPS extends Class_Bluetooth{
	
	//private ProgressDialog pDL;
	
	 public Class_Bluetooth_GPS (String nomBT, Context context, Handler toMainFrame, String receivedSplit)	{
 		super(nomBT, context, toMainFrame, receivedSplit);
 	} 


	@Override
	protected void m_traiteMessage(Message msg) {
		IsConnected = true;
		if (etatThreadLog==1){
			//sauve les données
		}
	}

	@Override
	protected void m_LOGThread() {
		 try {
			 Looper.prepare();
			 if (m_creeFichierLOG("GPS")==1){
				 etatThreadLog = 1;
			 }	
		 }
		 catch (Exception e) {
			etatThreadLog = 2;
		}		
	}

	@Override
	protected void m_connecteBT() {
		pDL = new ProgressDialog(_context);		
		pDL.setMax(4);
		pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDL.setTitle("RECHERCHE GPS");
		pDL.setMessage("Essai Connection");
		pDLShow.sendEmptyMessage(0);
		IsInitialised=false;
		Thread thread_initialiseGPS = new Thread(new Runnable() {
			
			public void run() {
				Looper.prepare();
				try{
					long start_time = System.currentTimeMillis();
					long elapsed_time = 0;
					//#region connection
			        while (!IsConnected & !quitter){
			        	elapsed_time = System.currentTimeMillis() - start_time;
			        	if (elapsed_time>5000) {
			        		relancerTest = false;
							wait_for_alert = true;
							alertbox = new AlertDialog.Builder(_context);
							alertbox.setTitle("PAS DE REPONSE DU GPS");
				            alertbox.setMessage("Voulez vous encore attendre ?");
				            alertbox.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface arg0, int arg1) {
				                	wait_for_alert = false;
				                }
				            });
				            alertbox.setNegativeButton("Non", new DialogInterface.OnClickListener() {           
				                public void onClick(DialogInterface arg0, int arg1) {
				                	wait_for_alert = false;
				                	quitter=true;
				                }
				            });
				            alertBoxShow();
				            while (wait_for_alert){}
				            if (!quitter){
				            	start_time = System.currentTimeMillis();
				            }
			        	}
			        }
			        if (quitter){
			        	m_incrementpDL("Echec à la connection du GPS");
			        	Thread.sleep(2000);
			        	pDLDismiss.sendEmptyMessage(0);
			        }
			        //#endregion
			        //#region initialisation
			        //attend localisation satellites
			        //#endregion
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
		thread_initialiseGPS.start();
	}
}
