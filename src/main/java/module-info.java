module com.bupa.bupadigitalsystem {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires java.desktop;
    requires mysql.connector.java;
    requires jbcrypt;
    requires com.fasterxml.uuid;
    requires org.apache.poi.ooxml;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires mail;
    requires com.opencsv;

    opens com.controller to javafx.fxml;
    opens constructors to javafx.fxml;
    exports com.controller;
    exports constructors;
}