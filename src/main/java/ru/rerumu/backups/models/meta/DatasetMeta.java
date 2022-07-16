package ru.rerumu.backups.models.meta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatasetMeta {

//    private final List<String> parts = new ArrayList<>();
    private final List<PartMeta> parts = new ArrayList<>();

    public DatasetMeta(JSONObject jsonObject){
        JSONArray jsonArray = jsonObject.getJSONArray("parts");

        for (int i = 0; i<jsonArray.length();i++){
            JSONObject tmp = jsonArray.getJSONObject(i);
            parts.add(
                    new PartMeta(
                            tmp.get("partName").toString(),
                            Long.parseLong(tmp.get("partSize").toString())
                    )
            );
        }
    }

    public DatasetMeta(){

    }

    public boolean isAdded(String partName){
        for (PartMeta part: parts){
            if (part.getPartName().equals(partName)){
                return true;
            }
        }
        return false;
    }

    public void addPart(PartMeta partMeta){
        parts.add(partMeta);
    }

    public List<String> getParts() {
        List<String> res = new ArrayList<>();

        for (PartMeta part: parts){
            res.add(part.getPartName());
        }

        return res;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (PartMeta part: parts){
            jsonArray.put(part.toJSONObject());
        }

        jsonObject.put("parts",jsonArray);
        return jsonObject;
    }


}
