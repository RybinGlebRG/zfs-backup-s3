module ru.rerumu.s3module {
    requires org.slf4j;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires org.apache.commons.lang3;
    requires software.amazon.awssdk.core;
    requires ru.rerumu.utils;
    exports ru.rerumu.s3.repositories.impl;
    exports ru.rerumu.s3.models;
    exports ru.rerumu.s3.factories;
    exports ru.rerumu.s3.factories.impl;
    exports ru.rerumu.s3;
    exports ru.rerumu.s3.impl;
    exports ru.rerumu.s3.repositories;
}