package obd.manu;

import java.util.ArrayList;

import obd.manu.R;
import obd.manu.R.id;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class Menu_Options extends Activity {
	
	private TextView textView;
	static String result = "rien";
	static boolean fini =false;
	
	public void onCreate(Bundle savedInstanceState) 
	    {
		try
		{
	        super.onCreate(savedInstanceState);
	        setContentView(R.xml.preference); 
	        //ajoute les periph bluetooth dispos dans les spinners
	       // spinnerOBD = (Spinner) findViewById(id.spinnerChoixOBD);
	        Class_Bluetooth.listePeriphBluetooth.add("ee");
	       // m_spinner(Bluetooth.listePeriphBluetooth);
	        
	        Toast.makeText(Menu_Options.this,result, Toast.LENGTH_SHORT);
	//        textView =(TextView)findViewById(id.tVOBDChoisi);
	       m_spinner(Class_Bluetooth.listePeriphBluetooth);
	        textView.setText( result.toCharArray(), 0, 4); 
		}
		catch (Exception e) {
			Toast.makeText(this, "Erreur ", Toast.LENGTH_LONG).show();
		}
	    }
	
	String m_spinner(ArrayList<CharSequence> liste)
	{
		result = "";
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_dropdown_item,liste);
    
        
        

   
        
        
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Périphériques Bluetooth :"); 
     /*   alert.setSingleChoiceItems(spinnerArrayAdapter , -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item){
                // User clicked on a radio button do some stuff 
            	Toast.makeText(menuOptions.this, String.format("%d", item), Toast.LENGTH_SHORT).show();
            	result=String.format("%d", item);
                }
        });
*/
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id)
            {
            	Toast.makeText(Menu_Options.this,"ee", Toast.LENGTH_SHORT);
        }
        });

        alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
              	Toast.makeText(Menu_Options.this,"nul", Toast.LENGTH_SHORT);
              	result="2";
                dialog.cancel();
            }
        });

//alert.show();
AlertDialog a = alert.create();
//a.show();



AlertDialog.Builder adb = new AlertDialog.Builder(this);

//On affecte la vue personnalisé que l'on a crée à notre AlertDialog


//On donne un titre à l'AlertDialog
adb.setTitle("Titre de notre boite de dialogue");

//On modifie l'icône de l'AlertDialog pour le fun ;)
adb.setIcon(android.R.drawable.ic_dialog_alert);
   adb.setSingleChoiceItems(spinnerArrayAdapter , -1, new DialogInterface.OnClickListener() {

public void onClick(DialogInterface dialog, int item){
    // User clicked on a radio button do some stuff 
	Toast.makeText(Menu_Options.this, String.format("%d", item), Toast.LENGTH_SHORT).show();
	result=String.format("%d", item);
    }
});
//On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) {

    	//Lorsque l'on cliquera sur le bouton "OK", on récupère l'EditText correspondant à notre vue personnalisée (cad à alertDialogView)
    	//EditText et = (EditText)alertDialogView.findViewById(R.id.EditText1);

    	//On affiche dans un Toast le texte contenu dans l'EditText de notre AlertDialog
    	Toast.makeText(Menu_Options.this, "ok", Toast.LENGTH_SHORT).show();
    	fini=true;
  } });

//On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
adb.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) {
    	Toast.makeText(Menu_Options.this, "echap", Toast.LENGTH_SHORT).show();
    	result="";
    	fini=true;
    	
  } });
//adb.show();



AlertDialog dial = new AlertDialog.Builder(Menu_Options.this).create();
DialogInterface.OnClickListener btListener = new DialogInterface.OnClickListener(){	

public void onClick(DialogInterface dialog, int which) {
if(which == AlertDialog.BUTTON_POSITIVE)
result="true";
else result="";
}
};

dial.setButton(AlertDialog.BUTTON_POSITIVE, "ok",btListener);
dial.setButton(AlertDialog.BUTTON_NEGATIVE, "non",btListener);
dial.show();

Toast.makeText(Menu_Options.this,"fin", Toast.LENGTH_SHORT);     
		return result;
	}
}