package com.example.ykg3965.camerapractice.adapter;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.ykg3965.camerapractice.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ykg3965 on 2016/7/13.
 */
public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> mParentArrayList;
    private HashMap<String,ArrayList<String>> mChildHashMap;
    private Context mContext;
    private LayoutInflater mInflater;
    private Camera mCamera;
    private int selectItem = 100;



    public MyExpandableListViewAdapter(Context context,ArrayList<String> parentArrayList,
                                       HashMap<String, ArrayList<String>> childHashMap,Camera camera) {
        this.mContext = context;
        this.mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mParentArrayList = parentArrayList;
        this.mChildHashMap = childHashMap;
        this.mCamera = camera;
    }

    @Override
    public int getGroupCount() {
        return mParentArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildHashMap.get(mParentArrayList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mParentArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String key = mParentArrayList.get(groupPosition);

        return mChildHashMap.get(key).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        String key = (String) getGroup(groupPosition);
        ParentViewHolder parentViewHolder =null;
        if (convertView==null){
            parentViewHolder = new ParentViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_parent,null);
            parentViewHolder.parentTextView = (TextView) convertView.findViewById(R.id.tv_parent);
            convertView.setTag(parentViewHolder);
        }else {
            parentViewHolder = (ParentViewHolder) convertView.getTag();
            convertView.setBackgroundColor(Color.argb(79,0,0,0));
        }

        if (groupPosition==selectItem){
            convertView.setBackgroundColor(Color.argb(79, 255, 0, 0));
        }
        parentViewHolder.parentTextView.setText(key);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {

        String info = (String) getChild(groupPosition,childPosition);
        ChildViewHolder childViewHolder  = null;
        if (convertView==null){
            //LayoutInflater inflater = LayoutInflater.from(mContext);
            childViewHolder = new ChildViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_child,null);
            childViewHolder.childTextView = (TextView)convertView.findViewById(R.id.tv_child);
            convertView.setTag(childViewHolder);
        }else {
            childViewHolder = (ChildViewHolder)convertView.getTag();
            convertView.setBackgroundColor(Color.argb(79,0,0,0));
        }
        String key = mParentArrayList.get(groupPosition);
        Camera.Parameters parameters = mCamera.getParameters();
        if (info .equals(parameters.get(key))){
            convertView.setBackgroundColor(Color.argb(79, 255, 0, 0));
        }
        childViewHolder.childTextView.setText(info);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ChildViewHolder {
        TextView childTextView;
    }

    class ParentViewHolder{
        TextView parentTextView;
    }

    public void setSelectedItem(int selectedItem){
        this.selectItem = selectedItem;
    }
}
