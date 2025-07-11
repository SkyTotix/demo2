module com.example.bibliosystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    
    // Oracle Cloud Database modules
    requires java.sql;
    
    // Security
    requires jbcrypt;
    
    // JSON processing
    requires com.google.gson;

    // Ikonli modules
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.example.demo2 to javafx.fxml;
    opens com.example.demo2.controller to javafx.fxml;
    opens com.example.demo2.models to com.google.gson;
    opens com.example.demo2.models.enums to com.google.gson;
    opens com.example.demo2.config to com.google.gson;
    opens com.example.demo2.service to com.google.gson;
    
    exports com.example.demo2;
    exports com.example.demo2.models;
    exports com.example.demo2.models.enums;
    exports com.example.demo2.controller;
    exports com.example.demo2.service;
}