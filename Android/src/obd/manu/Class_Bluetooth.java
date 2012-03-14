package obd.manu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.spi.CharsetProvider;
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
import android.widget.ArrayAdapter;

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
		Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
		//listePeriphBluetooth = new String[pairedDevices.length];
		for (int i=0;i<pairedDevices.length;i++) 
		{
			listePeriphBluetooth.add ((CharSequence) pairedDevices[i].getName());
		}
		
		for(int i=0;i<pairedDevices.length;i++) {
			if(pairedDevices[i].getName().contains("obd2ecu")) {
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
		
		handler = hstatus;
		
		receiverThread = new ReceiverThread(h);
		
	}
	
	public void sendData(String data) {
		sendData(data, false);
	}
	
	public void sendData(String data, boolean deleteScheduledData) {
		try {
			sendStream.write(data.getBytes());
	        sendStream.flush();
	        IsBusy=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
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

   /* public String[] m_listeBluetooth()
    {
    	String[] listePeriph = new String[10];
        // On récupère la liste des périphériques associés
        Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
         
        // On parcours la liste pour trouver notre module bluetooth
        for(int i=0;i<pairedDevices.length;i++)
        {
        	listePeriph[i]=String.format("%d ", i) + pairedDevices[i].getAddress() + " " + pairedDevices[i].getName();
       /* 	Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("receivedData", pairedDevices[i].getAddress());
            msg.setData(b);
            handler.sendMessage(msg);
            
            
            // On teste si ce périphérique contient le nom du module bluetooth connecté au microcontrôleur
            if(pairedDevices[i].getName().contains("obd2ecu"))
            {
            	//device = pairedDevices[i];
               listePeriph[i]=pairedDevices[i].getName();
               /* device = pairedDevices[i];
                try {
                    // On récupère le socket de notre périphérique
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                     
                    receiveStream = socket.getInputStream();// Canal de réception (valide uniquement après la connexion)
                    sendStream = socket.getOutputStream();// Canal d'émission (valide uniquement après la connexion)
                     
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                break;
            }
        }
        return listePeriph;
    } */
}
