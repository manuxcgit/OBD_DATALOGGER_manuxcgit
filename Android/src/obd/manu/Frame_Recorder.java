package obd.manu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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
boolean recording;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
      
       recording = false;
      
       mediaRecorder = new MediaRecorder();
       initMediaRecorder();
      
       setContentView(R.layout.recording);
      
       SurfaceView myVideoView = (SurfaceView)findViewById(R.id.videoview);
       surfaceHolder = myVideoView.getHolder();
       surfaceHolder.addCallback(this);
       surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      
       myButton = (Button)findViewById(R.id.mybutton);
       myButton.setOnClickListener(myButtonOnClickListener);
   }
  
   private Button.OnClickListener myButtonOnClickListener
   = new Button.OnClickListener(){


 public void onClick(View arg0) {
  // TODO Auto-generated method stub
  if(recording){
   mediaRecorder.stop();
   mediaRecorder.release();
   finish();
  }else{
	  try{
		 //writeToSDFile();
	startRecording();	  
  // mediaRecorder.start();
  // recording = true;
   myButton.setText("STOP");
  }
	  catch (Exception e) {
		  Log.e("start rec","??");
	}
 }}};
  

public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 // TODO Auto-generated method stub

}

public void surfaceCreated(SurfaceHolder arg0) {
 // TODO Auto-generated method stub
 prepareMediaRecorder();
	Log.v("Surface created", "true");//
}

public void surfaceDestroyed(SurfaceHolder arg0) {
 // TODO Auto-generated method stub

}

private void initMediaRecorder(){
	try{
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File (root.getAbsolutePath() + "/download");
	    dir.mkdirs();
	    File file = new File(dir, "video.mp4");
		//recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
mediaRecorder.reset();
		//recorder.setOutputFile("/data/test.mp3");
		//Toast.makeText(getApplicationContext(), "debut init", Toast.LENGTH_SHORT).show();
 mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
 //Toast.makeText(getApplicationContext(), "setaudiosource", Toast.LENGTH_SHORT).show();
       mediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
     //  Toast.makeText(getApplicationContext(), "setvideosource", Toast.LENGTH_SHORT).show();
       CamcorderProfile camcorderProfile_HQ = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
     //  Toast.makeText(getApplicationContext(), "getprofile", Toast.LENGTH_SHORT).show();
       mediaRecorder.setProfile(camcorderProfile_HQ);
	//	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	//	mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    //   Toast.makeText(getApplicationContext(), "setprofile", Toast.LENGTH_SHORT).show();
       mediaRecorder.setOutputFile(file.getAbsolutePath());
     //  Toast.makeText(getApplicationContext(), "setoutput", Toast.LENGTH_SHORT).show();
       mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
     //  Toast.makeText(getApplicationContext(), "setduration", Toast.LENGTH_SHORT).show();
       mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M
  //  Toast.makeText(getApplicationContext(), "setsize", Toast.LENGTH_SHORT).show();
}
	catch (Exception e) {
		//Log.e("init",e.getMessage());
	}
}

private MediaRecorder mediaRecorder1;
private File file = null;
static final String PREFIX = "record";
static final String EXTENSION = ".3gpp";
private void startRecording() throws Exception {
	try{
        mediaRecorder1 = new MediaRecorder();
        mediaRecorder1.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder1.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder1.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
     //   mediaRecorder1.setVideoSource( (MediaRecorder.VideoSource.DEFAULT);
        if (file == null) {
            File rootDir = Environment.getExternalStorageDirectory();
            file = File.createTempFile(PREFIX, EXTENSION, rootDir);
        }
        mediaRecorder1.setOutputFile(file.getAbsolutePath());
        mediaRecorder1.prepare();
        Log.i("Camera","prepared");
     	 mediaRecorder1.setPreviewDisplay(surfaceHolder.getSurface());
         Log.i("Camera","surfaceholder");
        mediaRecorder1.start();
        Log.i("Camera","RECORD STARTED");
    }
	catch(Exception e){e.printStackTrace();}
}

private void writeToSDFile(){
    
    // Find the root of the external storage
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
    } catch (FileNotFoundException e) {
        e.printStackTrace();
        Toast.makeText (getApplicationContext(),  "******* File not found. Did you" +
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
  Log.e("illegalstate","error");
 } catch (IOException e) {
  // TODO Auto-generated catch block
  e.printStackTrace();
  Log.e("IO exception","error");
 }
}
}