package obd.classes;

import java.io.File;
import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.R.string;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.sax.StartElementListener;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Class_Bluetooth_GPS extends Class_Bluetooth{
	
	//private string _latitude;
	//private string _longitude;
	private string _vitesseGPS;
	
	 public Class_Bluetooth_GPS (String nomBT, Context context, Handler toMainFrame, String receivedSplit)	{
 		super(enumBt.GPS, nomBT, context, toMainFrame, receivedSplit);
 	} 


	@Override
	protected void m_traiteMessage(Message msg) {
		try{
			IsInitialised = true;
			String received = msg.getData().getString("data");
			if (debug){
				Message _toFrame = MessageReceived.obtainMessage();
				Bundle b = new Bundle();
				b.putString("data", received.replace("\r\n", "").substring(0, 20));
	            _toFrame.setData(b);  
	            ToMainFrame.sendMessage(_toFrame);
			}
			else{
				if (received.contains("RMC")){
					String[] infos = received.split(",");
					try {
						double _v = Double.parseDouble(infos[7].replace(".", ","));
						_v *= 0.14278;
						Message _toFrame = MessageReceived.obtainMessage();
						Bundle b = new Bundle();
						b.putString("data", String.format("Vitesse : %d km/h", _v));
		                _toFrame.setData(b);  
		                ToMainFrame.sendMessage(_toFrame);
					} catch (Exception e) {
						toast(infos[7]);
					}
				}
			}
			if (etatThreadLog==1){
				WriteLOG(received);
			}
		}
		catch (Exception e){}
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
		pDL.setMax(2);
		pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pDL.setTitle("RECHERCHE GPS " + BT_Name);
		pDL.setMessage("Essai Connection");
		pDLShow.sendEmptyMessage(0);
		IsInitialised=false;
		Thread thread_initialiseGPS = new Thread(new Runnable() {			
			public void run() {
				Looper.prepare();
				try{
					m_setBT();
					Thread.sleep(1500);
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
					m_incrementpDL("GPS connecté");
					Thread.sleep(2000);
					//receiverThread.start();
			        //#region initialisation
			        //attend localisation satellites
					while (!IsInitialised){}
					m_incrementpDL("GPS initialisé, pret !!");
					Thread.sleep(2000);
					pDLDismiss.sendEmptyMessage(0);
			        //#endregion
			        //#endregion
				}
				catch (Exception e) {
		        	m_incrementpDL("Probleme imprévu !!");
		        	try{Thread.sleep(2000);}
		        	catch(Exception e1) {}
		        	pDLDismiss.sendEmptyMessage(0);
				}
				init_terminee=true;
			}
		});
		thread_initialiseGPS.start();
	}
}
