package ru.rerumu.backups.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupMeta {

    private final List<String> datasets = new ArrayList<>();

    public  BackupMeta(JSONObject jsonObject){
        JSONArray jsonArray = jsonObject.getJSONArray("datasets");

        for (int i = 0; i<jsonArray.length();i++){
            JSONObject tmp = jsonArray.getJSONObject(i);
            datasets.add(tmp.get("datasetName").toString());
        }
    }

    public BackupMeta(){

    }

    public void addDataset(String datasetName){
        datasets.add(datasetName);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (String dataset: datasets){
            jsonArray.put(
                    new JSONObject().put("datasetName",dataset)
            );
        }

        jsonObject.put("datasets",jsonArray);
        return jsonObject;
    }


}
