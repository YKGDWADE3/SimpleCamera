package com.example.ykg3965.camerapractice.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ykg3965.camerapractice.activity.MainActivity;
import com.example.ykg3965.camerapractice.utils.ParseCameraParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ykg3965 on 2016/7/7.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public boolean isBackCameraOn = true;//当前打开的摄像头标记
    private static final String TAG = "CameraDemo";
    private SurfaceHolder mHolder;
    protected Activity mActivity;
    public Camera mCamera;
    private int mCameraId;
    private LayoutMode mLayoutMode;
    private MediaRecorder mMediaRecorder;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;
    private float oldDist = 1f;
    protected List<Camera.Size> mPreviewSizeList;
    protected List<Camera.Size> mPictureSizeList;
    private SharedPreferences mSharedPref;
    public ArrayList<String> mParent = new ArrayList<>();
    public HashMap<String,ArrayList<String>> mChild = new HashMap<>();
    protected Camera.Size mPreviewSize;
    protected Camera.Size mPictureSize;
    private int numberCameraFacingBack;
    private int numberCameraFacingFront;

    public static enum LayoutMode {
        FitToParent, // Scale to the size that no side is larger than the parent
        NoBlank // Scale to the size that no side is smaller than the parent
    }


    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public CameraPreview(Activity activity, int cameraId, LayoutMode mode) {
        super(activity); // Always necessary
        mActivity = activity;
        mLayoutMode = mode;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCameraId = cameraId;
        mCamera = Camera.open(mCameraId);
        ParseCameraParams.parseParamsByFlatten(mCamera.getParameters().flatten(), mChild, mParent);
        if (mCameraId==0){
            mSharedPref = mActivity.getSharedPreferences("test1", Activity.MODE_PRIVATE);
        }else{
            mSharedPref = mActivity.getSharedPreferences("test2", Activity.MODE_PRIVATE);
        }

        Camera.Parameters cameraParams = mCamera.getParameters();
        Log.e("ykg",cameraParams.flatten());
        if (mSharedPref.getString("hasPut","").equals("true")){
            getParamsByPref(cameraParams);
        }else{
            putParamsByPrefFirst(cameraParams);
        }

        mCamera.setParameters(cameraParams);
        /*System.out.println(cameraParams.flatten());
        mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        mPictureSizeList = cameraParams.getSupportedPictureSizes();*/
    }

    /**
     * 第一次没有保存数据时把默认值放入sharedPref
     * @param cameraParams
     */
    private void putParamsByPrefFirst(Camera.Parameters cameraParams){
        SharedPreferences.Editor editor = mSharedPref.edit();
        for (int i = 0;i<mParent.size();i++){
            String key = mParent.get(i);
            editor.putString(key,cameraParams.get(key));
        }
        editor.putString("hasPut","true");
        editor.commit();
    }
    
    private void getParamsByPref(Camera.Parameters cameraParams){
        for (int i = 0; i < mParent.size(); i++) {
            String key = mParent.get(i);
            cameraParams.set(key,mSharedPref.getString(key,""));
        }
    }


    public void takePicture(final ImageView view) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                    view.setImageURI(outputMediaFileUri);
                    MainActivity.mMediaFileUri = outputMediaFileUri;
                    MainActivity.mMediaFileType = getOutputMediaFileType();
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        });
    }

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording(final ImageView view) {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            view.setImageBitmap(thumbnail);
        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }







    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //boolean portrait = isPortrait();
        //int rotation = getCameraDisplayOrientation(isBackCameraOn?numberCameraFacingBack:numberCameraFacingFront);
        int rotation = getDisplayOrientation(mCameraId);
        Log.d("changed", "ykg");
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        if (mCameraId ==1){//前置摄像头竖屏照出来会上下颠倒
            rotation = (360-rotation)%360;
        }
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(rotation);

    }



    private boolean prepareVideoRecorder() {


        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefVideoSize = prefs.getString("video_size", "");
        String[] split = prefVideoSize.split("x");
        mMediaRecorder.setVideoSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        //int rotation = getCameraDisplayOrientation(isBackCameraOn ? numberCameraFacingBack:numberCameraFacingFront);
        int rotation = getDisplayOrientation(mCameraId);
        mMediaRecorder.setOrientationHint(rotation);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    /**
     * 预览或照片大小与屏幕适配
     * @param rotation
     */
    private void adjustDisplayRatio(int rotation) {
        Log.e("ykg","used");
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    public int getDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result=0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), TAG);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }

    private  Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private  int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private  void handleFocus(MotionEvent event, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, previewSize);

        camera.cancelAutoFocus();

        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });

        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, previewSize);

        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocus(event, mCamera);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    private  float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public boolean isPortrait() {
        return (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }




}
