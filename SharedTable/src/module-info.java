module sharedtable {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.xml.dom;
    //requires kotlin.stdlib;
    exports com.sharedtable.view;
    exports com.sharedtable.controller;
    opens com.sharedtable.controller;
}