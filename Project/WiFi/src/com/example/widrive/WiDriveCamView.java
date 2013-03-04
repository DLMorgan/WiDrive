package com.example.widrive;

import java.util.List;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WiDriveCamView implements SurfaceHolder.Callback{
    public static interface CameraReadyCallback { 
        public void onCameraReady(); 
    }  

    private Camera camera_ = null;
    private SurfaceHolder surfaceHolder_ = null;
    private SurfaceView	  surfaceView_;
    CameraReadyCallback cameraReadyCb_ = null;
    private static int BACK_CAMERA = 0;
    //private static int FRONT_CAMERA = 1;
 
    private List<Camera.Size> supportedSizes; 
    private Camera.Size procSize_;

    @SuppressWarnings("deprecation")
	public WiDriveCamView(SurfaceView sv){
        surfaceView_ = sv;

        surfaceHolder_ = surfaceView_.getHolder();
        surfaceHolder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder_.addCallback(this); 
    }

    public int Width() {
        return procSize_.width;
    }

    public int Height() {
        return procSize_.height;
    }

    public void setCameraReadyCallback(CameraReadyCallback cb) {
        cameraReadyCb_ = cb;
    }

    public void StartPreview(){
        if ( camera_ == null)
            return;
        camera_.startPreview();
    }
    
    public void StopPreview(){
        if ( camera_ == null)
            return;
        camera_.stopPreview();
    }
    
    public void Release() {
        if ( camera_ != null) {
            camera_.stopPreview();
            camera_.release();
            camera_ = null;
        }
    }
    
    public void setupCamera(int wid, int hei, PreviewCallback cb) {
        procSize_.width = wid;
        procSize_.height = hei;
        
        Camera.Parameters p = camera_.getParameters();        
        p.setPreviewSize(procSize_.width, procSize_.height);
        camera_.setParameters(p);
        
        camera_.setPreviewCallback(cb);
    }

    private void setupCamera() {
        camera_ = safeCameraOpen();
        
        if (camera_!=null) {
	        procSize_ = camera_.new Size(0, 0);
	        Camera.Parameters p = camera_.getParameters();        
	       
	        supportedSizes = p.getSupportedPreviewSizes();
	        procSize_ = supportedSizes.get( supportedSizes.size()/2 );
	        p.setPreviewSize(procSize_.width, procSize_.height);
	        
	        camera_.setParameters(p);
	        try {
	            camera_.setPreviewDisplay(surfaceHolder_);
	        } catch ( Exception ex) {
	            ex.printStackTrace(); 
	        }
	        camera_.startPreview();
        }
    }
    
	private Camera safeCameraOpen() {
	    Camera c = null;
	    try {
	        c = Camera.open(BACK_CAMERA); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int w, int h){
    }
    
	@Override
    public void surfaceCreated(SurfaceHolder sh){        
        setupCamera();        
        if ( cameraReadyCb_ != null)
            cameraReadyCb_.onCameraReady();
    }
    
	@Override
    public void surfaceDestroyed(SurfaceHolder sh){
        Release();
    }
}
