package com.example.widrive;

import java.nio.ByteBuffer;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class WiDriveCamActivity extends Activity implements WiDriveCamView.CameraReadyCallback {
  private WiDriveCamView cameraView_;
  
  boolean inProcessing = false;
  final int maxVideoNumber = 3;
  WiDriveOutStream[] videoFrames = new WiDriveOutStream[maxVideoNumber];
  byte[] preFrame = new byte[1024*1024*8];
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_NO_TITLE);
      Window win = getWindow();
      win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
      win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      
      setContentView(R.layout.wicamsurface);
      
      for(int i = 0; i < maxVideoNumber; i++) {
          videoFrames[i] = new WiDriveOutStream(1024*1024*2);        
      }    
      initCamera();
  }
  
  @Override
  public void onCameraReady() {
          int w = cameraView_.Width();
          int h = cameraView_.Height();
          cameraView_.StopPreview();
          cameraView_.setupCamera(w, h, previewCb_);
          cameraView_.StartPreview();
  }

  @Override
  public void onDestroy(){
      super.onDestroy();
  }   

  @Override
  public void onStart(){
      super.onStart();
  }   

  @Override
  public void onResume(){
      super.onResume();
  }   
  
  @Override
  public void onPause(){  
      super.onPause();
      cameraView_.StopPreview(); 

      finish();
  }  
  
  @Override
  public void onBackPressed() {
      super.onBackPressed();
  }

  private void initCamera() {
      SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.MySurface);
      cameraView_ = new WiDriveCamView(cameraSurface);        
      cameraView_.setCameraReadyCallback(this);
  }

  private PreviewCallback previewCb_ = new PreviewCallback() {
      public void onPreviewFrame(byte[] frame, Camera c) {
         
              int picWidth = cameraView_.Width();
              int picHeight = cameraView_.Height(); 
              ByteBuffer bbuffer = ByteBuffer.wrap(frame); 
              bbuffer.get(preFrame, 0, picWidth*picHeight + picWidth*picHeight/2);

      }
  };
}