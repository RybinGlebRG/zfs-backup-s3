package ru.rerumu.backups.models.meta;

import org.json.JSONArray;
import org.json.JSONObject;

public class PartMeta {

    private final String partName;
    private final long partSize;

    public PartMeta(String partName, long partSize){
        this.partName = partName;
        this.partSize = partSize;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getPartName() {
        return partName;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("partName",partName);
        jsonObject.put("partSize",partSize);
        return jsonObject;
    }
}
