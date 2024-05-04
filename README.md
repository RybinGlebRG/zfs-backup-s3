# ZFSBackupS3

## Full backup (level 0)

```bash
./zfs-backup-s3.sh --backupFull <pool name> <bucket name>
```

## Continue full backup

Files has been generated but not loaded to S3 (or loaded partially):

```bash
./zfs-backup-s3.sh --backupFull -c <snapshot name> <pool name> <bucket name>
```

## Restore

```bash
./zfs-backup-s3.sh --restore <bucket name> <pool name>
```