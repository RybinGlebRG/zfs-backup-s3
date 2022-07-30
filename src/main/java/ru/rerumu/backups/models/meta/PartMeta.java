package ru.rerumu.backups.models.meta;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.rerumu.backups.Generated;

import java.util.Objects;

public class PartMeta {

    private final String partName;
    private final long partSize;
    private final String datasetName;
    private final String md5Hex;

    public PartMeta(String partName, long partSize,String datasetName,String md5Hex){
        this.partName = partName;
        this.partSize = partSize;
        this.datasetName = datasetName;
        this.md5Hex = md5Hex;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getPartName() {
        return partName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public String getMd5Hex() {
        return md5Hex;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("partName",partName);
        jsonObject.put("partSize",partSize);
        jsonObject.put("datasetName",datasetName);
        jsonObject.put("md5Hex",md5Hex);
        return jsonObject;
    }


    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartMeta partMeta = (PartMeta) o;
        return partSize == partMeta.partSize
                && partName.equals(partMeta.partName)
                && datasetName.equals(partMeta.datasetName)
                && md5Hex.equals(partMeta.md5Hex);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(partName, partSize,datasetName,md5Hex);
    }
}
