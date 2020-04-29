package visfx.api;

import visfx.graph.VisGraph;
import visfx.gui.GraphView;
import javafx.application.Platform;
import javafx.stage.Stage;

public class VisFx{

    /**
     * Plots the given visfx.graph to the mainStage.
     * @param graph the network visfx.graph to be plotted.
     * @param mainStage the main Stage.
     */
    public static void graphNetwork(VisGraph graph , Stage mainStage){
        GraphView graphView = new GraphView(graph);
        Platform.runLater(() -> graphView.start(mainStage));
    }

}
