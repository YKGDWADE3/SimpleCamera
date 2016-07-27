package com.example.ykg3965.camerapractice.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.ykg3965.camerapractice.R;
import com.example.ykg3965.camerapractice.adapter.MyExpandableListViewAdapter;
import com.example.ykg3965.camerapractice.view.CameraPreview;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {
    private CameraPreview mPreview;
    private Button buttonSwitch;
    private  boolean isSettingOn = false;
    private int mCameraId = 0;
    private SharedPreferences mSharedPref;

    private ArrayList<String> mParent = new ArrayList<>();
    private HashMap<String,ArrayList<String>> mChild = new HashMap<>();
    private FrameLayout mFrameLayoutPreview;
    private FrameLayout mFrameLayoutSetting;
    private ExpandableListView mExpandableListView;
    private Camera.Parameters mParams;
    private LinearLayout linearLayout;
    public static Uri mMediaFileUri;
    public static String mMediaFileType;
    private ImageView mediaPreview = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mFrameLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mFrameLayoutSetting = (FrameLayout) findViewById(R.id.setting_frame_layout);
        linearLayout = (LinearLayout) findViewById(R.id.activity_main);
        mediaPreview = (ImageView) findViewById(R.id.media_preview);
        if (savedInstanceState!=null){
            mCameraId = savedInstanceState.getInt("cameraId");
            mMediaFileType = savedInstanceState.getString("mediaFileType");
            mMediaFileUri = savedInstanceState.getParcelable("mediaFileUri");
        }
        Log.e("mCameraId", mCameraId + "");


    }


    public void onResume() {
        super.onResume();
        if (mPreview == null) {
            initCamera();
            initOnClick();

        }
    }
    View view ;
    private void initOnClick(){
        view = new View(MainActivity.this);
        Button buttonSettings = (Button) findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSettingOn) {


                    //linearLayout.addView(view);

                    mFrameLayoutSetting.addView(mExpandableListView);
                    isSettingOn = !isSettingOn;
                } else {
                    mFrameLayoutSetting.removeView(mExpandableListView);
                    isSettingOn = !isSettingOn;
                }

            }
        });


        final Button buttonCapturePhoto = (Button) findViewById(R.id.button_capture_photo);
        buttonCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.takePicture(mediaPreview);
                //mMediaFileUri = mPreview.getOutputMediaFileUri();
                //mMediaFileType = mPreview.getOutputMediaFileType();
            }
        });

        final Button buttonCaptureVideo = (Button) findViewById(R.id.button_capture_video);
        buttonCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreview.isRecording()) {
                    mPreview.stopRecording(mediaPreview);
                    buttonCaptureVideo.setText("录像");
                    //mMediaFileUri = mPreview.getOutputMediaFileUri();
                    //mMediaFileType = mPreview.getOutputMediaFileType();
                } else {
                    if (mPreview.startRecording()) {
                        buttonCaptureVideo.setText("停止");
                    }
                }
            }
        });

        mediaPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ShowPhotoVideoActivity.class);

                intent.setDataAndType(mMediaFileUri, mMediaFileType);

                startActivityForResult(intent, 0);
            }
        });

        buttonSwitch = (Button)findViewById(R.id.button_switch_camera);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.stop();
                mFrameLayoutPreview.removeView(mPreview);
                mCameraId = 1 - mCameraId;
                initCamera();
            }
        });
    }

    private void initCamera() {

        ListView list = new ListView(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        mPreview = new CameraPreview(this,mCameraId, CameraPreview.LayoutMode.FitToParent);
        mParent = mPreview.mParent;
        mChild = mPreview.mChild;
        mFrameLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mFrameLayoutSetting = (FrameLayout) findViewById(R.id.setting_frame_layout);
        mFrameLayoutPreview.addView(mPreview);

        mExpandableListView = new ExpandableListView(this);
        final MyExpandableListViewAdapter myExpandableListViewAdapter = new MyExpandableListViewAdapter(this,mParent,mChild,mPreview.mCamera);
        mExpandableListView.setAdapter(myExpandableListViewAdapter);
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                myExpandableListViewAdapter.setSelectedItem(groupPosition);
                myExpandableListViewAdapter.notifyDataSetInvalidated();
                return false;
            }
        });

        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < mParent.size(); i++) {
                    if (i!=groupPosition){
                        mExpandableListView.collapseGroup(i);
                    }
                }
            }
        });
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String key = mParent.get(groupPosition);
                String value = null;

                //Toast.makeText(MainActivity.this, (String) myExpandableListViewAdapter.getChild(groupPosition, childPosition), Toast.LENGTH_SHORT).show();
                myExpandableListViewAdapter.notifyDataSetInvalidated();
                value = (String) myExpandableListViewAdapter.getChild(groupPosition, childPosition);
                mParams = mPreview.mCamera.getParameters();
                if (mCameraId == 0) {
                    mSharedPref = getSharedPreferences("test1", Activity.MODE_PRIVATE);

                } else {
                    mSharedPref = getSharedPreferences("test2", Activity.MODE_PRIVATE);

                }
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(key, value);
                editor.commit();


                mParams.set(key, value);
                mPreview.mCamera.stopPreview();
                mPreview.mCamera.setParameters(mParams);
                mPreview.mCamera.startPreview();

                return true;
            }
        });





    }




    public void onPause() {
        super.onPause();
        mPreview.stop();
        mFrameLayoutPreview.removeView(mPreview);
        mPreview = null;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("cameraId", mCameraId);
        outState.putParcelable("mediaFileUri", mMediaFileUri);
        outState.putString("mediaFileType", mMediaFileType);
    }

}
