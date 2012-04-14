package obd.manu;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import obd.manu.Class_Bluetooth_.ReceiverThread;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class Class_Bluetooth_OBD extends Class_Bluetooth_ {
	
	private ProgressDialog pDL;
	private int RPM;
	private int Speed;
	private int WaterTemp;
	private int OilTemp;
	private boolean isInitialised = false;
	private boolean debug = false;
	private String protocole_name = "";
	ReceiverThread receiverThread;

    public Class_Bluetooth_OBD (String ClassName, Context context, Handler toMainFrame)// Handler hstatus, Handler h, Context context)
    	{
    		super(ClassName, context, toMainFrame);// hstatus, h, context);
    		receiverThread =  new ReceiverThread();
    		Class_UserPreferences mPref = new Class_UserPreferences(_context) ;
    		debug = (mPref.m_getParam("pref_debug")=="true");
    		if (debug){Log.v("OBD","Debug on");}
    		this.m_connect();
    	}
    	
    protected void m_connect() {
			pDL = new ProgressDialog(_context);		
			pDL.setMax(4);
			pDL.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDL.setTitle("Initialisation OBD");
			pDL.setMessage("Essai Connection");
			pDL.show();
			isInitialised=false;
    		InitialiseOBD.start();
    }
    
    Thread InitialiseOBD = new Thread(new Runnable() {		
		public void run() {
			try{	
				m_setBT();
				// #region connecte
				socket.connect();
				IsConnected = true;
				receiverThread.start();
				m_incrementpDL("Connection ok, teste OBD ...");
				m_sendData("ATZ\r", 5000);
				if (!protocole_name.equals("ELM327") | IsBusy){
					//isbusy donc probleme
					isInitialised=false;
					m_incrementpDL("Echec echo ELM327");
					Thread.sleep(2000);
					pDL.dismiss();
					return;						
					}						
				// #endregion
				m_incrementpDL(m_initialise());
			}
			catch (Exception e) {
				m_incrementpDL("Probleme de connection !!");
				try {
					Thread.sleep(2000);
				} 
				catch (Exception e2) {
					// TODO: handle exception
				}
			}		
			pDL.dismiss();
		}
	});
    
	private String m_initialise() {
		while (!isInitialised) {
			m_sendData("0105\r", 1000);
			if (WaterTemp<0){
				m_sendData("ATSP0", 1000);
				
			}
		}
		//echo off
		//m_sendData("ATE0\r", 1000);
		//cherche protocole
		//m_sendData("ATSP0\r", 1000);
		m_sendData("ATDP\r", 1000);
		m_incrementpDL(protocole_name);
		Thread.sleep(2000);
		//enleve les spaces
		m_sendData("ATS0\r", 1000);
		//teste un retour de valeur temp eau
		m_sendData("0105\r", 1000);
		Thread.sleep(2000);
		return "ECHEC D'INITIALISATION !";
	}
  
    protected class ReceiverThread extends Thread{
    	 				
    			String data = "";
    		
    			 protected ReceiverThread(){}
    			 
    			 public void run() {
    					while(true) {
    						try {
    							if(receiveStream.available() > 0) {
    								byte buffer[] = new byte[100];
    								int k = receiveStream.read(buffer, 0, 100);    								
    								if(k > 0) {
    									byte rawdata[] = new byte[k];
    									for(int i=0;i<k;i++)
    										rawdata[i] = buffer[i];    									
    									data = data.concat(new String(rawdata));    									
    									if (data.endsWith(">")){
	    									Message msg = MessageReceived.obtainMessage();
	    									Bundle b = new Bundle();
	    									b.putString("data", data);
	    					                msg.setData(b);
	    					                MessageReceived.sendMessage(msg);
	    					                data = "";
	    					                IsBusy = false;
    									}
    								}
    							}
    						} catch (IOException e) {
    							e.printStackTrace();
    						}
    					}
    				}
    		 }
 	
	Handler pDLincrement = new Handler() {
	        public void handleMessage(Message msg) {
	        	pDL.setMessage(msg.getData().getString("Text"));
	            pDL.incrementProgressBy(1);
	        }
	    };
  	    
	public void m_incrementpDL(String newText) {		
			Message msg = pDLincrement.obtainMessage();
			Bundle b = new Bundle();
			b.putString("Text", newText);
	        msg.setData(b);
	        pDLincrement.sendMessage(msg);		
		}

	Handler MessageReceived = new Handler(){
			public void handleMessage(Message msg){
				String received = msg.getData().getString("data");
				if (debug){Toast.makeText(_context, received , Toast.LENGTH_SHORT).show();}
				if (received.startsWith("01")){
					try {
						String value_string = received.substring(8, received.length()-2);
						int value ;
						if (debug){Toast.makeText(_context, value_string , Toast.LENGTH_LONG).show();}
						if (received.contains("NO DATA")){
							value=-1;
						} 
						else {
							if (received.contains("CAN ERROR") | received.contains("NOT FOUND")){
								value=-2;							
							}
							else{
								value = m_getValue(value_string);
							}							
						}					
						int type_donnee = Integer.parseInt(received.substring(2, 4), 16);
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
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
				/*
				//#region rpm
				if (received.startsWith("010C")){
					if (received.length()!=13)
					{ RPM = -1; return;}
					try {
						RPM = ((Integer.parseInt(received.substring(8, 10), 16) * 256) + 
								Integer.parseInt(received.substring(10, 12), 16)) / 4;
						return;
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				//#endregion
				//#region vitesse
				if (received.startsWith("010D")){
					if (received.length()!=11)
					{ Speed = -1; return;}
					try {
						Speed = Integer.parseInt(received.substring(8, 10), 16);
						return;
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				//#endregion
				//#region temp eau
				if (received.startsWith("0105")){
					if (received.length()!=11)
					{ 
						WaterTemp = -1;
						if (received.endsWith("NO DATA>")){WaterTemp=-2;}
						return;}
					try {
						WaterTemp = Integer.parseInt(received.substring(8, 10), 16) - 40;
						return;
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
					
				//#endregion
				//#region temp huile
				if (received.startsWith("015C")){
					if (received.length()!=11)
					{ OilTemp = -1; return;}
					try {
						OilTemp = Integer.parseInt(received.substring(8, 10), 16) - 40;
						return;
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				//#endregion
				if (received.contains("ELM327 v1.5")){
					//isInitialised=true;
					protocole_name="ELM327";
					return;
					}
				if (received.startsWith("ATDP")){
					protocole_name = received.substring(4,received.length()-2);
					return;
					}
				} */

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
		};
}

