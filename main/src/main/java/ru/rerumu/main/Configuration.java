package ru.rerumu.main;

import ru.rerumu.zfs_backup_s3.backups.Generated;
import software.amazon.awssdk.regions.Region;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;


public final class Configuration {
    private final Map<String,String> env;

    public Configuration() {
        env = System.getenv();
    }

    public Region region(){
        return Region.of(env.get("ZFS_BACKUP_S3_REGION"));
    }

    public String keyId(){
        return env.get("ZFS_BACKUP_S3_ACCESS_KEY_ID");
    }
    public String secretKey(){
        return env.get("ZFS_BACKUP_S3_SECRET_ACCESS_KEY");
    }
    public Path prefix(){
        return Paths.get(env.get("ZFS_BACKUP_S3_FULL_PREFIX"));
    }
    public URI endpoint() throws URISyntaxException {
        return new URI(env.get("ZFS_BACKUP_S3_ENDPOINT_URL"));
    }
    public String storageClass(){
        return env.get("ZFS_BACKUP_S3_FULL_STORAGE_CLASS");
    }
    public int maxPartSize(){
        return Integer.parseInt(env.get("ZFS_BACKUP_S3_MAX_S3_PART_SIZE"));
    }
    public long filePartSize(){
        return Long.parseLong(env.get("ZFS_BACKUP_S3_MAX_FILE_SIZE"));
    }
    public Path tempDir(){
        return Paths.get(env.get("ZFS_BACKUP_S3_TEMP_DIR"));
    }
}
