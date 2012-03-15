package obd.manu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

import android.R.string;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class Class_Bluetooth {
	
	public boolean IsBusy = false;
	public static ArrayList<CharSequence> listePeriphBluetooth = new ArrayList<CharSequence>();

	private BluetoothDevice device = null;
	private BluetoothSocket socket = null;
	private InputStream receiveStream = null;
	private OutputStream sendStream = null;
	
	private ReceiverThread receiverThread;

	Handler handler;

	public Class_Bluetooth(Handler hstatus, Handler h) {
	
		// #region ajoute les periph existant		
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
		if (listePeriphBluetooth.isEmpty())
		{
			for (int i=0;i<pairedDevices.length;i++) 
			{
				listePeriphBluetooth.add ((CharSequence) pairedDevices[i].getName());
			}
		}	
		// #endregion
		
		handler = hstatus;		
		receiverThread = new ReceiverThread(h);	
	}
	
	public void m_sendData(String data) {
		m_sendData(data, false);
	}
	
	public void m_sendData(String data, boolean deleteScheduledData) {
		try {
			sendStream.write(data.getBytes());
	        sendStream.flush();
	        IsBusy=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void m_setBT (String nameBT)	{
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
		for(int i=0;i<pairedDevices.length;i++) {
			if(pairedDevices[i].getName().contains(nameBT)) {
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
	
	public void m_connect() {
		new Thread() {
			@Override public void run() {
				try {
					socket.connect();
					
					Message msg = handler.obtainMessage();
					msg.arg1 = 1;
	                handler.sendMessage(msg);
	                
					receiverThread.start();
					
				} catch (IOException e) {
					Log.v("N", "Connection Failed : "+e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BluetoothDevice getDevice() {
		return device;
	}
	
	private class ReceiverThread extends Thread {
		Handler handler;
		
		ReceiverThread(Handler h) {
			handler = h;
		}
		
		@Override public void run() {
			while(true) {
				try {
					if(receiveStream.available() > 0) {

						byte buffer[] = new byte[100];
						//perd un peu de temps pour finir de charger buffer
						
						for(int i=0;i<100;i++)
						{}
						
						int k = receiveStream.read(buffer, 0, 100);
						
						if(k > 0) {
							byte rawdata[] = new byte[k];
							for(int i=0;i<k;i++)
								rawdata[i] = buffer[i];
							
							String data = new String(rawdata);

							Message msg = handler.obtainMessage();
							Bundle b = new Bundle();
							b.putString("receivedData", data);
			                msg.setData(b);
			                handler.sendMessage(msg);
			                IsBusy = (data.endsWith(">"));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

    public static CharSequence[] m_getListeBT(){
    	CharSequence[] result = new CharSequence[listePeriphBluetooth.size()];
    	for (int i=0; i<listePeriphBluetooth.size();i++)
    	{	
    		result[i]=listePeriphBluetooth.get(i);
    	}
    	return result;
    }
}
