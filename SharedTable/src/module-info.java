module sharedtable {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;
    requires java.desktop;
    requires javafx.swing;
    //requires kotlin.stdlib;
    exports com.sharedtable.view;
    exports com.sharedtable.controller;
    opens com.sharedtable.controller;
}