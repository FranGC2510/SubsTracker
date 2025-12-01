module org.dam.fcojavier.substracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires jbcrypt;
    requires java.sql;
    requires java.desktop;

    opens org.dam.fcojavier.substracker to javafx.fxml;
    opens org.dam.fcojavier.substracker.controller to javafx.fxml;
    opens org.dam.fcojavier.substracker.model to javafx.base;
    exports org.dam.fcojavier.substracker;
}