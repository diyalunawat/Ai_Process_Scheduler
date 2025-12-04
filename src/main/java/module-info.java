module com.example.os {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.os to javafx.fxml;
    exports com.example.os;
    exports com.example.os.model;
    exports com.example.os.scheduler;
    exports com.example.os.ai;
}