package com.cadsystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.ToggleButton;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label statusBar = new Label("Ready");
        ViewCanvas viewCanvas = new ViewCanvas(statusBar);


        // --- Toolbar ---
        Button zoomInBtn = new Button("Zoom +");
        zoomInBtn.setOnAction(e -> viewCanvas.zoomIn());

        Button zoomOutBtn = new Button("Zoom -");
        zoomOutBtn.setOnAction(e -> viewCanvas.zoomOut());

        Button rotateLeftBtn = new Button("Rotate Left");
        rotateLeftBtn.setOnAction(e -> viewCanvas.rotateLeft(false));

        Button rotateRightBtn = new Button("Rotate Right");
        rotateRightBtn.setOnAction(e -> viewCanvas.rotateRight(false));

        ToggleButton handToolBtn = new ToggleButton("Hand Tool");
        handToolBtn.setOnAction(e -> viewCanvas.setHandToolActive(handToolBtn.isSelected()));


        Button fitAllBtn = new Button("Fit All");
        fitAllBtn.setOnAction(e -> viewCanvas.fitAll());

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(e -> viewCanvas.resetView());

        ToolBar toolBar = new ToolBar(
            zoomInBtn, zoomOutBtn, rotateLeftBtn, rotateRightBtn, handToolBtn, fitAllBtn, resetBtn
        );

        // --- Layout ---
        BorderPane root = new BorderPane();
        root.setCenter(viewCanvas);
        root.setTop(toolBar);
        root.setBottom(statusBar);

        // Связываем размеры Canvas с размерами центральной области
        viewCanvas.widthProperty().bind(root.widthProperty());
        viewCanvas.heightProperty().bind(root.heightProperty().subtract(toolBar.heightProperty()).subtract(statusBar.heightProperty()));
        viewCanvas.widthProperty().addListener((obs, oldVal, newVal) -> viewCanvas.draw());
        viewCanvas.heightProperty().addListener((obs, oldVal, newVal) -> viewCanvas.draw());


        // --- Scene & Stage ---
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("CAD System Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

        viewCanvas.requestFocus(); // Фокус на Canvas для обработки клавиатуры
    }

    public static void main(String[] args) {
        launch(args);
    }
}
