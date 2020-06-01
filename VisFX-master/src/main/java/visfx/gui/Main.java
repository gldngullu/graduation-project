package visfx.gui;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import visfx.graph.VisGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.CENTER_RIGHT;

public class Main extends Application {

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    private VisGraph graph;
    private ListingAllMethods listingAllMethods = new ListingAllMethods();
    private LinkedHashMap<String, String> filePathStringMap = new LinkedHashMap<>(); //<FilePath, FileName>
    private ArrayList<MethodCallInformation> currentProjectMethods = new ArrayList<>();
    private TabPane sourceCodeTabs = new TabPane();

    @Override
    public void start(Stage primaryStage){
        Locale.setDefault(Locale.forLanguageTag("en"));
        VBox root = new VBox();

        HBox firstRow = new HBox();
        root.getChildren().add(firstRow);
        root.setAlignment(Pos.CENTER_LEFT);
        Button openFileButton = new Button("Open New File");
        Button contactButton = new Button("Contact");
        Button informationButton = new Button("?");
        firstRow.getChildren().addAll(openFileButton, contactButton, informationButton);

        openFileButton.setOnAction(this::handleOpenFileButtonClick);

        HBox secondRow = new HBox();
        root.getChildren().add(secondRow);

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
        //webEngine.load((getClass().getClassLoader().getResource("baseGraph.html")).toString());
        //setGraph();

        Scene scene = new Scene(root,1280, 720);
        sourceCodeTabs.setMinWidth(scene.getWidth()*(0.4));
        sourceCodeTabs.setMaxWidth(scene.getWidth()*(0.4));
        scene.getStylesheets().add("mainStylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Best java static call graph generator ever!");
        primaryStage.show();
    }

    public void setGraph(VisGraph graph) {
        this.graph = graph;
    }

    private void handleOpenFileButtonClick(ActionEvent event)
    {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Select java project to analyze");
        fileChooser.setInitialDirectory(new File("C:\\Users\\gldng\\IdeaProjects"));
        File file = fileChooser.showDialog(((Button)event.getSource()).getScene().getWindow());
        determineSourcePackage(file);
    }

    private void determineSourcePackage(File projectFile){

        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Select source code directory");
        ListView filesContainer = new ListView();
        filesContainer.setPrefHeight(400);
        getListOfFiles(projectFile, "");
        filesContainer.getItems().addAll(filePathStringMap.keySet());

        Label label = new Label("Please select the source directory for the project.\nThis file is the smallest directory which includes all the packages.");
        Button selectButton = new Button("Choose selected directory");

        selectButton.setOnAction(e -> {
            popupWindow.close();
            try {
                currentProjectMethods = listingAllMethods.findMethodCalls(filePathStringMap.get(filesContainer.getSelectionModel().getSelectedItem().toString()));
                printTheSourceCode();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox layout= new VBox(10);
        layout.getChildren().addAll(label, filesContainer, selectButton);
        layout.setAlignment(Pos.CENTER);
        label.setAlignment(CENTER_LEFT);
        Scene scene1= new Scene(layout, 400, 600);
        popupWindow.setScene(scene1);
        popupWindow.setResizable(false);
        popupWindow.showAndWait();
    }

    private void printTheSourceCode(){
        ArrayList<CompilationUnit> classes = listingAllMethods.getAllClassesInProject();
        for (CompilationUnit tempClass : classes){
            String className = "";
            for (int i = tempClass.getChildNodes().size()-1; i >= 0  ; i--) {
                if(tempClass.getChildNodes().get(i) instanceof ClassOrInterfaceDeclaration) {
                    className = ((ClassOrInterfaceDeclaration) tempClass.getChildNodes().get(i)).getNameAsString();
                    break;
                }
            }
            Tab tempTab = new Tab(className);
            tempTab.setText(tempClass.toString());
            sourceCodeTabs.getTabs().add(tempTab);
        }
    }

    private void getListOfFiles(File projectFile, String indent){
        filePathStringMap.put( indent + "-" + projectFile.getName(), projectFile.getPath());
        if(projectFile.isDirectory()){
            File[] allFiles = projectFile.listFiles();
            for (File allFile : allFiles) {
                getListOfFiles(allFile, "\t" + indent);
            }
        }
    }

    private void setGraph(){
        String script = "setTheData(" + graph.getNodesJson() +  "," + graph.getEdgesJson() + ")";
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == Worker.State.SUCCEEDED)
                webEngine.executeScript(script);
        });
    }
}
