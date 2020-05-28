package visfx.gui;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import visfx.graph.VisGraph;

import static javafx.geometry.Pos.CENTER_RIGHT;

public class Main extends Application {

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    public Main(VisGraph graph){
        this.graph = graph;
    }

    private VisGraph graph;

    @Override
    public void start(Stage primaryStage){
        VBox root = new VBox();

        HBox firstRow = new HBox();
        root.getChildren().add(firstRow);
        root.setAlignment(Pos.CENTER_LEFT);
        Button openFileButton = new Button("Open New File");
        Button contactButton = new Button("Contact");
        Button informationButton = new Button("?");
        firstRow.getChildren().addAll(openFileButton, contactButton, informationButton);

        HBox secondRow = new HBox();
        root.getChildren().add(secondRow);

        TabPane sourceCodeTabs = new TabPane();
        Tab tab1 = new Tab("First tab");
        Tab tab2 = new Tab("Second tab");
        Tab tab3 = new Tab("Third tab");
        sourceCodeTabs.getTabs().addAll(tab1, tab2, tab3);
        VBox rightHalfPane = new VBox();
        secondRow.getChildren().addAll(sourceCodeTabs, rightHalfPane);

        BorderPane settingLine1 = new BorderPane();
        rightHalfPane.getChildren().add(settingLine1);
        Button createGraphForProject = new Button("Create graph for whole project");
        CheckBox seeConnectedMethods = new CheckBox("See connected methods on click");
        settingLine1.setLeft(createGraphForProject);
        settingLine1.setRight(seeConnectedMethods);
        settingLine1.setAlignment(seeConnectedMethods, CENTER_RIGHT);

        BorderPane settingLine2 = new BorderPane();
        rightHalfPane.getChildren().add(settingLine2);
        Button createGraphForFile = new Button("Create graph for current class ");
        TextField searchBox = new TextField();
        settingLine2.setLeft(createGraphForFile);
        settingLine2.setRight(searchBox);
        settingLine2.setAlignment(searchBox, CENTER_RIGHT);

        rightHalfPane.getChildren().add(webView);
        webEngine.load((getClass().getClassLoader().getResource("baseGraph.html")).toString());
        setGraph();

        Scene scene = new Scene(root,1280, 720);
        sourceCodeTabs.setMinWidth(scene.getWidth()*(0.4));
        sourceCodeTabs.setMaxWidth(scene.getWidth()*(0.4));
        scene.getStylesheets().add("mainStylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Best java static call graph generator ever!");
        primaryStage.show();
    }

    private void setGraph(){
        String script = "setTheData(" + graph.getNodesJson() +  "," + graph.getEdgesJson() + ")";
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == Worker.State.SUCCEEDED)
                webEngine.executeScript(script);
        });
    }
}
