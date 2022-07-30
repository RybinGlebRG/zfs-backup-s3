package ru.rerumu.backups.models.meta;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.rerumu.backups.Generated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetMeta that = (DatasetMeta) o;
        return parts.equals(that.parts);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }
}
