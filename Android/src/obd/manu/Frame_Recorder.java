package obd.manu;

//http://blog.developpez.com/android23/p8541/android/creer-un-sd-card-ajouter-des-fichiers-et/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Frame_Recorder extends Activity implements SurfaceHolder.Callback{

Button myButton;
MediaRecorder mediaRecorder;
SurfaceHolder surfaceHolder;
	MediaRecorder recorder = new MediaRecorder();
boolean recording;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
      
       recording = false;
      
       mediaRecorder = new MediaRecorder();
      
       setContentView(R.layout.recording);
      
       SurfaceView myVideoView = (SurfaceView)findViewById(R.id.videoview);
       surfaceHolder = myVideoView.getHolder();
       surfaceHolder.addCallback(this);
       surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
       
       initMediaRecorder();
      
       myButton = (Button)findViewById(R.id.mybutton);
       myButton.setOnClickListener(myButtonOnClickListener);
       Toast.makeText(this,"Start mediarecorder", Toast.LENGTH_SHORT).show();
       //recorder.prepare();
   }
  
   private Button.OnClickListener myButtonOnClickListener
   = new Button.OnClickListener(){


 public void onClick(View arg0) {
  // TODO Auto-generated method stub
  if(recording){
	//  recorder.stop();
	 // recorder.release();
   mediaRecorder.stop();
  // mediaRecorder.release();
   //finish();
  }else{
	  try{
		  //recorder.prepare();
  // mediaRecorder.start();
		  String test = myButton.getText().toString();
		  if (test.equals("REC")){recorder.prepare(); myButton.setText("START");return;}
		  recorder.start();
   recording = true;
   myButton.setText("STOP");
	  writeToSDFile();
  }
	  catch (Exception e) {
		  e.printStackTrace();
	}
 }}};
  

public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 // TODO Auto-generated method stub

}

public void surfaceCreated(SurfaceHolder arg0) {
 // TODO Auto-generated method stub
 //prepareMediaRecorder();
//	Log.v("Surface created", "true");//
}

public void surfaceDestroyed(SurfaceHolder arg0) {
 // TODO Auto-generated method stub

}

private void initMediaRecorder(){
	try{
/*		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.reset();
		//recorder.setOutputFile("/data/test.mp3");
		//Toast.makeText(getApplicationContext(), "debut init", Toast.LENGTH_SHORT).show();
 mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
 //Toast.makeText(getApplicationContext(), "setaudiosource", Toast.LENGTH_SHORT).show();
       mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
     //  Toast.makeText(getApplicationContext(), "setvideosource", Toast.LENGTH_SHORT).show();
       CamcorderProfile camcorderProfile_HQ = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
     //  Toast.makeText(getApplicationContext(), "getprofile", Toast.LENGTH_SHORT).show();
       mediaRecorder.setProfile(camcorderProfile_HQ);
		//mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    //   Toast.makeText(getApplicationContext(), "setprofile", Toast.LENGTH_SHORT).show();
       mediaRecorder.setOutputFile(android.os.Environment.getRootDirectory().getAbsolutePath()+ "/video_manu_test.mp4");
     //  Toast.makeText(getApplicationContext(), "setoutput", Toast.LENGTH_SHORT).show();
       mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
     //  Toast.makeText(getApplicationContext(), "setduration", Toast.LENGTH_SHORT).show();
       mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M
  //  Toast.makeText(getApplicationContext(), "setsize", Toast.LENGTH_SHORT).show();*/
		
	    File root = android.os.Environment.getExternalStorageDirectory(); 
	    //tv.append("\nExternal file system root: "+root);
	    
	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
	    
	    File dir = new File (root.getAbsolutePath() + "/download");
	    dir.mkdirs();
	    File file = new File(dir, "test.mp4");
		
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
		String filename = file.getAbsolutePath();
		recorder.setOutputFile(filename);
		Log.v("filename",filename);
		recorder.setVideoSize(640,480);// Class_Camera.cameraSize.x, Class_Camera.cameraSize.y);
		Log.v("setvideosize","320.240");
		recorder.setPreviewDisplay(surfaceHolder.getSurface());
		Log.v("surfaceholder","initialised");
		//recorder.prepare();
}
	catch (Exception e) {
		e.printStackTrace();
	}
}

private void writeToSDFile(){
    
    // Find the root of the external storage.
    // See http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
            
    File root = android.os.Environment.getExternalStorageDirectory(); 
    //tv.append("\nExternal file system root: "+root);
    
    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
    
    File dir = new File (root.getAbsolutePath() + "/download");
    dir.mkdirs();
    File file = new File(dir, "myData.txt");

    try {
        FileOutputStream f = new FileOutputStream(file);
        PrintWriter pw = new PrintWriter(f);
        pw.println("Howdy do to you.");
        pw.println("Here is a second line.");
        pw.flush();
        pw.close();
        f.close();
        Toast.makeText (getApplicationContext(),  "Write reussi", Toast.LENGTH_SHORT).show();
    } catch (FileNotFoundException e) {
        e.printStackTrace();
        Toast.makeText (getApplicationContext(),  file + 
                        " add a WRITE_EXTERNAL_STORAGE permission to the manifest?", Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
        e.printStackTrace();
    }	
    //tv.append("\n\nFile written to "+file);
}

private void prepareMediaRecorder(){
 mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
 try {
  mediaRecorder.prepare();
 } catch (IllegalStateException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
 } catch (IOException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
 }
}
}