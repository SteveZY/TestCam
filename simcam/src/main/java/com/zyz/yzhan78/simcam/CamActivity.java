package com.zyz.yzhan78.simcam;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import java.lang.reflect.ParameterizedType;


public class CamActivity extends Activity {
    private boolean mCamPresent = false;
    private int numOfCam = 0;
    private String TAG = "CamActivity";
    private Camera mCamDev = null;
    private CameraPreview mPreview = null;
    private FrameLayout preFl;
    private Camera.Parameters params= null;
    private Button fBtn = null;
    private SeekBar pBar = null;

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mPreview)
            preFl.removeView(mPreview);
        if(null != mCamDev) {
            Log.d(TAG, "releasing camera devices!");
            mCamDev.release();
            mCamDev = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        fBtn = (Button)findViewById(R.id.button_capture);
        if (mCamPresent) {
//            numOfCam = Camera.getNumberOfCameras();
            Log.d(TAG,"getting cam device and staring preview");
            getCamInst();
            params = mCamDev.getParameters();
            params.setPreviewSize(640,480);
            //params.setRotation(180);
            //mCamDev.setParameters(params);
            mCamDev.setDisplayOrientation(270);
            mPreview = new CameraPreview(this, mCamDev);
            preFl = (FrameLayout) findViewById(R.id.camera_preview);
            preFl.addView(mPreview);

            fBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "clicked");
                    mCamDev.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean b, Camera c) {
                            Log.d(TAG, "Focused " + b);
                        }
                    });
                }
            });
        } else {
            Log.d(TAG, "no cam feature");
        }

    }

    private void getCamInst() {

        int camID=0;
        if(mCamDev == null) {
            try {
                if(Camera.getNumberOfCameras()>=1)
                    camID = 1;
                Log.d(TAG,"opening Camera "+camID);
                mCamDev = Camera.open(camID);
            } catch (Exception e) {
                Log.d(TAG, "failed to open cam dev");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCamPresent = checkCamHW();
        setContentView(R.layout.activity_cam);
        pBar = (SeekBar) findViewById(R.id.seekBar);
        pBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG,"Progress changed!");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"Start Touching!");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"Stop Touching!");
            }
        });

    }
    //@Override

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cam, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkCamHW() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            numOfCam = Camera.getNumberOfCameras();
            Log.d(TAG,"total number of camera is"+numOfCam);
            return true;
        } else {
            return false;
        }
    }
}
