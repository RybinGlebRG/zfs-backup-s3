package ru.rerumu.zfs_backup_s3.utils;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public record ImmutableMap(@NonNull Map<String,String> map) {
    public ImmutableMap {
        Objects.requireNonNull(map);
        map = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement)-> existing+", "+replacement,
                        HashMap::new));
    }

    public Map<String, String> map() {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement)-> existing+", "+replacement,
                        HashMap::new));
    }
}
