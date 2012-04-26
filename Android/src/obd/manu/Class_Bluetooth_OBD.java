package obd.manu;

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
	private ProgressDialog pDL;
	private int RPM;
	private int Speed;
	private int WaterTemp;
	private int OilTemp;
	private int etatThreadLog = 0; //0 si pas de thread, 1 si ok, 2 si erreur
	private int nbrLoopTest = 5;
	private long intervalleLOG = 1000;
	private long start_time_LOG;
	private boolean debug = false;
	private String protocole_name = "";
	//ReceiverThread receiverThread;
	LOGThread LogThread;
	boolean wait_for_alert = false;
	boolean quitter = false;
	boolean relancerTest = false;
	Timer timerLOG ;
    File fileOBD = null;
    FileWriter writerOBD = null;
    AlertDialog.Builder alertbox;
	//boolean loggerOBD = false;
    
    static final int TOMAINFRAME_LOG_UPDATE = 1;
    static final int TOMAINFRAME_LOG_SIZE = 2;
    static final int TOMAINFRAME_LOG_STOPPED = 3;
    static final int TOMAINFRAME_LOG_READY = 4;
	//#endregion

    public Class_Bluetooth_OBD (String ClassName, Context context, Handler toMainFrame, String receivedSplit)	{
    		super(ClassName, context, toMainFrame, receivedSplit);// hstatus, h, context);
    		//receiverThread =  new ReceiverThread();
    		Class_UserPreferences mPref = new Class_UserPreferences(_context) ;
    		debug = (mPref.m_getParam("pref_debug")=="true");
    		try {
				intervalleLOG = Integer.parseInt(mPref.m_getParam("pref_periodeLOG"));
				nbrLoopTest = Integer.parseInt(mPref.m_getParam("pref_nbrtestconnection"));
			} catch (Exception e) {	}
    		if (debug){
    			Log.v("OBD","Debug on");
    			nbrLoopTest = 1;
    		}
    		this.m_connect();
    	}
    
    public boolean StartLog(){
		LogThread = new LOGThread();
		etatThreadLog = 0;
    	LogThread.start();
    	while (etatThreadLog==0){ new Runnable() {			
			public void run() {
		    	//pour lancer threadLOG et voir si tout se passe bien
			}
		};}
    	return (etatThreadLog==1);
    }
    
    public void StopLog(){
    	try{
	    	timerLOG.cancel();
	    	writerOBD.close();	
	    	Thread.sleep(1000);
    	}
    	catch (IOException e){
    		Log.e("StopLog", "erreur fichier");
    	}
    	catch (Exception e){}
    	Message info = new Message();
    	info.arg1 = TOMAINFRAME_LOG_STOPPED;
    	ToMainFrame.sendMessage(info); 
    }
    
    public int[] getValues(){
    	return new int[] {WaterTemp, OilTemp, RPM, Speed};
    }
    //#region connection
    @Override
	protected void m_connect() {
			pDL = new ProgressDialog(_context);		
			pDL.setMax(4);
			pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDL.setTitle("Initialisation OBD");
			pDL.setMessage("Essai Connection");
			pDL.show();
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
						pDL.dismiss();
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
						Thread.sleep(5000);
						pDL.dismiss();
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
		    		Message info = new Message();
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
			pDL.dismiss();
			Log.v("pDL","dismiss");
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
 	
	Handler pDLincrement = new Handler() {
	        @Override
			public void handleMessage(Message msg) {
	        	String message = msg.getData().getString("Text");
	        	pDL.setMessage(message);
	        	if (!message.startsWith("Tentative")){
	        		pDL.incrementProgressBy(1);
	        	}
	        }
	    };
  	    
	private void m_incrementpDL(String newText) {	
			Message msg = pDLincrement.obtainMessage();
			Bundle b = new Bundle();
			b.putString("Text", newText);
	        msg.setData(b);
	        pDLincrement.sendMessage(msg);		
		}
	
	private void alertBoxShow(){
		alertboxShow.sendEmptyMessage(0);
	}
	
	Handler alertboxShow = new Handler(){
		@Override
		public void handleMessage(Message msg) {
        	alertbox.show();
        }
	};
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
	
	protected class LOGThread extends Thread{	
			 boolean LOGBusy;
			 int LogOil = 1; //pour faire un releve de temperature toutes les 10 mesures
			 
			 protected  LOGThread() {};
			 
			 @Override
			public void run() {
				 try {
					 Looper.prepare();
					 if (!IsInitialised & !debug){	
						 	alertbox = new AlertDialog.Builder(_context);
							alertbox.setTitle("LOG Impossible");
				            alertbox.setMessage("OBD non initialisé");
				            alertbox.setPositiveButton("Retour", new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface arg0, int arg1) {
				                		              	
				                }
				            });
							alertBoxShow();
							etatThreadLog = 2;
							return;
						}
					 //#region prepare fichier
					File root = android.os.Environment.getExternalStorageDirectory(); 
					File dir = new File (root.getAbsolutePath() + "/OBD");
					dir.mkdirs();
					Time now = new Time();
					now.setToNow();					
					fileOBD = new File(Environment.getExternalStorageDirectory() +"/OBD", "LOG "+ 
					            String.format("%04d%02d%02d_%02d%02d%02d", now.year,now.month+1,now.monthDay,now.hour,now.minute,now.second) +".txt");
					fileOBD.createNewFile();
					writerOBD = new FileWriter(fileOBD,false);
					writerOBD.write("RPM,Speed,OilTemp,WaterTemp\r\n");
					start_time_LOG = System.currentTimeMillis();
					 //#endregion
					 timerLOG = new Timer();
					 LOGBusy = false;
					 timerLOG.scheduleAtFixedRate(new TimerTask() {					
						@Override
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
									info.arg2 = (int)fileOBD.length();
								}
								LogOil++;
								WriteLOG();
								ToMainFrame.sendMessage(info);
								LOGBusy=false;
							}
						}
					}, 0, intervalleLOG);
					etatThreadLog = 1;
				 }
				 catch (Exception e) {
					etatThreadLog = 2;
				}
			 }			 
	}
	
	public void WriteLOG() {
		 try{
			 String Result = String.format("%4d,%3d,%3d,%3d,%5d", RPM,Speed,OilTemp,WaterTemp,(System.currentTimeMillis()-start_time_LOG)/1000);
			 writerOBD.write(Result+"\r\n");
			 writerOBD.flush();
		 }
		 catch (IOException e){}           
	 }
}

