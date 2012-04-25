package obd.manu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public abstract class Class_Bluetooth {
	
	// #region declarations	
	public boolean IsBusy = false;
	public boolean IsConnected = false;
	public boolean IsInitialised = false;
	
	private static ArrayList<CharSequence> listeNomPeriphBluetooth = new ArrayList<CharSequence>();
	private static ArrayList<BluetoothDevice> listePeriphBluetooth = new ArrayList<BluetoothDevice>();
	BluetoothDevice device = null;
	BluetoothSocket socket = null;
	InputStream receiveStream = null;
	OutputStream sendStream = null;	
//	protected Handler MessageReceived;
	protected Handler ToMainFrame;
	ReceiverThread receiverThread;
	String BT_Name;
	String _receivedSplit;
	private String receivedData;
	Context _context;
	// #endregion

	public Class_Bluetooth(String NomBT, Context context, Handler toMainFrame, String receivedSplit ){// Handler hstatus, Handler h, Context context) {	
		//handler = null;// hstatus;		
		BT_Name = NomBT;
		_context = context;
		ToMainFrame = toMainFrame;
		_receivedSplit = receivedSplit;
		receiverThread = new ReceiverThread();
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
	
	/*public void m_sendData(String data){
		try {
			sendStream.write(data.getBytes());
	        sendStream.flush();
		}	        
		catch (IOException e) {
			e.printStackTrace();
		}
	}*/
    
    protected String m_sendData(String data, int tempo) {
		if (IsBusy) {return "IsBusy, nothing sent";}
		if (sendStream==null){return "OBD non connect�";}
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
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		}
		Log.v(BT_Name,"introuvable");
		Toast.makeText(_context, BT_Name + " pour OBD introuvable !", Toast.LENGTH_LONG).show();
	}
	
	protected abstract void m_connect() ;
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

/*	private BluetoothDevice getDevice() {
		return device;
	}*/
	
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
										data.replace(_receivedSplit, "");
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
