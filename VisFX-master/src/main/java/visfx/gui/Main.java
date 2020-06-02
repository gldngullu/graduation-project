package visfx.gui;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import findCall.ListingAllMethods;
import findCall.MethodCallInformation;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Observable;

import static javafx.geometry.Pos.*;

public class Main extends Application {

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    private VisGraph graph;
    private ListingAllMethods listingAllMethods = new ListingAllMethods();
    private LinkedHashMap<String, String> filePathStringMap = new LinkedHashMap<>(); //<FileName, FilePath>
    private ArrayList<MethodCallInformation> currentProjectMethods = new ArrayList<>();
    private TabPane sourceCodeTabs = new TabPane();
    private File projectFile;

    @Override
    public void start(Stage primaryStage){
        Locale.setDefault(Locale.forLanguageTag("en"));
        VBox root = new VBox();

        HBox firstRow = new HBox();//firstRow includes top row of buttons
        root.getChildren().add(firstRow);
        root.setAlignment(Pos.CENTER_LEFT);
        Button openFileButton = new Button("Open New File");
        Button contactButton = new Button("Contact");
        Button informationButton = new Button("?");
        firstRow.getChildren().addAll(openFileButton, contactButton, informationButton);
        openFileButton.setOnAction(this::handleOpenFileButtonClick);

        HBox secondRow = new HBox();//secondRow includes the whole bottom part
        sourceCodeTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.getChildren().add(secondRow);

        VBox rightHalfPane = new VBox();//rightHalfPane includes settings and graph
        secondRow.getChildren().addAll(sourceCodeTabs, rightHalfPane);

        BorderPane settingLine1 = new BorderPane();//settingLine1 includes button and checkbox
        rightHalfPane.getChildren().add(settingLine1);
        Button createGraphForProject = new Button("Create graph for whole project");
        CheckBox seeConnectedMethods = new CheckBox("See connected methods on click");
        settingLine1.setLeft(createGraphForProject);
        settingLine1.setRight(seeConnectedMethods);
        settingLine1.setAlignment(seeConnectedMethods, CENTER_RIGHT);

        BorderPane settingLine2 = new BorderPane();//settingLine2 includes button and search box
        rightHalfPane.getChildren().add(settingLine2);
        Button createGraphForFile = new Button("Create graph for current class ");
        TextField searchBox = new TextField();
        settingLine2.setLeft(createGraphForFile);
        settingLine2.setRight(searchBox);
        settingLine2.setAlignment(searchBox, CENTER_RIGHT);

        rightHalfPane.getChildren().add(webView);
        //webEngine.load((getClass().getClassLoader().getResource("baseGraph.html")).toString());
        //buildGraph();

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

    private void handleOpenFileButtonClick(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Select java project to analyze");
        fileChooser.setInitialDirectory(new File("C:\\Users\\gldng\\IdeaProjects"));
        projectFile = fileChooser.showDialog(((Button)event.getSource()).getScene().getWindow());
        determineSourcePackage();
    }

    private void determineSourcePackage(){

        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Select source code directory");
        ListView filesContainer = new ListView();
        filesContainer.setPrefHeight(400);
        getListOfFiles(projectFile, "");
        filesContainer.getItems().addAll(filePathStringMap.keySet());

        Label label = new Label("Please select the source directory for the project.\nThis file is the smallest directory which includes all the packages(if there is any) of the source codes.");
        label.setId("infoLabel");
        label.setMinHeight(Region.USE_PREF_SIZE);
        Button selectButton = new Button("Choose selected directory");

        selectButton.setOnAction(e -> {
            popupWindow.close();
            try {
                listingAllMethods.findJavaFiles(filePathStringMap.get(filesContainer.getSelectionModel().getSelectedItem()));
                determineLibraries();
                printTheSourceCode();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox layout= new VBox(10);
        layout.setId("popupWindow");
        layout.getChildren().addAll(label, filesContainer, selectButton);
        Scene scene1= new Scene(layout, 400, 600);
        scene1.getStylesheets().add("mainStylesheet.css");
        popupWindow.setScene(scene1);
        popupWindow.setResizable(false);
        popupWindow.showAndWait();
    }

    private void determineLibraries(){

        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Select library directories");
        ListView<String> directoriesList = new ListView();
        directoriesList.setPrefHeight(350);

        Label label = new Label("Please select the directories for libraries that are used in your project. \n" +
                "You can either select the whole library directory(For maven \".m2\") or the single library directories one by one.\n" +
                "If there is not any used in the project, you may just skip this step");
        label.setId("infoLabel");
        label.setMinHeight(Region.USE_PREF_SIZE);
        Button selectDirectoryButton = new Button("Add library directory");
        Button doneWithDirectoriesButton = new Button("Save these libraries");

        ObservableList<String> observableDirectories = FXCollections.observableArrayList();

        ArrayList<File> libraryDirectories = new ArrayList<>();
        directoriesList.setItems(observableDirectories);

        // TODO Listview do not update

        selectDirectoryButton.setOnAction(e -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select library directory");
            fileChooser.setInitialDirectory(new File("C:\\"));
            File file = fileChooser.showDialog(((Button)e.getSource()).getScene().getWindow());
            libraryDirectories.add(file);
            observableDirectories.add("Directory name: " + file.getName() + "- Path: " +file.getPath());
            directoriesList.getItems().clear();
            directoriesList.setItems(observableDirectories);
        });



        doneWithDirectoriesButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you confirm the selected directories?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                popupWindow.close();
                for (File file : libraryDirectories) {
                    listingAllMethods.findJarFilesInDirectory(file.getPath());
                }
            }
        });

        VBox layout = new VBox(10);
        layout.setId("popupWindow");
        layout.getChildren().addAll(label, directoriesList, selectDirectoryButton, doneWithDirectoriesButton);
        Scene scene1= new Scene(layout, 500, 600);
        scene1.getStylesheets().add("mainStylesheet.css");
        popupWindow.setScene(scene1);
        popupWindow.setResizable(false);
        popupWindow.showAndWait();
    }

    private void printTheSourceCode(){
        ArrayList<CompilationUnit> classes = listingAllMethods.getParsedClasses();
        sourceCodeTabs.getTabs().clear();
        for (CompilationUnit tempClass : classes){
            String className = "";
            for (int i = tempClass.getChildNodes().size()-1; i >= 0  ; i--) {
                if(tempClass.getChildNodes().get(i) instanceof ClassOrInterfaceDeclaration) {
                    className = ((ClassOrInterfaceDeclaration) tempClass.getChildNodes().get(i)).getNameAsString();
                    break;
                }
            }
            Tab tempTab = new Tab(className);
            TextArea sourceCode = new TextArea(tempClass.toString());
            sourceCode.setEditable(false);
            tempTab.setContent(sourceCode);
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

    private void buildGraph(){
        String script = "setTheData(" + graph.getNodesJson() +  "," + graph.getEdgesJson() + ")";
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == Worker.State.SUCCEEDED)
                webEngine.executeScript(script);
        });
    }
}
