module ru.rerumu.utils {
    requires org.apache.commons.codec;
    requires org.slf4j;
    exports ru.rerumu.utils.processes.factories;
    exports ru.rerumu.utils.processes.factories.impl;
    exports ru.rerumu.utils.processes;
    exports ru.rerumu.utils;
}