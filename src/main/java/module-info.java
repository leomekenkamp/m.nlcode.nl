module nl.nlcode.m {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.prefs;
    requires org.slf4j;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens nl.nlcode.m to com.fasterxml.jackson.databind;
    opens nl.nlcode.m.engine to javafx.fxml, com.fasterxml.jackson.databind;
    opens nl.nlcode.marshalling to com.fasterxml.jackson.databind;
    opens nl.nlcode.m.ui to javafx.fxml, org.testfx.junit5;
    exports nl.nlcode.m.ui;
    exports nl.nlcode.javafxutil;
}