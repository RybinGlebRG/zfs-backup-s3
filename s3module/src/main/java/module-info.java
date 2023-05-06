module ru.rerumu.s3module {

    requires org.slf4j;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires org.apache.commons.lang3;
    requires ru.rerumu.utils;

    exports ru.rerumu.s3.models to ru.rerumu.backup;
    exports ru.rerumu.s3 to ru.rerumu.backup;

}