package visfx.api;

import visfx.graph.VisGraph;
import javafx.application.Platform;
import javafx.stage.Stage;
import visfx.gui.GraphView;
import visfx.gui.Main;

public class VisFx{

    /**
     * Plots the given visfx.graph to the mainStage.
     * @param graph the network visfx.graph to be plotted.
     * @param mainStage the main Stage.
     */
    public static void graphNetwork(VisGraph graph , Stage mainStage){
        Main graphView = new Main(graph);
        Platform.runLater(() -> graphView.start(mainStage));
    }

}
