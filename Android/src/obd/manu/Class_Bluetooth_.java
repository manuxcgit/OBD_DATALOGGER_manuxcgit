package obd.manu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public abstract class Class_Bluetooth_ {
	
	// #region declarations
	
	public boolean IsBusy = false;
	public boolean IsConnected = false;
	
	private static ArrayList<CharSequence> listePeriphBluetooth = new ArrayList<CharSequence>();
	BluetoothDevice device = null;
	BluetoothSocket socket = null;
	InputStream receiveStream = null;
	OutputStream sendStream = null;	
	//protected ReceiverThread receiverThread;
	protected Handler MessageReceived;
	String BT_Name;
	Context _context;

	//Handler handler;
	// #endregion

	public Class_Bluetooth_(String NomBT, Context context, Handler toMainFrame){// Handler hstatus, Handler h, Context context) {	
		//handler = null;// hstatus;		
		BT_Name = NomBT;
		_context = context;
		m_listePeriph();
	}
	
	private static void m_listePeriph() {
		// #region ajoute les periph existant
		try{
		listePeriphBluetooth.clear();
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] pairedDevices = setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
		for (int i=0;i<pairedDevices.length;i++) 
		{
			listePeriphBluetooth.add (pairedDevices[i].getName());
		}	
		}
		catch (Exception e) { Log.e("ListeperiphBT", "erreur");}
		// #endregion		
	}
	
	public boolean m_sendData(String data, int tempo) {
		if (IsBusy)return false;
		try {
			sendStream.write(data.getBytes());
	        sendStream.flush();
	        IsBusy=true;
	        long start_time = System.currentTimeMillis();
	        long elapsed_time = 0;
	        while (IsBusy & elapsed_time<tempo) {	
	        	elapsed_time = System.currentTimeMillis() - start_time;
			}	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		return IsBusy;
	}

	protected void m_setBT(){
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
			BluetoothDevice[] pairedDevices = setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
			for(int i=0;i<pairedDevices.length;i++) {
				if(pairedDevices[i].getName().contains(BT_Name)) {
					device = pairedDevices[i];
					try {
						socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
						receiveStream = socket.getInputStream();
						sendStream = socket.getOutputStream();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
	}
	
	protected abstract void m_connect() ;
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BluetoothDevice getDevice() {
		return device;
	}
	
	protected abstract class ReceiverThread extends Thread{
	}

    public static CharSequence[] m_getListeBT(){
    	//retourne la liste des bluetooth dispos
    	m_listePeriph();
    	CharSequence[] result = new CharSequence[listePeriphBluetooth.size()];
    	for (int i=0; i<listePeriphBluetooth.size();i++)
    	{	
    		result[i]=listePeriphBluetooth.get(i);
    	}
    	return result;
    }
}
