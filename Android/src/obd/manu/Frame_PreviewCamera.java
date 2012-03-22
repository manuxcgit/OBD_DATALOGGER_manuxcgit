package obd.manu;


import java.io.IOException;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class Frame_PreviewCamera extends Activity implements SurfaceHolder.Callback{

 Camera camera;
 SurfaceView surfaceView;
 SurfaceHolder surfaceHolder;
 boolean previewing = false;;
 
 String stringPath = "/sdcard/samplevideo.3gp";
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.preview_video);
      
       Button buttonStartCameraPreview = (Button)findViewById(R.id.startcamerapreview);
       Button buttonStopCameraPreview = (Button)findViewById(R.id.stopcamerapreview);
      
       getWindow().setFormat(PixelFormat.UNKNOWN);
       surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
       surfaceHolder = surfaceView.getHolder();
       surfaceHolder.addCallback(this);
       surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      
       buttonStartCameraPreview.setOnClickListener(new Button.OnClickListener(){

   
   public void onClick(View v) {
    // TODO Auto-generated method stub
    if(!previewing){
     camera = Camera.open();
     if (camera != null){
      try {
       camera.setPreviewDisplay(surfaceHolder);
       camera.startPreview();
       previewing = true;
      } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
      }
     }
    }
   }});
      
       buttonStopCameraPreview.setOnClickListener(new Button.OnClickListener(){

   
   public void onClick(View v) {
    // TODO Auto-generated method stub
    if(camera != null && previewing){
     camera.stopPreview();
     camera.release();
     camera = null;
     
     previewing = false;
    }
   }});
      
   }
  
  

 
 public void surfaceChanged(SurfaceHolder holder, int format, int width,
   int height) {
  // TODO Auto-generated method stub
  
 }

 
 public void surfaceCreated(SurfaceHolder holder) {
  // TODO Auto-generated method stub
  
 }


 public void surfaceDestroyed(SurfaceHolder holder) {
  // TODO Auto-generated method stub
  
 }
}



/*
 * 
 

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class Frame_PreviewCamera extends Activity{
	
	 private Preview mPreview;
	    Camera mCamera;
	    int numberOfCameras;
	    int cameraCurrentlyLocked;

	    // The first rear facing camera
	    int defaultCameraId;
	    
	    
	    private Button bouton;
		private TextView tv;
		private Toast test;

		private LinearLayout groupeDeVue;
		
		
		

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       // setContentView(R.layout.preview_video); 
	        // Hide the window title.
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        // Create a RelativeLayout container that will hold a SurfaceView,
	        // and set it as the content of our activity.
	        mPreview = new Preview(this);
	       // setContentView(mPreview);// (R.layout.preview_video);// (mPreview);
	        
	        tv = new TextView(this);
	        bouton = new Button(this);
	        bouton.setMinimumWidth(100);
	        bouton.setText("huhu");
	        bouton.setOnClickListener(new View.OnClickListener() 
	        {
	            public void onClick(View v) 
	            {
	                // Perform action on click
	            	afficherNotif();
	            }       
	        });
	 
	        groupeDeVue = new LinearLayout(this);
	      //  groupeDeVue.addView(bouton);
	      //  groupeDeVue.addView(tv);
	        groupeDeVue.addView(mPreview);
	       // groupeDeVue.setOrientation(1);
	        setContentView(groupeDeVue);
	      //  tv.setText("Salut le monde");
	        
	        
	    }
	 
	    public void afficherNotif()
	    {
	    	test = Toast.makeText(this, "huhu", 10);
	        test.show();
	     //   mCamera = Camera.open();
	     //   cameraCurrentlyLocked = defaultCameraId;
	    /*    CameraSurfaceView sW = (CameraSurfaceView)findViewById(R.id.surfaceView1);
	        
				mCamera.setPreviewDisplay((SurfaceHolder) csw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("00",e.getMessage());
			}

	      //  mPreview.setCamera(mCamera);
	    }


	            for (int i = 0; i < numberOfCameras; i++) {
	                Camera.getCameraInfo(i, cameraInfo);
	                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
	                    defaultCameraId = i;
	                }
	            } 
	

	    @Override
	    protected void onResume() {
	        
	        super.onResume();

	        // Open the default i.e. the first rear facing camera.
	    
	        mCamera = Camera.open();
	        cameraCurrentlyLocked = defaultCameraId;
	    /*    CameraSurfaceView sW = (CameraSurfaceView)findViewById(R.id.surfaceView1);
	        
				mCamera.setPreviewDisplay((SurfaceHolder) csw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("00",e.getMessage());
			}

	        mPreview.setCamera(mCamera);  
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();

	        // Because the Camera object is a shared resource, it's very
	        // important to release it when the activity is paused.
	        if (mCamera != null) {
	            mPreview.setCamera(null);
	            mCamera.release();
	            mCamera = null;
	        }
	    }

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {

	        // Inflate our menu which can gather user input for switching camera
	        MenuInflater inflater = getMenuInflater();
	//        inflater.inflate(R.menu.camera_menu, menu);
	        return true;
	    }

	   /* @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	         Handle item selection
	        switch (item.getItemId()) {
	        case R.id.switch_cam:
	            // check for availability of multiple cameras
	            if (numberOfCameras == 1) {
	                AlertDialog.Builder builder = new AlertDialog.Builder(this);
	                builder.setMessage(this.getString(R.string.camera_alert))
	                       .setNeutralButton("Close", null);
	                AlertDialog alert = builder.create();
	                alert.show();
	                return true;
	            }

	            // OK, we have multiple cameras.
	            // Release this camera -> cameraCurrentlyLocked
	            if (mCamera != null) {
	                mCamera.stopPreview();
	                mPreview.setCamera(null);
	                mCamera.release();
	                mCamera = null;
	            }

	            // Acquire the next camera and request Preview to reconfigure
	            // parameters.
	            mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
	            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
	                    % numberOfCameras;
	            mPreview.switchCamera(mCamera);

	            // Start the preview
	            mCamera.startPreview();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	        
	    }



	 ----------------------------------------------------------------------

	
	 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
	 * to the surface. We need to center the SurfaceView because not all devices have cameras that
	 * support preview sizes at the same aspect ratio as the device's display.
	
	class Preview extends ViewGroup implements SurfaceHolder.Callback {
	    private final String TAG = "Preview";

	    SurfaceView mSurfaceView;
	    SurfaceHolder mHolder;
	    Size mPreviewSize;
	    List<Size> mSupportedPreviewSizes;
	    Camera mCamera;

	    Preview(Context context) {
	        super(context);

	        mSurfaceView = new SurfaceView(context);
	        addView(mSurfaceView);

	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = mSurfaceView.getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void setCamera(Camera camera) {
	        mCamera = camera;
	        if (mCamera != null) {
	            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
	            requestLayout();
	        }
	    }

	    public void switchCamera(Camera camera) {
	       setCamera(camera);
	       try {
	           camera.setPreviewDisplay(mHolder);
	       } catch (IOException exception) {
	           Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
	       }
	       Camera.Parameters parameters = camera.getParameters();
	       parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	       requestLayout();

	       camera.setParameters(parameters);
	    }

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        // We purposely disregard child measurements because act as a
	        // wrapper to a SurfaceView that centers the camera preview instead
	        // of stretching it.
	        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
	        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
	        setMeasuredDimension(width, height);

	        if (mSupportedPreviewSizes != null) {
	            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
	        }
	    }

	    @Override
	    protected void onLayout(boolean changed, int l, int t, int r, int b) {
	        if (changed && getChildCount() > 0) {
	            final View child = getChildAt(0);

	            final int width = r - l;
	            final int height = b - t;

	            int previewWidth = width;
	            int previewHeight = height;
	            if (mPreviewSize != null) {
	                previewWidth = mPreviewSize.width;
	                previewHeight = mPreviewSize.height;
	            }

	            // Center the child SurfaceView within the parent.
	            if (width * previewHeight > height * previewWidth) {
	                final int scaledChildWidth = previewWidth * height / previewHeight;
	                child.layout((width - scaledChildWidth) / 2, 0,
	                        (width + scaledChildWidth) / 2, height);
	            } else {
	                final int scaledChildHeight = previewHeight * width / previewWidth;
	                child.layout(0, (height - scaledChildHeight) / 2,
	                        width, (height + scaledChildHeight) / 2);
	            }
	        }
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, acquire the camera and tell it where
	        // to draw.
	        try {
	            if (mCamera != null) {
	                mCamera.setPreviewDisplay(holder);
	            }
	        } catch (IOException exception) {
	            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // Surface will be destroyed when we return, so stop the preview.
	        if (mCamera != null) {
	            mCamera.stopPreview();
	        }
	    }


	    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.1;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;

	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;

	        int targetHeight = h;

	        // Try to find an size match aspect ratio and size
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }

	        // Cannot find the one match the aspect ratio, ignore the requirement
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // Now that the size is known, set up the camera parameters and begin
	        // the preview.
	        Camera.Parameters parameters = mCamera.getParameters();
	        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	        requestLayout();

	        mCamera.setParameters(parameters);
	        mCamera.startPreview();
	    }

	}

	class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback
	{
	        private SurfaceHolder holder;
	        private Camera camera;
	        
	        public CameraSurfaceView(Context context) 
	        {
	                super(context);
	                
	                //Initiate the Surface Holder properly
	                this.holder = this.getHolder();
	                this.holder.addCallback(this);
	                this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        }
	        
	        
	        public void surfaceCreated(SurfaceHolder holder) 
	        {
	                try
	                {
	                        //Open the Camera in preview mode
	                        this.camera = Camera.open();
	                        this.camera.setPreviewDisplay(this.holder);
	                }
	                catch(IOException ioe)
	                {
	                        ioe.printStackTrace(System.out);
	                }
	        }

	        
	        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	        {
	                // Now that the size is known, set up the camera parameters and begin
	                // the preview.
	                Camera.Parameters parameters = camera.getParameters();
	                parameters.setPreviewSize(width, height);
	                camera.setParameters(parameters);
	                camera.startPreview();
	        }


	        
	        public void surfaceDestroyed(SurfaceHolder holder) 
	        {
	                // Surface will be destroyed when replaced with a new screen
	                //Always make sure to release the Camera instance
	                camera.stopPreview();
	                camera.release();
	                camera = null;
	        }
	        
	        public Camera getCamera()
	        {
	                return this.camera;
	        }
	}
*/