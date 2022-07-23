package ru.rerumu.backups.models.meta;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.rerumu.backups.Generated;

import java.util.Objects;

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


    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartMeta partMeta = (PartMeta) o;
        return partSize == partMeta.partSize && partName.equals(partMeta.partName);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(partName, partSize);
    }
}
