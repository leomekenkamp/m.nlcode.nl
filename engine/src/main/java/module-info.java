module nl.nlcode.m.engine {
    requires transitive java.desktop;
    requires transitive java.prefs;
    requires org.slf4j;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires static ch.qos.logback.classic;
    requires static ch.qos.logback.core;
    requires uk.co.xfactorylibrarians.coremidi4j;

    opens nl.nlcode.m.engine to com.fasterxml.jackson.databind;
    opens nl.nlcode.marshalling to com.fasterxml.jackson.databind;
            
    exports nl.nlcode.m.engine;
    exports nl.nlcode.m.linkui;
    exports nl.nlcode.m.util;
}