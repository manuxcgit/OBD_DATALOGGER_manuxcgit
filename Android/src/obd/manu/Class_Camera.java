package obd.manu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.test.IsolatedContext;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class Class_Camera 
{
	// #region declarations
	static ArrayList<CharSequence> ListeSizeVideo = new ArrayList<CharSequence>();
	static Camera mCamera;
	static Point cameraSize = new Point(320, 240); //width , height
	static boolean _ispreview = false;
	Class_UserPreferences mPref;
	Context ctx;
	// #endregion
	
	public Class_Camera(Context context){
		ctx = context;
		mPref = new Class_UserPreferences(ctx);
	}
	
	public static CharSequence[] m_getListe(){
		// #region liste les size dispo
		Log.v("ListVideoSize length = ",String.format("%d",  ListeSizeVideo.size()));
		try{
		if (ListeSizeVideo.size()==0){
			mCamera = Camera. open();
		    List<Size> tmpList =  mCamera.getParameters().getSupportedPreviewSizes();
		    for (Size size : tmpList) {
		        ListeSizeVideo.add(String.format("%dX%d", size.width,size.height));
				}
			}
		}
		catch (Exception e) {
			Log.e("ListeSizeVideo", e.getMessage());
		}
		finally{
		    try{mCamera.release();}
		    catch (Exception e) {
				// TODO: handle exception
			}
		}
	    // #endregion
		CharSequence[] result = new CharSequence[ListeSizeVideo.size()];
    	for (int i=0; i<ListeSizeVideo.size();i++)
    	{	
    		result[i]=ListeSizeVideo.get(i);
    	}
    	Log.v("ListVideoSize charseq[] = ",String.format("%d",  ListeSizeVideo.size()));
    	return result;
	}

	public boolean m_open(){
		try {
			//recupere le Size
			String sSize = mPref.m_getParam("pref_videosize");
			Log.v("pref_videosize", sSize);
			mCamera = Camera.open();
			Camera.Parameters paramCamera = mCamera.getParameters();
			if (sSize!="defaultvalue"){
				try{
					String[] values = sSize.split("X");
					cameraSize = new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
				}
				catch (Exception e) {
					Log.e("conversion size", sSize.split("X").toString() );
				}
			}
			paramCamera.setPreviewSize(cameraSize.x, cameraSize.y);
			Log.v("m_open","mCamera = " + mCamera.toString());
		} 
		catch (Exception e) {
			Log.e("m_open", e.getMessage());
		}
		return (mCamera!=null);
	}
	
	public boolean m_close(){
		try{
		mCamera.release();
		mCamera = null;
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		return (mCamera==null);
	}
	
	public void m_preview(SurfaceHolder sH, boolean startPreview){
		if (startPreview){
			Log.v("m_preview","Suface Holder = " + sH.toString());
			if (m_open() & (!_ispreview)) {
			      try {
			       mCamera.setPreviewDisplay(sH);
			       mCamera.startPreview();
			       _ispreview = true;
			       //previewing = true;
			      }// 
			      catch (IOException e) {
			       // TODO Auto-generated catch block
			       e.printStackTrace();
			      }
			  }
		}
		else {
			Log.v("m_preview_false","stop preview");
			if (_ispreview){
			mCamera.stopPreview();
			_ispreview = !m_close();
			}
		}
	}

	public void m_filme(String nomFichier, boolean filmer){
		
	}
}