/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;

import fun3kochfractalfx.FUN3KochFractalFX;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import timeutil.TimeStamp;

/**
 * @author Nico Kuijpers
 * Modified for FUN3 by Gertjan Schouten
 */
public class KochManager {

    private ArrayList<Edge> edges;
    private FUN3KochFractalFX application;
    private TimeStamp tsCalc;
    private TimeStamp tsDraw;
    public ExecutorService pool;
    public  List<Future<?>> futures;

    public KochManager(FUN3KochFractalFX application) {
        this.edges = new ArrayList<Edge>();
        this.application = application;
        this.tsCalc = new TimeStamp();
        this.tsDraw = new TimeStamp();
    }


    public void changeLevel(int nxt) {
        edges.clear();
        tsCalc.init();
        tsCalc.setBegin("Begin calculating");

        CountDownLatch latch = new CountDownLatch(3);
        edges.clear();


        KochTask leftTask = new KochTask(KochEnum.BOTTOM, latch, this, nxt);
        KochTask rightTask = new KochTask(KochEnum.RIGHT, latch, this, nxt);
        KochTask bottomTask = new KochTask(KochEnum.LEFT, latch, this, nxt);

        //bind de progress van de task met de progress van de bar
        getFractalFX().getProgressBarLeft().progressProperty().bind(leftTask.progressProperty());
        getFractalFX().getProgressBarBottom().progressProperty().bind(bottomTask.progressProperty());
        getFractalFX().getProgressBarRight().progressProperty().bind(rightTask.progressProperty());

        //bind de message van de task met de messgae van de bar
        getFractalFX().getProgressBarLeftLabel().textProperty().bind(leftTask.messageProperty());
        getFractalFX().getProgressBarRightLabel().textProperty().bind(rightTask.messageProperty());
        getFractalFX().getProgressBarBottomLabel().textProperty().bind(bottomTask.messageProperty());

        futures = new ArrayList<Future<?>>();

        //maak een threadpool
        pool = Executors.newFixedThreadPool(3);
        //submit (voer uit de Tasks)
        Future<?> future = pool.submit(leftTask);
        futures.add(future);
        future = pool.submit(rightTask);
        futures.add(future);
        future = pool.submit(bottomTask);
        futures.add(future);
        pool.shutdown();  //nadat alles uitgevoerd is sluit de threadpool
    }


    public void drawEdges() {
        tsDraw.init();
        tsDraw.setBegin("Begin drawing");
        application.clearKochPanel();
        for (Edge e : edges) {
            application.drawEdge(e);
        }
        tsDraw.setEnd("End drawing");
        application.setTextDraw(tsDraw.toString());
    }

    FUN3KochFractalFX getFractalFX() {
        return application;
    }

    ArrayList<Edge> getEdges() {
        return edges;
    }

    synchronized void addAllEdges(ArrayList<Edge> edges) {
        this.edges.addAll(edges);
    }
}
