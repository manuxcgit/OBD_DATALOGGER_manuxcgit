package obd.classes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;



import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;


public abstract class Class_Bluetooth {
	
	// #region declarations		
	private static ArrayList<CharSequence> listeNomPeriphBluetooth = new ArrayList<CharSequence>();
	private static ArrayList<BluetoothDevice> listePeriphBluetooth = new ArrayList<BluetoothDevice>();
	private String receivedData;
	private enum_typeBluetooth typeBT;
	private BluetoothDevice device = null;
	private InputStream receiveStream = null;
	private OutputStream sendStream = null;	
	
	LOGThread LogThread;
	protected int etatThreadLog = 0; //0 si pas de thread, 1 si ok, 2 si erreur
	protected BluetoothSocket socket = null;
	protected Handler ToMainFrame;
	protected boolean debug = false;
	protected Class_UserPreferences mPref;
	protected ProgressDialog pDL;
	protected Message info = new Message();
	protected  boolean IsBusy = false;
	protected  boolean IsConnected = false;
	protected  boolean IsInitialised = false;
	protected  enum enum_typeBluetooth {OBD, GPS}
	protected ReceiverThread receiverThread;
	protected Context _context;
	protected AlertDialog.Builder alertbox;
	protected FileWriter writerOBD = null;
	protected Timer timerLOG ;
	protected String BT_Name;
	protected String _receivedSplit;
	protected boolean init_terminee = false;
	
    static public final int TOMAINFRAME_LOG_UPDATE = 1;
    static public final int TOMAINFRAME_LOG_SIZE = 2;
    static public final int TOMAINFRAME_LOG_STOPPED = 3;
    static public final int TOMAINFRAME_LOG_READY = 4;
    static protected final int PDL_SHOW = 0;
    static protected final int PDL_DISMISS = 1;
    public boolean InitTerminée() { return init_terminee;}
	// #endregion

	public Class_Bluetooth(String NomBT, enum_typeBluetooth tBT, Context context, Handler toMainFrame, String receivedSplit ){
		BT_Name = NomBT;
		_context = context;
		ToMainFrame = toMainFrame;
		_receivedSplit = receivedSplit;
		receiverThread = new ReceiverThread();
		IsConnected = false;
		typeBT = tBT;
		mPref = new Class_UserPreferences(_context) ;
		debug = (mPref.m_getParam("pref_debug")=="true");
		m_listePeriph();
	}
	
	private static void m_listePeriph() {
		// #region ajoute les periph existant
		try{
			listePeriphBluetooth.clear();
			listeNomPeriphBluetooth.clear();
			Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			BluetoothDevice[] pairedDevices = setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
			for (int i=0;i<pairedDevices.length;i++) 
			{
				listePeriphBluetooth.add(pairedDevices[i]);
				listeNomPeriphBluetooth.add (pairedDevices[i].getName());
			}	
		}
		catch (Exception e) {
			//si debug sans BT
			listeNomPeriphBluetooth.add("Test");
			listeNomPeriphBluetooth.add("Debug");
			Log.e("ListeperiphBT", "erreur");}
		// #endregion		
	}
	
    public static CharSequence[] m_getListeBT(){
    	//retourne la liste des bluetooth dispos
    	m_listePeriph();
    	CharSequence[] result = new CharSequence[listeNomPeriphBluetooth.size()];
    	for (int i=0; i<listeNomPeriphBluetooth.size();i++)
    	{	
    		result[i]=listeNomPeriphBluetooth.get(i);
    	}
    	return result;
    }
  
    public String m_sendData(String data, int tempo) {
		if (IsBusy) {return "IsBusy, nothing sent";}
		if (sendStream==null){return "OBD non connecté";}
		try {
			sendStream.write(data.getBytes());
	        sendStream.flush();
			IsBusy=true;
	        long start_time = System.currentTimeMillis();
	        long elapsed_time = 0;
	        while (IsBusy & elapsed_time<tempo) {	
	        	elapsed_time = System.currentTimeMillis() - start_time;
			}	        
		} catch (Exception e) {
			return "m_sendData EXCEPTION";
		}
		if (IsBusy){return "IsBusy AFTER sent";}
		else{
			return receivedData;
		}
	}

	protected void m_setBT(){
		for (BluetoothDevice device : listePeriphBluetooth) {
			if (device.getName().contains(BT_Name)) {
				try {
					socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					receiveStream = socket.getInputStream();
					sendStream = socket.getOutputStream();
					IsConnected = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		Log.v(BT_Name,"introuvable");
		Toast.makeText(_context, BT_Name + " pour OBD introuvable !", Toast.LENGTH_LONG).show();
	}
	
	public abstract void m_connect() ;
	
	public boolean StartLog(){
		LogThread = new LOGThread() {
		};
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
    
	Handler pDLincrement = new Handler() {
        @Override
		public void handleMessage(Message msg) {
        	String message = msg.getData().getString("Text");
        	pDL.setMessage(message);
        	if (!message.startsWith("Tentative")){
        		pDL.incrementProgressBy(1);
        	}
        	Log.v("pDL increment", message);
        }
    };
	
	Handler alertboxShow = new Handler(){
		@Override
		public void handleMessage(Message msg) {
        	alertbox.show();
        }
	};
    
	Handler pDLShow = new Handler(){
		@Override
		public void handleMessage(Message msg) {
        	pDL.show();
        }
	};
	
	Handler pDLDismiss = new Handler(){
		@Override
		public void handleMessage(Message msg){
			pDL.hide();
			pDL.dismiss();
        	Log.v("pDL", "Dismiss");
		}
	};
	
    protected void m_incrementpDL(String newText) {	
		Message msg = pDLincrement.obtainMessage();
		Bundle b = new Bundle();
		b.putString("Text", newText);
        msg.setData(b);
        pDLincrement.sendMessage(msg);		
	}
    
    protected abstract class LOGThread extends Thread{	

		protected  LOGThread() {};

		public void run() {
			m_LOGThread();
		 }
    }

    protected abstract void m_LOGThread();
    
	protected void alertBoxShow(){
		alertboxShow.sendEmptyMessage(0);
	}	
	
	protected void WriteLOG(String toWrite) {
	 try{
		 writerOBD.write(toWrite + "\r\n");
		 writerOBD.flush();
	 }
	 catch (IOException e){}           
}
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//#region reception message
		protected class ReceiverThread extends Thread{
				
			String data = "";
		
			protected ReceiverThread(){}
			 
			 @Override
			public void run() {
					while(receiveStream!=null) {
						try {
							if(receiveStream.available() > 0) {
								byte buffer[] = new byte[100];
								int k = receiveStream.read(buffer, 0, 100);    								
								if(k > 0) {
									byte rawdata[] = new byte[k];
									for(int i=0;i<k;i++)
										rawdata[i] = buffer[i];    									
									data = data.concat(new String(rawdata));  									
									if (data.endsWith(_receivedSplit)){
										data = data.replace(_receivedSplit, "").trim();
										Message msg = MessageReceived.obtainMessage();
										Bundle b = new Bundle();
										b.putString("data", data);
						                msg.setData(b);
						                MessageReceived.sendMessage(msg);
						                receivedData = data;
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
	
		protected abstract void m_traiteMessage(Message msg);
		
		Handler MessageReceived = new Handler(){
				@Override
				public void handleMessage(Message msg){
					m_traiteMessage(msg);
				}
		};
		
	//#endregion
}
