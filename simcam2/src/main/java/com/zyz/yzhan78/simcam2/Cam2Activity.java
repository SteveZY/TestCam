package com.zyz.yzhan78.simcam2;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.os.Handler;

import java.util.Arrays;

import android.view.SurfaceHolder;
import android.widget.TextView;

public class Cam2Activity extends Activity {
    private boolean mCamPresent = false;
    private int numOfCam = 0;
    private String TAG = this.getClass().getSimpleName();
    private CameraDevice mCamDev = null;
    private CameraPreview mPreview = null;
    private FrameLayout preFl;
    private Camera.Parameters params = null;
    private Button fBtn = null;
    private SeekBar pBar = null;


    private CameraManager cm = null;
    private Handler mHdlr = null;
    private CameraCaptureSession.StateCallback mCaptureCallback;
    private CaptureRequest.Builder mPrevBuilder;
    private Size mPrevSize = new Size(100, 100);
    private CameraCaptureSession mCapSession = null;
    private TextView mTv = null;
    private Float mMinFocusD;
    private CameraCaptureSession.CaptureCallback mCapcb;

    public CameraManager getCameraManager() {
        return cm;
    }

    public void openCam() {
        try {
            cm.openCamera(cm.getCameraIdList()[0], mCameraStLsnr, mHdlr);
        } catch (CameraAccessException e) {
            Log.d(TAG, "Error when open camera");
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mCapSession.stopRepeating();
            mCapSession.close();
            mCamDev.close();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (null != mPreview)
            preFl.removeView(mPreview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fBtn = (Button) findViewById(R.id.button_capture);
        if (mCamPresent) {
//            numOfCam = Camera.getNumberOfCameras();
            Log.d(TAG, "add preview to main layout and prepare for preview");
            //getCamInst();
            //params = mCamDev.getParameters();
            //params.setPreviewSize(640, 480);
            //params.setRotation(180);
            //mCamDev.setParameters(params);
            //mCamDev.setDisplayOrientation(270);
            mPreview = new CameraPreview(this);// mCamDev);
            preFl = (FrameLayout) findViewById(R.id.camera_preview);
            preFl.addView(mPreview);

            fBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "clicked");
                    if (CaptureResult.CONTROL_AF_MODE_OFF !=
                            mPrevBuilder.get(CaptureRequest.CONTROL_AF_MODE)) {
                        Log.d(TAG, "disable auto focus");
                        mPrevBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_MODE_OFF);
                        ((Button) v).setText(R.string.manual_focus);
                    } else {
                        mPrevBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_MODE_AUTO);
                        mPrevBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureResult.CONTROL_AF_TRIGGER_START);
                        Log.d(TAG, "enable auto focus");
                        ((Button) v).setText(R.string.auto_focus);
                    }
                    try {
                        updatePreview(mCapSession);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
//                    mCamDev.autoFocus(new Camera.AutoFocusCallback() {
//                        @Override
//                        public void onAutoFocus(boolean b, Camera c) {
//                            Log.d(TAG, "Focused " + b);
//                        }
//                    });
                }
            });
        } else {
            Log.d(TAG, "no cam feature");
        }

    }

    @Deprecated
    private void getCamInst() {
        try {
            //mCamDev = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "failed to open cam dev");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCamPresent = checkCamHW();
        setContentView(R.layout.activity_cam2);
        pBar = (SeekBar) findViewById(R.id.seekBar);
        //pBar.getMax()
        mHdlr = new Handler();       //{
//            @Override
//            public void close() {
//
//            }
//
//            @Override
//            public void flush() {
//
//            }
//
//            @Override
//            public void publish(LogRecord record) {
//
//            }
//        };
        mTv = (TextView) findViewById(R.id.textView);
        mCaptureCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                try {
                    updatePreview(session);//startRepeating AKA preview
                    mCapSession = session;
                } catch (CameraAccessException e) {
                    Log.e(TAG, "in session callback updatePreview failed");
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                mCapSession = null;
            }
        };
        //List<Surface> outputs = new ArrayList<>();
        mCapcb = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                //Log.d(TAG, "onCaptureCompleted" + result.get(CaptureResult.CONTROL_AF_STATE));
                if (true)/*CaptureResult.CONTROL_AF_MODE_AUTO == result.get(CaptureResult.CONTROL_AF_MODE))*/ {
                    if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == result.get(CaptureResult.CONTROL_AF_STATE) ||
                            CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == result.get(CaptureResult.CONTROL_AF_STATE))
                        mTv.setText(result.get(CaptureResult.LENS_FOCUS_DISTANCE) + "");
                    //Log.d(TAG, "set focus distance to " + result.get(CaptureResult.LENS_FOCUS_DISTANCE));
                }
            }
        };
        pBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "Progress changed!" + mPrevBuilder.get(CaptureRequest.CONTROL_AF_MODE));

                if (CaptureRequest.CONTROL_AF_MODE_OFF
                        == mPrevBuilder.get(CaptureRequest.CONTROL_AF_MODE)) {
                    double fdis = ((double) progress / (double) pBar.getMax()) * (double) mMinFocusD;
                    mTv.setText(fdis + "");
                    mPrevBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) fdis);
                    try {
                        updatePreview(mCapSession);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Start Touching!");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "Stop Touching!");
            }
        });

    }

    //@Override
    //to be called by Cap Session callback once session configured(in onConfigured)
    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        Log.d(TAG, "updatePreview");

        session.setRepeatingRequest(mPrevBuilder.build(), mCapcb, mHdlr);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cam2, menu);
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
        Log.d(TAG, "Start connecting to camera");
        cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
       /* if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            numOfCam = Camera.getNumberOfCameras();
            return true;
        } else {
            return false;
        }*/
        try {
            String ids[];
            ids = cm.getCameraIdList();
            CameraCharacteristics cc = cm.getCameraCharacteristics(ids[0]);
            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //boolean ss = cc.get(CameraCharacteristics.CONTROL_SCENE_MODE_)
            mMinFocusD = cc.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

            mPrevSize = map.getOutputSizes(SurfaceHolder.class)[0];
            {
                if (mPrevSize != null)
                    Log.d(TAG, "width is" + mPrevSize.getWidth());
            }
            //cm.openCamera(ids[0], mCameraStLsnr, mHdlr);
            return true;

        } catch (CameraAccessException e) {
            return false;
        }
    }

    private CameraDevice.StateCallback mCameraStLsnr = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//start the preview
            Log.d(TAG, "CameraDevice DeviceStsCB onOpend.");
            mCamDev = camera;
            mPreview.getHolder().setFixedSize(mPrevSize.getWidth(), mPrevSize.getHeight());
            try {
                mPrevBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mPrevBuilder.addTarget(mPreview.getHolder().getSurface());
            //mMinFocus = mPrevBuilder.get(CaptureResult.)
            try {
                camera.createCaptureSession(Arrays.asList(mPreview.getHolder().getSurface()),
                        mCaptureCallback, mHdlr);
            } catch (CameraAccessException e) {
                Log.d(TAG, "failed when create Capture Session");
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "CameraDevice StsCB onDisconnnected.");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "CameraDevice StsCB onError:" + error);
        }
    };
}
