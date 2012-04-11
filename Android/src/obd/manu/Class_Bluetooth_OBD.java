package obd.manu;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import obd.manu.Class_Bluetooth_.ReceiverThread;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

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
				if (!m_sendData("ATZ\r",5000)){
					isInitialised=false;
					return;						
					}

				
				
				Thread.sleep(2000);
				
				
			// #endregion
			// #region initialise
				//echo off
				m_sendData("ATE0", 1000);
				m_sendData("ATSP0\r", 2000);
			// #endregion
				}
				catch (Exception e) {
					m_incrementpDL("Probleme de connection !!");
					try {
					Thread.sleep(2000);
				} catch (Exception e2) {
					// TODO: handle exception
				}
				}
		
				pDL.dismiss();
			}
	});
  
    protected class ReceiverThread extends Thread{
    	 				
    			String data = "";
    		
    			 protected ReceiverThread(){}
    			 
    			 public void run() {
    					while(true) {
    						try {
    							if(receiveStream.available() > 0) {
    								byte buffer[] = new byte[100];
    								//perd un peu de temps pour finir de charger buffer
    								
    							//	for(int i=0;i<100;i++)
    							//	{}
    								
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
				Toast.makeText(_context, received , Toast.LENGTH_SHORT).show();
				if (received.equalsIgnoreCase("atz elm327 v1.5>")){
					isInitialised=true;
					return;
				}
			}
		};
}

