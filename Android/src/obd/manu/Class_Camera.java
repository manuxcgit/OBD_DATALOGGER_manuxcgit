package obd.manu;

import java.util.ArrayList;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.widget.Toast;

public class Class_Camera
{
	static ArrayList<CharSequence> ListeSizeVideo = new ArrayList<CharSequence>();
	
	public Class_Camera(){
	}
	
	public static CharSequence[] m_getListe(){
		// #region liste les size dispo
		if (ListeSizeVideo.size()==0){
			Camera cameratest = Camera. open();
		    List<Size> tmpList =  cameratest.getParameters().getSupportedPreviewSizes();
		    for (Size size : tmpList) {
		        ListeSizeVideo.add(String.format("%dX%d", size.width,size.height));
			}
		    cameratest.release();
		}
	    // #endregion
		CharSequence[] result = new CharSequence[ListeSizeVideo.size()];
    	for (int i=0; i<ListeSizeVideo.size();i++)
    	{	
    		result[i]=ListeSizeVideo.get(i);
    	}
    	return result;
	}
	
}