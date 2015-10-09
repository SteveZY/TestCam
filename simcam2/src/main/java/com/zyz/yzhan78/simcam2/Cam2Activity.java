package com.zyz.yzhan78.simcam2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.os.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
    private CameraCaptureSession.StateCallback mSessionSttCb;
    private Builder mPrevBuilder;
    private Size mPrevSize = new Size(100, 100);
    private CameraCaptureSession mCapSession = null;
    private TextView mTv = null;
    private Float mMinFocusD;
    private CameraCaptureSession.CaptureCallback mCapcb;
    private Button mBtnCapture;
    private ImageReader mImgReader;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

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
            mCapSession.abortCaptures();
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
        //displayCodecsInfo();
        fBtn = (Button) findViewById(R.id.button_autof);
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
                        //mPrevBuilder.set(CaptureRequest.CONTROL_SCENE_MODE_);
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
        mBtnCapture = (Button) findViewById(R.id.button_capture);
        mImgReader = ImageReader.newInstance(1600, 1200, ImageFormat.JPEG, 3);
        mHdlr = new Handler();
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
        mSessionSttCb = new CameraCaptureSession.StateCallback() {
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
        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "capture is clicked");
                if (null != mCapSession) {

                    try {
                        mCapSession.capture(mCapBuilder.build(), mCapcb, mHdlr);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mImgReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                String picFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                Log.d(TAG, "Image is available and pic folder is " + picFolder);
                File file = new File(picFolder+"/Camera", "imgbyyong.jpg");
                ContentResolver cr = getContentResolver();
                Image img = reader.acquireNextImage();
                ByteBuffer jpgBuf = img.getPlanes()[0].getBuffer();

                byte[] jpgData = new byte[jpgBuf.capacity()];
                jpgBuf.get(jpgData);
                FileOutputStream fout = null;
                try {
                    file.createNewFile();
                    if(!file.canWrite())
                        return;
                    fout = new FileOutputStream(file);
                    fout.write(jpgData);
                    //update content resolver
                    ContentValues values = new ContentValues(9);
                    values.put(MediaStore.Images.ImageColumns.TITLE, "imgbyyong");
                    values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, "imgbyyong.jpg");
                    values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
                    // Clockwise rotation in degrees. 0, 90, 180, or 270.
                    values.put(MediaStore.Images.ImageColumns.ORIENTATION, "90");
                    values.put(MediaStore.Images.ImageColumns.DATA, picFolder+"/Camera/"+"imgbyyong.jpg");
                    values.put(MediaStore.Images.ImageColumns.SIZE, jpgData.length);
                    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                    //fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }


                img.close();
            }
        }, mHdlr);
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
            if(ids == null) return false;
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

        } catch (/*CameraAccessException*/Exception e) {
            return false;
        }
    }

    private Builder mCapBuilder;
    private CameraDevice.StateCallback mCameraStLsnr;

    private Builder mVideoBuilder;

    {
        mCameraStLsnr = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {//start the preview
                Log.d(TAG, "CameraDevice DeviceStsCB onOpend.");
                ArrayList<Surface> oSurface = new ArrayList<Surface>();
                mCamDev = camera;
                mPreview.getHolder().setFixedSize(mPrevSize.getWidth(), mPrevSize.getHeight());
                try {
                    mPrevBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    mCapBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    mVideoBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                } catch (CameraAccessException e) {
                   e.printStackTrace();
                }

                oSurface.add(mPreview.getHolder().getSurface());
                oSurface.add(mImgReader.getSurface());

                mPrevBuilder.addTarget(mPreview.getHolder().getSurface());
                mCapBuilder.addTarget(mImgReader.getSurface());

                //create capture session
                try {
                    camera.createCaptureSession(oSurface/*Arrays.asList(mPreview.getHolder().getSurface())*/,
                            mSessionSttCb, mHdlr);
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

    private void displayCodecsInfo(){
        StringBuffer types = new StringBuffer() ;
        MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
        MediaCodecInfo[]  infoCodecs = mcl.getCodecInfos();
        MediaCodecInfo.CodecCapabilities ccap;
        MediaCodecInfo.EncoderCapabilities encCap;
        MediaCodecInfo.VideoCapabilities vidCap;
        //List<MediaCodecInfo> cl= Arrays.asList(infoCodecs);
        Log.i(TAG, "Enumeration all the codecs...");
        for(MediaCodecInfo ci: infoCodecs /*cl*/){

            for(String t : ci.getSupportedTypes()){
                if(t.contains("video")) {
                    StringBuffer pl = new StringBuffer();
                    types.append(t);
                    //types.append(":");
                 //   Log.i(TAG, "Type is: " + (ci.isEncoder() ? "encoder" : "decoder"));
                    Log.i(TAG, "The supported type of this "+ (ci.isEncoder() ? "Encoder" : "Decoder") + " is: " + types);// + " P/L:"+ ccap.profileLevels);
                    /*if(ci.isEncoder())*/ {//an encoder
                        ccap = ci.getCapabilitiesForType(t);
                        encCap = ccap.getEncoderCapabilities();
                        vidCap = ccap.getVideoCapabilities();
                        for(MediaCodecInfo.CodecProfileLevel cpl : ccap.profileLevels){
                            pl.append(cpl.profile + "/" +cpl.level + " ");
                        }
                        Log.i(TAG, " P/L:" + pl);//ccap.profileLevels[0].profile+"/"+ccap.profileLevels[0].level );
                        pl.setLength(0);
                        Log.i(TAG,"Bitrates - "+vidCap.getBitrateRange());
                        Log.i(TAG,"Framerate - "+vidCap.getSupportedFrameRates());
                        Log.i(TAG,"Heights - "+vidCap.getSupportedHeights());
                        Log.i(TAG,"Widths - "+vidCap.getSupportedWidths());
                    }
                }
            }
            types.setLength(0);
        }

    }
}
