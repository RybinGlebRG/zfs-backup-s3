package ru.rerumu.zfs_backup_s3.utils;

import java.lang.annotation.*;

// TODO: How to check that depended upon type has annotation?
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Synchronized {
}
