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
	private BluetoothDevice device = null;
	private InputStream receiveStream = null;
	private OutputStream sendStream = null;	
	private String _messageToast;
	private enumBt _typeBT;
	
	protected LOGThread LogThread;
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
	protected ReceiverThread receiverThread;
	protected Context _context;
	protected AlertDialog.Builder alertbox;
	protected FileWriter writerLOG = null;
	protected Timer timerLOG ;
	protected String BT_Name;
	protected String _receivedSplit;
	protected boolean init_terminee = false;
	protected File fileLOG;
	protected boolean wait_for_alert = false;
	protected boolean quitter = false;
	protected boolean relancerTest = false;
	
    static public final int TOMAINFRAME_LOG_UPDATE = 1;
    static public final int TOMAINFRAME_LOG_SIZE = 2;
    static public final int TOMAINFRAME_LOG_STOPPED = 3;
    static public final int TOMAINFRAME_LOG_READY = 4;
    static public final int TOMAINFRAME_LOG_MESSAGE_FROM_GPS = 5;
    static protected final int PDL_SHOW = 0;
    static protected final int PDL_DISMISS = 1;
    
    public enum enumBt { OBD, GPS };
	// #endregion

    //#region public
	public Class_Bluetooth(enumBt typeBT, String NomBT, Context context, Handler toMainFrame, String receivedSplit ){
		BT_Name = NomBT;
		_context = context;
		ToMainFrame = toMainFrame;
		_receivedSplit = receivedSplit;
		_typeBT = typeBT;
		receiverThread = new ReceiverThread();
		IsConnected = false;
		mPref = new Class_UserPreferences(_context) ;
		debug = (mPref.m_getParam("pref_debug")=="true");
		m_listePeriph();
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
    
    public void m_connect(){
    	Thread threadInitialiseBT =new Thread(new Runnable() {
			
			public void run() {
				Looper.prepare();
				m_connecteBT();
			}
		});
    	threadInitialiseBT.start();    	
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
    
    public boolean InitTerminée(){ 
    	return init_terminee;
    }
    
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
	    	writerLOG.close();	
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
    
	public void close() {
		try {
			socket.close();
			IsConnected = false;
			//if (receiverThread.isAlive()){receiverThread.interrupt();}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}    
    //#endregion
    
	//#region private
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
    //#endregion
    
    //#region protected    
    protected void m_setBT(){
		for (BluetoothDevice device : listePeriphBluetooth) {
			if (device.getName().contains(BT_Name)) {
				try {
					socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
					receiveStream = socket.getInputStream();
					sendStream = socket.getOutputStream();
					//IsConnected = true;
					toast(BT_Name + " trouvé");
					socket.connect();
					toast(BT_Name + " connecté");;
					IsConnected = true;
					receiverThread.start();
				} catch (IOException e) {
					toast(BT_Name + " NON connecté");
				}
				return;
			}
		}
		Log.v(BT_Name,"introuvable");
		toast(BT_Name + " pour BlueTooth introuvable !");
	}
    
    protected void m_incrementpDL(String newText) {	
		Message msg = pDLincrement.obtainMessage();
		Bundle b = new Bundle();
		b.putString("Text", newText);
        msg.setData(b);
        pDLincrement.sendMessage(msg);		
	}
    
    protected void toast(String text) {
    	if (debug){
			_messageToast = text;
			ShowToast.sendEmptyMessage(0);
    	}
	}
        
	protected void alertBoxShow(){
		alertboxShow.sendEmptyMessage(0);
	}	
	
	protected void WriteLOG(String toWrite) {
		 try{
			 writerLOG.write(toWrite);
			 writerLOG.flush();
		 }
		 catch (IOException e){}           
	}
	
	protected int m_creeFichierLOG(String nameBT){
		try{
		 if (!IsInitialised & !debug){	
			 	alertbox = new AlertDialog.Builder(_context);
				alertbox.setTitle("LOG Impossible");
	            alertbox.setMessage(nameBT + " non initialisé");
	            alertbox.setPositiveButton("Retour", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                		              	
	                }
	            });
				alertBoxShow();
				//etatThreadLog = 2;
				return 2;
		 }		 
		 //#region prepare fichier
		File root = android.os.Environment.getExternalStorageDirectory(); 
		File dir = new File (root.getAbsolutePath() + "/OBD");
		dir.mkdirs();
		Time now = new Time();
		now.setToNow();	
		fileLOG = new File(Environment.getExternalStorageDirectory() +"/OBD", "LOG " + nameBT + 
		            String.format(" %04d%02d%02d_%02d%02d%02d", now.year,now.month+1,now.monthDay,now.hour,now.minute,now.second) +".txt");
		fileLOG.createNewFile();
		writerLOG = new FileWriter(fileLOG,false);
		//#endregion
		return 1;
		}
		catch (Exception e) {
			//etatThreadLog = 2;
			return 2;
		}
	}
	
	protected class ReceiverThread extends Thread{
		String data = "";	
		
		protected ReceiverThread(){}	
		
		 @Override
		public void run() {
			 if (debug){
				toast("ReceiverThread " + BT_Name + " démarré");
				try{Thread.sleep(500);}
				catch (Exception ee){}
			 }
			while(IsConnected) {
				try {					
					if(receiveStream.available() > 0) {
						byte buffer[] = new byte[200];
						int k = receiveStream.read(buffer, 0, 200);
						if(k > 0) {
							byte rawdata[] = new byte[k];
							for(int i=0;i<k;i++)
								rawdata[i] = buffer[i];    									
							data = data.concat(new String(rawdata));
							if (data.contains(_receivedSplit)){
								//data = data.replace(_receivedSplit, "").trim();
								Message msg = MessageReceived.obtainMessage();
								Bundle b = new Bundle();
								String _first = data.split(_receivedSplit)[0];
								b.putString("data", _first);
				                msg.setData(b);  
				                MessageReceived.sendMessage(msg);
				                receivedData = _first;
				                IsBusy = false;
				                if (!data.endsWith(_receivedSplit)){
				                	data = data.split(_receivedSplit)[1];
				                }
				                else {
				                	data = "";
				                }
							} 
						}						
					}
				} 
				catch (IOException e) {
					toast("receiver stream "+ BT_Name + " IO erreur !");
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
			toast("receiver stream "+BT_Name+" arrété !!");
		}
	 }
	
    protected class LOGThread extends Thread{
		protected  LOGThread() {};

		public void run() {
			m_LOGThread();
		 }
    }
    //#endregion
    
    //#region abstract    
    protected abstract void m_connecteBT();
    protected abstract void m_LOGThread();
    protected abstract void m_traiteMessage(Message msg);
    //#endregion
   
    //#region handler
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
	
	Handler MessageReceived = new Handler(){
		@Override
		public void handleMessage(Message msg){
			m_traiteMessage(msg);
		}
	};
	
	Handler ShowToast = new Handler(){
		public void handleMessage(Message msg){
			Toast.makeText(_context, _messageToast, Toast.LENGTH_SHORT).show();
		}
	};
    //#endregion
}
