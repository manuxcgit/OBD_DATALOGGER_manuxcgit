package obd.classes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.ETC1Util;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Class_Bluetooth_OBD extends Class_Bluetooth {

	//#region declarations
	private int RPM;
	private int Speed;
	private int WaterTemp;
	private int OilTemp;
	private int nbrLoopTest = 5;
	private long intervalleLOG = 1000;
	private long start_time_LOG;
	private String protocole_name = "";
    
	//#endregion

    public Class_Bluetooth_OBD (String nomBT, Context context, Handler toMainFrame, String receivedSplit)	{
    		super(nomBT, context, toMainFrame, receivedSplit);// hstatus, h, context);
    		//receiverThread =  new ReceiverThread();
    		try {
				intervalleLOG = Integer.parseInt(mPref.m_getParam("pref_periodeLOG"));
				nbrLoopTest = Integer.parseInt(mPref.m_getParam("pref_nbrtestconnection"));
			} catch (Exception e) {	}
    		if (debug){
    			Log.v("OBD","Debug on");
    			nbrLoopTest = 1;
    		}
    		//this.m_connect();
    	}
    
    
    public int[] getValues(){
    	return new int[] {WaterTemp, OilTemp, RPM, Speed};
    }
    
    //#region connection
    @Override
	protected void m_connecteBT() {
			pDL = new ProgressDialog(_context);		
			pDL.setMax(4);
			pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDL.setTitle("Initialisation OBD " + BT_Name);
			pDL.setMessage("Essai Connection");
			pDLShow.sendEmptyMessage(0);
			IsInitialised=false;
    		thread_InitialiseOBD.start();
    }
    
    Thread thread_InitialiseOBD = new Thread(new Runnable() {
    	public void run() {
			try{
				Looper.prepare();
				Thread.sleep(1000);
				m_setBT();
				Log.v("m_setBT","ok");
				// #region connecte
				if (socket!=null){
					socket.connect();
					m_incrementpDL("Connection ok, teste OBD ...");
				}
				else {
					if (!debug){
						m_incrementpDL("Connection échouée");
						Thread.sleep(2000);
						pDLDismiss.sendEmptyMessage(0);
						init_terminee = true;
						return;
					}
					else{
						m_incrementpDL("Connection mode Debug !!");
					}
				}
				IsConnected = true;
				receiverThread.start();
				Log.v("receiverThread","ok");
				Thread.sleep(2000);
				String answer = m_sendData("ATZ\r", 5000);
				Log.v("ATZ",answer);
				if (!protocole_name.equals("ELM327") | IsBusy) {
					if (!debug){
						//isbusy donc probleme
						IsInitialised=false;
						m_incrementpDL("Echec echo ELM327");
						Thread.sleep(2000);
						pDLDismiss.sendEmptyMessage(0);
						return;						
					}	
					else{
						m_incrementpDL("Echo DEBUB");
					}
				}
				else{
					m_incrementpDL("ELM 327 ok");
				}
				Thread.sleep(2000);
				// #endregion
				//supprime les " " dans les reponses
				answer = m_sendData("ATS0\r", 1000);
	            Log.v("ATS0",answer);
				m_initialise.start();
				while (!IsInitialised & !quitter){}		
				Thread.sleep(200);//pour recuperer protocole_name
				m_incrementpDL(protocole_name);
				Thread.sleep(2000);
				if (IsInitialised){
					m_incrementpDL("INITIALISATION REUSSIE !!");
		        	info.arg1 = TOMAINFRAME_LOG_READY;
		        	ToMainFrame.sendMessage(info);
					Thread.sleep(2000);
				}
			}
			catch (Exception e) {
				try {
					m_incrementpDL("Erreur non prévue ....");	
					Thread.sleep(2000);
				} 
				catch (Exception e2) {				}
			}	
			pDLDismiss.sendEmptyMessage(0);
			init_terminee = true;
		}
	});
    
	Thread  m_initialise = new Thread ( new Runnable() {
		public void run() {			
			Looper.prepare();
			IsInitialised = false;
			quitter = false;
			WaterTemp = -2;
			String test;
			while (!IsInitialised & !quitter) {
				for (int i = 0; i < nbrLoopTest; i++) {
					m_incrementpDL(String.format("Tentative d'initialisation %d", i+1));
					test = m_sendData("0105\r", 4000);
					try {
						if (!IsConnected){
							Thread.sleep(3000);
						}
					} catch (Exception e) {}
		            Log.v("0105",test);
		            if (WaterTemp>=0){
		            	IsInitialised = true;
		            	Toast.makeText(_context, "Initialisation OK", Toast.LENGTH_SHORT).show();
		            	break;
		            }
				}
				if (!IsInitialised){
					//#region cherche Protocole
					relancerTest = false;
					wait_for_alert = true;
					alertbox = new AlertDialog.Builder(_context);
					alertbox.setTitle("Problème d'initialisation");
		            alertbox.setMessage("Voulez vous faire une recherche de protocole ?");
		            alertbox.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface arg0, int arg1) {
		                	try	{
		                			String test = m_sendData("ATSP0\r", 2000);
			        	            Log.v("ATSP0",test);
		                	}
		                	catch (Exception e) {
								// TODO: handle exception
							}
		                	relancerTest = true;
		                	wait_for_alert = false;
		                }
		            });
		            alertbox.setNegativeButton("Non", new DialogInterface.OnClickListener() {           
		                public void onClick(DialogInterface arg0, int arg1) {
		                	wait_for_alert = false;
		                	//quitter=true;
		                }
		            });
		            alertBoxShow();
		            while (wait_for_alert){}
		            //#endregion
		            //#region Question reesayer
		            if (!relancerTest){
						wait_for_alert = true;
						alertbox = new AlertDialog.Builder(_context);
						alertbox.setTitle("PAS DE REPONSE DE L OBD");
			            alertbox.setMessage("Voulez vous réessayer ?");
			            alertbox.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface arg0, int arg1) {
			                		wait_for_alert = false;                	
			                }
			            });
			            alertbox.setNegativeButton("Non", new DialogInterface.OnClickListener() {           
			                public void onClick(DialogInterface arg0, int arg1) {
			                	wait_for_alert = false;
			                	quitter = true;
			                }
			            });
			            alertBoxShow();
			            while (wait_for_alert){}
		            }
		            //#endregion
				}
	            if (quitter){
	            	protocole_name =  "ECHEC D'INITIALISATION !";
	            }			
			}
			Log.i("initialisation", protocole_name);
			if (quitter) {return;}
			test = m_sendData("ATDP\r", 1000);
            Log.v("ATDP",test);
		}
	});
	
	//#endregion
	
	//#region reception message
	@Override
	protected  void m_traiteMessage(Message msg){
		String received = msg.getData().getString("data");
		if (debug){Toast.makeText(_context, "Received = '" + received + "'", Toast.LENGTH_SHORT).show();}
		if (received.startsWith("01")){
			try {
				String value_string = received.substring(9);// car  en theorie ">" eliminé, received.length()-1);
				int value ;
				if (debug){Toast.makeText(_context, "Value string = '" + value_string + "'", Toast.LENGTH_LONG).show();}
				if (received.contains("NO DATA")){
					value=39;//-1 au final
				} 
				else {
					if (received.contains("CAN ERROR") | received.contains("NOT FOUND") | received.contains("UNABLE TO CONNECT")){
						value=38;// -2 au final							
					}
					else{
						value = m_getValue(value_string);
					}							
				}					
				int type_donnee = Integer.parseInt(received.substring(2, 4), 16);
				if (debug)
				{Toast.makeText(_context, String.format("value = %d ... type donnée = %d", value , type_donnee), Toast.LENGTH_SHORT).show();}
				switch (type_donnee) {
				case 12:
					//rpm
					RPM = value;
					break;							
				case 13:
					//vitesse
					Speed = value;
					break;
				case 05:
					//temp eau
					WaterTemp = value - 40;
					break;
				case 92:
					//temp huile
					OilTemp = value - 40;
					break;

				default:
					break;
				}
				return;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		if (received.contains("ELM327")){
			protocole_name = "ELM327";
			return;
		}
		if (received.startsWith("ATDP")){
			protocole_name = received.substring(4);//,received.length()-1);
			return;
			}
	};

	private int m_getValue(String _value) {
		try {
			if (_value.length()==2){
				return Integer.parseInt(_value, 16);
			} else
			{
				return ((Integer.parseInt(_value.substring(0, 2), 16) * 256) + 
						Integer.parseInt(_value.substring(2, 4), 16)) / 4;
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}
		
	//#endregion
	
	@Override
	protected void m_LOGThread() {
		 try {
			 Looper.prepare();
			 if (m_creeFichierLOG("ODB")==1){
				writerLOG.write("RPM,Speed,OilTemp,WaterTemp\r\n");
				start_time_LOG = System.currentTimeMillis();
				timerLOG = new Timer();
				timerLOG.scheduleAtFixedRate(new TimerTask() {						 
					int LogOil = 1; //pour faire un releve de temperature toutes les 10 mesures
					boolean LOGBusy = false; 
					
					public void run() {
						Message info =new Message();
						if (!LOGBusy){
							info.arg1=TOMAINFRAME_LOG_UPDATE;
							LOGBusy=true;
							m_sendData("010C\r", 1000);
							m_sendData("010D\r", 1000);
							if (LogOil==10){
								m_sendData("0105\r", 1000);
								m_sendData("015C\r", 1000);
								LogOil = 0;
								info.arg1 = TOMAINFRAME_LOG_SIZE;
								info.arg2 = (int)fileLOG.length();
							}
							LogOil++;
							WriteLOG( String.format("%4d,%3d,%3d,%3d,%5d", RPM,Speed,OilTemp,WaterTemp,(System.currentTimeMillis()-start_time_LOG)/1000));
							ToMainFrame.sendMessage(info);
							LOGBusy=false;
						}
					}
				},0, intervalleLOG);
				etatThreadLog = 1; 
			 }	
			 else {
				etatThreadLog = 2; 
			 }
		 }
		 catch (Exception e) {
			etatThreadLog = 2;
		}
	 }		 
}

