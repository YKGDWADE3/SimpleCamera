package com.example.ykg3965.camerapractice.utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ykg3965 on 2016/7/14.
 */
public class ParseCameraParams {

    public static void parseParamsByFlatten(String flatten,HashMap<String,ArrayList<String>> map,
                                            ArrayList<String> parent){
        String[] settings = flatten.split(";");
        for (String string : settings) {
            //System.out.println(string);
            int index = string.indexOf("=");
            if (string.contains("supported")&& string.substring(index+1).equals("true")) {
                String key = string.substring(0, index-10);
                parent.add(key);

                addSupportedListToMap(key,map);


            }else if (string.contains("values")) {
                String key = string.substring(0, index-7);
                if (key.equals("preview-fps-range")){
                    continue;
                }
                else{
                    parent.add(key);
                    addValuesToMapByComma(string.substring(index+1), key,map);
                }



            }else if (string.contains("step")) {
                String key = string.substring(0,index-5);
                if (key.equals("sharpness")){
                    continue;
                }else if (key.equals("contrast")){
                    continue;
                }else if (key.equals("exposure-compensation")){
                    continue;
                }else if (key.equals("saturation")){
                    continue;
                }else if (key.equals("brightness")){
                    continue;
                }else if (key.equals("sce-factor")){
                    continue;
                }
                parent.add(key);
                String step = string.substring(index+1);
                addStepListToMap(key, step,map);

            }


        }
    }

    private static void addValuesToMapByComma(String string,String key,HashMap<String,ArrayList<String>> map) {
        ArrayList<String> temp = new ArrayList<>();
        String[] strings = string.split("\\,");
        for (int i = 0; i < strings.length; i++) {
            temp.add(strings[i]);
        }
        map.put(key, temp);



    }

    private  static void addSupportedListToMap(String key,HashMap<String,ArrayList<String>> map) {
        ArrayList<String> temp = new ArrayList<>();
        temp.add("true");
        temp.add("false");
        map.put(key, temp);
    }

    private static void addStepListToMap(String key,String step,HashMap<String,ArrayList<String>> map) {
        ArrayList<String> temp = new ArrayList<>();
        temp.add(step);
        map.put(key, temp);
    }

    private static void spAddValuesToMapByComma(String string,String key,HashMap<String,ArrayList<String>> map){
        ArrayList<String> temp = new ArrayList<>();
        String[] strings = string.split("\\),");
        for (int i = 0; i < strings.length; i++) {
            if (i!=strings.length-1){
                temp.add(strings[i]+")");
            }else{
                temp.add(strings[i]);
            }
        }
        map.put(key,temp);
    }
}
