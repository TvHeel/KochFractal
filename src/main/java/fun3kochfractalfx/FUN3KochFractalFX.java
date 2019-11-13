package fun3kochfractalfx;

import calculate.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Nico Kuijpers
 * Modified for FUN3 by Gertjan Schouten
 */
public class FUN3KochFractalFX extends Application {

    // Zoom and drag
    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private double startPressedX = 0.0;
    private double startPressedY = 0.0;
    private double lastDragX = 0.0;
    private double lastDragY = 0.0;

    // Koch manager
    // TO DO: Create class KochManager in package calculate
    private KochManager kochManager;

    // Current level of Koch fractal
    private int currentLevel = 1;

    // Labels for level, nr edges, calculation time, and drawing time
    private Label labelLevel;
    private Label labelNrEdges;
    private Label labelNrEdgesText;
    private Label labelCalc;
    private Label labelCalcText;
    private Label labelDraw;
    private Label labelDrawText;

    // Koch panel and its size
    private Canvas kochPanel;
    private final int kpWidth = 500;
    private final int kpHeight = 500;

    // counter for snapshot and its threshold (fixes rendering issue)
    private int counter = 0;
    private static final int THRESHOLD = 200_000;
    private final WritableImage image = new WritableImage(kpWidth, kpHeight);

    private Label progressBarLeftLabel;
    private Label progressBarRightLabel;
    private Label progressBarBottomLabel;

    private ProgressBar progressBarLeft;
    private ProgressBar progressBarRight;
    private ProgressBar progressBarBottom;


    @Override
    public void start(Stage primaryStage) throws ExecutionException, InterruptedException {

        // Define grid pane
        GridPane grid;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // For debug purposes
        // Make de grid lines visible
        // grid.setGridLinesVisible(true);

        // Drawing panel for Koch fractal
        kochPanel = new Canvas(kpWidth,kpHeight);
        grid.add(kochPanel, 0, 3, 25, 1);

        // Labels to present number of edges for Koch fractal
        labelNrEdges = new Label("Nr edges:");
        labelNrEdgesText = new Label();
        grid.add(labelNrEdges, 0, 0, 4, 1);
        grid.add(labelNrEdgesText, 3, 0, 22, 1);

        // Labels to present time of calculation for Koch fractal
        labelCalc = new Label("Calculating:");
        labelCalcText = new Label();
        grid.add(labelCalc, 0, 1, 4, 1);
        grid.add(labelCalcText, 3, 1, 22, 1);

        // Labels to present time of drawing for Koch fractal
        labelDraw = new Label("Drawing:");
        labelDrawText = new Label();
        grid.add(labelDraw, 0, 2, 4, 1);
        grid.add(labelDrawText, 3, 2, 22, 1);

        // Label to present current level of Koch fractal
        labelLevel = new Label("Level: " + currentLevel);
        grid.add(labelLevel, 0, 6);

        progressBarLeftLabel = new Label("Progress Left:");
        grid.add(progressBarLeftLabel, 0, 8, 10, 1);
        progressBarRightLabel = new Label("Progress Right:");
        grid.add(progressBarRightLabel, 0, 10, 10, 1);
        progressBarBottomLabel = new Label("Progress Bottom:");
        grid.add(progressBarBottomLabel, 0, 12, 10, 1);
        progressBarLeft = new ProgressBar();
        grid.add(progressBarLeft, 5, 8, 30, 1);
        progressBarRight = new ProgressBar();
        grid.add(progressBarRight, 5, 10, 30, 1);
        progressBarBottom = new ProgressBar();
        grid.add(progressBarBottom, 5, 12, 30, 1);
        progressBarLeftLabel = new Label();
        grid.add(progressBarLeftLabel, 6, 8, 10, 1);
        progressBarRightLabel = new Label();
        grid.add(progressBarRightLabel, 6, 10, 10, 1);
        progressBarBottomLabel = new Label();
        grid.add(progressBarBottomLabel, 6, 12, 10, 1);

        // Button to increase level of Koch fractal
        Button buttonIncreaseLevel = new Button();
        buttonIncreaseLevel.setText("Increase Level");
        buttonIncreaseLevel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    increaseLevelButtonActionPerformed(event);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(buttonIncreaseLevel, 3, 6);

        // Button to decrease level of Koch fractal
        Button buttonDecreaseLevel = new Button();
        buttonDecreaseLevel.setText("Decrease Level");
        buttonDecreaseLevel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    decreaseLevelButtonActionPerformed(event);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        grid.add(buttonDecreaseLevel, 5, 6);

        // Button to fit Koch fractal in Koch panel
        Button buttonFitFractal = new Button();
        buttonFitFractal.setText("Fit Fractal");
        buttonFitFractal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fitFractalButtonActionPerformed(event);
            }
        });
        grid.add(buttonFitFractal, 14, 6);

        // Add mouse clicked event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMouseClicked(event);
                    }
                });

        // Add mouse pressed event to Koch panel
        kochPanel.addEventHandler(MouseEvent.MOUSE_PRESSED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        kochPanelMousePressed(event);
                    }
                });

        // Add mouse dragged event to Koch panel
        kochPanel.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                kochPanelMouseDragged(event);
            }
        });

        // Create Koch manager and set initial level
        resetZoom();
        kochManager = new KochManager(this);
        kochManager.changeLevel(currentLevel);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root, kpWidth+50, kpHeight+170);
        root.getChildren().add(grid);

        // Define title and assign the scene for main window
        primaryStage.setTitle("Koch Fractal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public Label getProgressBarLeftLabel() {
        return progressBarLeftLabel;
    }
    public Label getProgressBarRightLabel() {
        return progressBarRightLabel;
    }
    public Label getProgressBarBottomLabel() {
        return progressBarBottomLabel;
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0,0.0,kpWidth,kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0,0.0,kpWidth,kpHeight);
        counter = 0;
    }

    public ProgressBar getProgressBarLeft() {
        return progressBarLeft;
    }

    public ProgressBar getProgressBarRight() {
        return progressBarRight;
    }

    public ProgressBar getProgressBarBottom() {
        return progressBarBottom;
    }
    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        // Set line color
        gc.setStroke(e1.color);

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(2.0);
        }
        else if (currentLevel <=5 ) {
            gc.setLineWidth(1.5);
        }
        else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1,e1.Y1,e1.X2,e1.Y2);

        counter++;
        if (counter>=THRESHOLD) {
            kochPanel.snapshot(null,image);
            counter = 0;
        }
    }

    public synchronized void setTextNrEdges(String text) {
        labelNrEdgesText.setText(text);
    }

    public void setTextCalc(String text) {
        labelCalcText.setText(text);
    }

    public void setTextDraw(String text) {
        labelDrawText.setText(text);
    }

    public synchronized void requestDrawEdges() {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                kochManager.drawEdges();
            }
        });
    }

    private void increaseLevelButtonActionPerformed(ActionEvent event) throws ExecutionException, InterruptedException {
        boolean allDone = true;
        if (currentLevel < 12) {
            for(Future<?> future : kochManager.futures){//haal de futures op die er in de threadpool zitten
                if(!future.isDone()){  //als er 1 neit done is zet boolean false
                    allDone = false;
                }
            }
            if(!allDone){//als ze niet allemaal done zijn en dus gecancelled moeten worden
                for(Future<?> future : kochManager.futures){
                    System.out.println("niet allemaal done");
                    future.cancel(true); //cancel elke future
                }
            }
            else {//als ze allemaal done zijn
                System.out.println("allemaal done");
                // resetZoom();
                currentLevel++;
                labelLevel.setText("Level: " + currentLevel);
                kochManager.changeLevel(currentLevel);
            }
        }
        allDone = true;//reset boolean
    }

    private void decreaseLevelButtonActionPerformed(ActionEvent event) throws ExecutionException, InterruptedException {
        boolean allDone = true;
        if (currentLevel > 1) {
            for(Future<?> future : kochManager.futures){
                if(!future.isDone()){
                    allDone = false;
                }
            }
            if(!allDone){
                for(Future<?> future : kochManager.futures){
                    System.out.println("niet allemaal done");
                    future.cancel(true);
                }
            }
            else {
                System.out.println("allemaal done");
                // resetZoom();
                currentLevel--;
                labelLevel.setText("Level: " + currentLevel);
                kochManager.changeLevel(currentLevel);
            }
        }
        allDone = true;
    }

    private void fitFractalButtonActionPerformed(ActionEvent event) {
        resetZoom();
        kochManager.drawEdges();
    }

    private void kochPanelMouseClicked(MouseEvent event) {
        if (Math.abs(event.getX() - startPressedX) < 1.0 &&
                Math.abs(event.getY() - startPressedY) < 1.0) {
            double originalPointClickedX = (event.getX() - zoomTranslateX) / zoom;
            double originalPointClickedY = (event.getY() - zoomTranslateY) / zoom;
            if (event.getButton() == MouseButton.PRIMARY) {
                zoom *= 2.0;
            } else if (event.getButton() == MouseButton.SECONDARY) {
                zoom /= 2.0;
            }
            zoomTranslateX = (int) (event.getX() - originalPointClickedX * zoom);
            zoomTranslateY = (int) (event.getY() - originalPointClickedY * zoom);
            kochManager.drawEdges();
        }
    }

    private void kochPanelMouseDragged(MouseEvent event) {
        zoomTranslateX = zoomTranslateX + event.getX() - lastDragX;
        zoomTranslateY = zoomTranslateY + event.getY() - lastDragY;
        lastDragX = event.getX();
        lastDragY = event.getY();
        kochManager.drawEdges();
    }

    private void kochPanelMousePressed(MouseEvent event) {
        startPressedX = event.getX();
        startPressedY = event.getY();
        lastDragX = event.getX();
        lastDragY = event.getY();
    }

    private void resetZoom() {
        int kpSize = Math.min(kpWidth, kpHeight);
        zoom = kpSize;
        zoomTranslateX = (kpWidth - kpSize) / 2.0;
        zoomTranslateY = (kpHeight - kpSize) / 2.0;
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color);
    }


    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
