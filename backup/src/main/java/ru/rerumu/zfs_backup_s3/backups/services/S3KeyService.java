package ru.rerumu.zfs_backup_s3.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// TODO: Test
@ThreadSafe
public final class S3KeyService {
    private final static Logger logger = LoggerFactory.getLogger(S3KeyService.class);
    private final static String SNAPSHOT_PREFIX="zfs-backup-s3__level-0__";

    private static String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }
    private static String formatDate(LocalDateTime localDateTime){
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
    }

    private static LocalDateTime extractTime(String snapshotName) {
        String timeStr = snapshotName.substring(SNAPSHOT_PREFIX.length()+2);
        LocalDateTime res = LocalDateTime.parse(timeStr,DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"));
        return res;
    }

    public static String getKey(String poolName, String snapshotName, int level){
        String key = String.format(
                "%s/level-%d/%s/",
                escapeSymbols(poolName),
                level,
                escapeSymbols(snapshotName)
        );
        return key;
    }

    public static String getKey(String poolName, LocalDateTime localDateTime, int level){
        String tmp = SNAPSHOT_PREFIX+"__" +formatDate(localDateTime);
        String key = String.format(
                "%s/level-%d/%s/",
                escapeSymbols(poolName),
                level,
                tmp
        );
        return key;
    }


    public static String getKey(String poolName, int level){
        String key = String.format(
                "%s/level-%d/%s",
                escapeSymbols(poolName),
                level,
                SNAPSHOT_PREFIX
        );
        return key;
    }

    public static Optional<LocalDateTime> parseAndGetMaxDate(List<String> keys, String poolName, int level){
        Optional<String> maxDateKey = keys.stream()
                .peek(item-> logger.debug(String.format("Before filter: %s",item)))
                .filter(item -> item.matches(poolName+"/level-"+level+"/"+SNAPSHOT_PREFIX+"[0-9:T_-]+/.*"))
                .peek(item-> logger.debug(String.format("Passed filter: %s",item)))
                .max(Comparator.comparing(
                        item -> extractTime(
                                Paths.get(item).getName(2).toString()
                        )
                ));

        if (maxDateKey.isEmpty()){
            return Optional.empty();
        }

        Path keyPath = Paths.get(maxDateKey.get());
        LocalDateTime res = extractTime(
                keyPath.getName(2).toString()
        );
        return Optional.of(res);
    }
}
