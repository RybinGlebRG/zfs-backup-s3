package ru.rerumu.zfs_backup_s3.backups;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Generated
public class Configuration {
    private final Properties properties;

    public Configuration() throws IOException {
        String confPath = System.getProperty( "conf.path" );
        FileInputStream is = new FileInputStream(confPath);
        InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        properties = new Properties();
        properties.load(inputStreamReader);
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }
}
