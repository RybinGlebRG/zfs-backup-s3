module ru.rerumu.utils {
    requires org.apache.commons.codec;
    requires org.slf4j;
    requires org.checkerframework.checker.qual;

    exports ru.rerumu.utils.processes.factories;
    exports ru.rerumu.utils.processes.factories.impl;
    exports ru.rerumu.utils.processes;
    exports ru.rerumu.utils;
    exports ru.rerumu.utils.processes.impl;
    exports ru.rerumu.utils.callables;
    exports ru.rerumu.utils.callables.impl;
}