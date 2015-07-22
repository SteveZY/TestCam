package com.zyz.yzhan78.simcam;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by yzhan78 on 6/5/15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCam;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCam = camera;
        this.mHolder = getHolder();

        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCam.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCam.setPreviewDisplay(mHolder);
            mCam.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
