package gui;

import analyze_project.AnalyzeProject;
import analyze_project.MethodCallInformation;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSException;
import visfx.create_graph.CreateGraph;
import visfx.graph.VisGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import static javafx.geometry.Pos.*;

public class Main extends Application {

    private WebView webView = new WebView();
    private WebEngine webEngine = webView.getEngine();

    private VisGraph graph;
    private AnalyzeProject analyzeProject = new AnalyzeProject();
    private CreateGraph createGraph = new CreateGraph();
    private LinkedHashMap<String, String> filePathStringMap; //<FilePath, FileName>
    private ArrayList<MethodCallInformation> currentProjectMethods;
    private TabPane sourceCodeTabs = new TabPane();
    private File projectFile;
    private Button createGraphForProject;
    private Button addLibraryButton;
    private ListView directoriesList;
    private Label mayTakeLong;
    private Label seeConnectedMethods;
    private ArrayList<File> libraryDirectories;
    private ObservableList<String> observableDirectories;
    private TextField searchBox;
    private Button searchButton;
    private long startTime;

    @Override
    public void start(Stage primaryStage) {
        Locale.setDefault(Locale.forLanguageTag("en"));
        VBox root = new VBox();

        HBox firstRow = new HBox();//firstRow includes top row of buttons
        root.getChildren().add(firstRow);
        root.setAlignment(Pos.CENTER_LEFT);
        Button openFileButton = new Button("Open New File");
        Button contactButton = new Button("Contact");
        Button informationButton = new Button("?");
        addLibraryButton = new Button("Add library source");
        addLibraryButton.setDisable(true);
        firstRow.getChildren().addAll(openFileButton, contactButton, informationButton, addLibraryButton);
        openFileButton.setOnAction(this::selectProject);
        addLibraryButton.setOnAction(event -> determineLibraries());
        contactButton.setOnAction(this::contactPopup);
        informationButton.setOnAction(this::infoPopup);

        HBox secondRow = new HBox();//secondRow includes the whole bottom part
        sourceCodeTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.getChildren().add(secondRow);

        VBox rightHalfPane = new VBox();//rightHalfPane includes settings and graph
        secondRow.getChildren().addAll(sourceCodeTabs, rightHalfPane);

        BorderPane settingLine1 = new BorderPane();//settingLine1 includes button and checkbox
        rightHalfPane.getChildren().add(settingLine1);
        createGraphForProject = new Button("Create graph for whole project");
        createGraphForProject.setDisable(true);
        createGraphForProject.setOnAction(event -> {
            mayTakeLong.setText("This process may take long, please wait");
            getMethodCallsForProject(event);
        });
        seeConnectedMethods = new Label("You can see connected methods by clicking nodes!");
        seeConnectedMethods.setId("label");
        seeConnectedMethods.setVisible(false);
        settingLine1.setLeft(createGraphForProject);
        settingLine1.setRight(seeConnectedMethods);
        settingLine1.setAlignment(seeConnectedMethods, CENTER_RIGHT);

        BorderPane settingLine2 = new BorderPane();//settingLine2 includes button and search box
        rightHalfPane.getChildren().add(settingLine2);
        mayTakeLong = new Label();
        mayTakeLong.setId("label");
        searchBox = new TextField();
        searchBox.setPromptText("Search nodes here!");
        searchBox.setDisable(true);
        searchButton = new Button("Go!");
        searchButton.setDisable(true);
        HBox searchUnit = new HBox(searchBox, searchButton);
        settingLine2.setLeft(mayTakeLong);
        settingLine2.setRight(searchUnit);
        settingLine1.setAlignment(mayTakeLong, CENTER_LEFT);
        settingLine2.setAlignment(searchUnit, CENTER_RIGHT);

        searchButton.setOnAction(event -> {
            if (searchBox.getText().length() > 0) search(searchBox.getText());
        });

        rightHalfPane.getChildren().add(webView);

        Scene scene = new Scene(root, 1280, 720);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            sourceCodeTabs.setPrefWidth(800);
        });
        sourceCodeTabs.setMinWidth(scene.getWidth() * (0.4));
        sourceCodeTabs.setMaxWidth(scene.getWidth() * (0.4));
        scene.getStylesheets().add("mainStylesheet.css");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Best java static call graph generator ever!");
        primaryStage.show();
    }

    public void contactPopup(ActionEvent event) {
        Stage popup = new Stage();
        popup.setResizable(false);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Contact");
        Label label = new Label(
                "Contact me for reporting an error or if you have a suggestion!\n" +
                        "E-mail address: gulden.gullu@isik.edu.tr\n" +
                        "Github: gldngullu/graphgen");
        label.setId("labelOnContact");
        label.setAlignment(CENTER);
        Button closeButton = new Button("Close window");
        closeButton.setOnAction(e -> popup.close());
        VBox layout = new VBox(label, closeButton);
        layout.setId("simplePopup");
        layout.setPrefSize(500, 200);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("mainStylesheet.css");
        popup.setScene(scene);
        popup.showAndWait();
    }

    public void infoPopup(ActionEvent event) {
        Stage popup = new Stage();
        popup.setResizable(false);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Information");
        TextArea text = new TextArea();
        try {
            text.setText(new String(Files.readAllBytes(Paths.get("C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master\\src\\main\\resources\\help.txt"))));
            text.setWrapText(true);
            text.setId("info-text");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        text.setPrefSize(480, 600);
        Button closeButton = new Button("Close window");
        closeButton.setOnAction(e -> popup.close());
        VBox layout = new VBox(text, closeButton);
        layout.setId("simplePopup");
        layout.setAlignment(CENTER);
        layout.setPrefSize(500, 650);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("mainStylesheet.css");
        popup.setScene(scene);
        popup.showAndWait();
    }

    private void selectProject(ActionEvent event) {
        try {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select java project to analyze");
            fileChooser.setInitialDirectory(new File("C:\\Users\\gldng\\IdeaProjects\\Examples"));
            //fileChooser.setInitialDirectory(new File("C:\\Users\\gldng\\OneDrive\\Belgeler\\GitHub\\graduation-project\\VisFX-master"));
            projectFile = fileChooser.showDialog(((Button) event.getSource()).getScene().getWindow());
            determineSourcePackage();
        } catch (NullPointerException ignored) {
        }
        ;
    }

    private void getListOfFiles(File projectFile, String indent) {
        filePathStringMap.put(projectFile.getPath(), indent + "-" + projectFile.getName());
        if (projectFile.isDirectory()) {
            File[] allFiles = projectFile.listFiles();
            for (File allFile : allFiles) {
                getListOfFiles(allFile, "\t" + indent);
            }
        }
    }

    private void determineSourcePackage() {

        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Select source code directory");
        ListView filesContainer = new ListView();
        filesContainer.setPrefHeight(400);
        filePathStringMap = new LinkedHashMap<>();
        getListOfFiles(projectFile, "");
        filesContainer.getItems().addAll(filePathStringMap.values());
        ArrayList<String> paths = new ArrayList<>(filePathStringMap.keySet());

        Label label = new Label("Please select the source code directory of the project.\nThis file is the smallest directory which includes all the packages(if there is any) of the source codes.");
        label.setId("infoLabel");
        label.setMinHeight(Region.USE_PREF_SIZE);
        Button selectButton = new Button("Choose selected directory");

        selectButton.setOnAction(e -> {
            popupWindow.close();
            try {
                int index = filesContainer.getSelectionModel().getSelectedIndex();
                String packagePath = paths.get(index);
                analyzeProject.initializeAnalyze();
                analyzeProject.findJavaFiles(packagePath);
                analyzeProject.setPackagePath(packagePath);
                clearLibraries();
                determineLibraries();
                printTheSourceCode();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Couldn't find any source code files. \n" +
                                "Perhaps selected the wrong directory? You can try again.");
                alert.setTitle("Could not find source code");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10);
        layout.setId("popupWindow");
        layout.getChildren().addAll(label, filesContainer, selectButton);
        Scene scene1 = new Scene(layout, 400, 600);
        scene1.getStylesheets().add("mainStylesheet.css");
        popupWindow.setScene(scene1);
        popupWindow.setResizable(false);
        popupWindow.showAndWait();
    }

    private void clearLibraries() {
        directoriesList = new ListView();
        libraryDirectories = new ArrayList<>();
        observableDirectories = FXCollections.observableArrayList();
    }

    private void determineLibraries() {
        Stage popupWindow = new Stage();

        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle("Select library directories");
        directoriesList.setPrefHeight(350);

        Label label = new Label("Please select the directories for libraries that are used in your project. \n" +
                "You can either select the whole library directory(For maven \".m2\") or the single library directories one by one.\n" +
                "If there is not any used in the project, you may just skip this step");
        label.setId("infoLabel");
        label.setMinHeight(Region.USE_PREF_SIZE);
        Button selectDirectoryButton = new Button("Add library directory");
        Button doneWithDirectoriesButton = new Button("Save these libraries");

        selectDirectoryButton.setOnAction(e -> {
            try {
                DirectoryChooser fileChooser = new DirectoryChooser();
                fileChooser.setTitle("Select library directory");
                fileChooser.setInitialDirectory(new File("C:\\"));
                File file = fileChooser.showDialog(((Button) e.getSource()).getScene().getWindow());
                libraryDirectories.add(file);
                observableDirectories.add("Directory name: " + file.getName() + "- Path: " + file.getPath());
                directoriesList.setItems(observableDirectories);
            } catch (NullPointerException ex) {
                System.out.println("Did not choose a directory");
            }
        });

        doneWithDirectoriesButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you confirm the selected directories?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                popupWindow.close();
                for (File file : libraryDirectories) {
                    try {
                        analyzeProject.findJarFilesInDirectory(file.getPath());
                    } catch (NullPointerException ex) {
                        System.out.println("Did not choose a directory");
                    }
                }
            }
        });

        VBox layout = new VBox(10);
        layout.setId("popupWindow");
        layout.getChildren().addAll(label, directoriesList, selectDirectoryButton, doneWithDirectoriesButton);
        Scene scene1 = new Scene(layout, 500, 600);
        scene1.getStylesheets().add("mainStylesheet.css");
        Platform.runLater(() -> {
            popupWindow.setScene(scene1);
            popupWindow.setResizable(false);
            popupWindow.showAndWait();
        });
    }

    private void printTheSourceCode() {
        ArrayList<CompilationUnit> classes = analyzeProject.getParsedClasses();
        sourceCodeTabs.getTabs().clear();
        for (CompilationUnit tempClass : classes) {
            String className = "";
            for (int i = tempClass.getChildNodes().size() - 1; i >= 0; i--) {
                if (tempClass.getChildNodes().get(i) instanceof ClassOrInterfaceDeclaration) {
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
        createGraphForProject.setDisable(false);
        addLibraryButton.setDisable(false);
    }

    private void clearSearchData() {
        String script = "clearSearchData()";
        webEngine.executeScript(script);
    }

    private void search(String searchData) {
        clearSearchData();
        String script = "search('" + searchData + "')";
        webEngine.executeScript(script);
    }

    private void getMethodCallsForProject(ActionEvent e) {
        startTime = System.nanoTime();
        Thread thread = new Thread(() -> {
            currentProjectMethods = analyzeProject.findMethodCalls();
            graph = createGraph.buildGraph(currentProjectMethods, analyzeProject.getQualifiedClassNames());
            buildGraph();
        });
        thread.start();
    }

    private void buildGraph() {
        Platform.runLater(() -> {
            mayTakeLong.setText("");
            seeConnectedMethods.setVisible(true);
            searchBox.setDisable(false);
            searchButton.setDisable(false);
            webEngine.load((getClass().getClassLoader().getResource("baseGraph.html")).toString());
            String script = "setTheData(" + graph.getNodesJson() + "," + graph.getEdgesJson() + ")";
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    try {
                        webEngine.executeScript(script);
                    } catch (JSException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Could not create the graph");
                        alert.setHeaderText(null);
                        alert.setContentText("There was an error occurred with rendering the graph:\n" + ex.getMessage() +
                                "\nError is valid if only the graph is not created.");
                        alert.showAndWait();
                        webEngine = webView.getEngine();
                    }
                }
            });
            long endTime = System.nanoTime();
            System.out.println("Execution time:" + ((endTime - startTime) / 1000000) + " milliseconds");
        });
    }

    public void setGraph(VisGraph graph) {
        this.graph = graph;
    }
}
