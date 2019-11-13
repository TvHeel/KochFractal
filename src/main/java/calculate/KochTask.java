package calculate;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import timeutil.TimeStamp;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;

public class KochTask  extends Task implements Runnable, Observer{
    private KochFractal koch;
    private KochEnum kochEnum;
    private CountDownLatch latch;
    private KochManager manager;
    private ArrayList<Edge> edges = new ArrayList<Edge>();
 //   private int maximaalEdges;
    private int maximaalSideEdges;
    private TimeStamp tsCalc = new TimeStamp();

    public KochTask(KochEnum kochEnum, CountDownLatch latch, KochManager manager, int nxt) {
        this.koch = new KochFractal(this);
        koch.setLevel(nxt);
        koch.addObserver(this);
        koch.setLevel(nxt);
        this.kochEnum = kochEnum;
        this.latch = latch;
        this.manager = manager;
    }

    public synchronized void addEdge(Edge e) {
        edges.add(e);
    }

    @Override
    public Void call() {
        tsCalc.init();
        tsCalc.setBegin("Begin calculating");

        maximaalSideEdges = koch.getNrOfEdges() / 3;//het aantal edges

        //genereate edges obv kant
        if (kochEnum == KochEnum.LEFT) {
            koch.generateLeftEdge();
        } else if (kochEnum == KochEnum.RIGHT) {
            koch.generateRightEdge();
        } else if (kochEnum == KochEnum.BOTTOM) {
            koch.generateBottomEdge();
        }
        manager.addAllEdges(edges); //voeg de edges toe aan de hoofdarray

        Platform.runLater(() -> { //doe dit later pas als de threads klaar zijn
            manager.getFractalFX().setTextNrEdges("" + manager.getEdges().size());
            tsCalc.setEnd("End calculating");
            manager.getFractalFX().setTextCalc(tsCalc.toString());
        });

        manager.getFractalFX().requestDrawEdges(); // ga naar de manager en ga de fractal tekenen.

        latch.countDown(); //gaat steeds 1 omlaag en zorgt ervoor dat er niet meerder threads in deze methode komen
        return null;
    }

    @Override
    public void update(Observable o, Object edge) {
        addEdge((Edge) edge); //voeg de edge toe
        //update de progress van deze KochTask
        updateProgress(edges.size(), maximaalSideEdges);
        //creeer de message
        updateMessage("Nr of edges: " + edges.size());
    }
}
