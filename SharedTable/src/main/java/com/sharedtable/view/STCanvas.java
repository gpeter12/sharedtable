package com.sharedtable.view;

import com.sharedtable.controller.CanvasController;
import com.sharedtable.controller.Point;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class STCanvas extends Canvas {

    private GraphicsContext graphicsContext;


    public STCanvas() {
        setWidth(1920);
        setHeight(1080);
        graphicsContext = this.getGraphicsContext2D();
    }

    public void initEventHandlers(CanvasController canvasController) {
        this.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    if(event.getButton().equals(MouseButton.PRIMARY)){
                        Point p = new Point(event.getX(), event.getY());
                        canvasController.mouseDown(p);
                    }
                });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    if(event.getButton().equals(MouseButton.PRIMARY)){
                        Point p = new Point(event.getX(), event.getY());
                        canvasController.mouseUp(p);
                    }
                });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseMove(p);
                    if(event.getButton().equals(MouseButton.PRIMARY)) {
                        canvasController.mouseMove(p);
                    }

                });
        /*this.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                Platform.runLater(canvasController::onWidthChanged);
            }
        });
        this.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                Platform.runLater(canvasController::onHeightChanged);
            }
        });*/
    }

    public void setColor(Color color){
        graphicsContext.setStroke(color);
    }
    public void setLineWidth(int lineWidth) {
        graphicsContext.setLineWidth(lineWidth);
    }
    public void drawLine(Point x, Point y) {
        graphicsContext.strokeLine(x.getX(), x.getY(), y.getX(), y.getY());
    }

    public void drawImage(Image image, Rectangle rectangle) {
        graphicsContext.drawImage(image,rectangle.getX(),rectangle.getY(),
                rectangle.getWidth(),rectangle.getHeight());
    }

    public void drawTriangle(Point a, Point b, Point c) {
        graphicsContext.strokePolygon(new double[]{a.getX(), b.getX(),c.getX()},
                new double[]{a.getY(), b.getY(), c.getY()}, 3);
    }

    public void drawEllipse(Rectangle rectangle) {
        graphicsContext.strokeOval(rectangle.getX(),rectangle.getY(),
                rectangle.getWidth(),
                rectangle.getHeight());
    }

    public void drawRectangle(Rectangle rectangle) {
        graphicsContext.strokeRect(rectangle.getX(),rectangle.getY(),rectangle.getWidth(),rectangle.getHeight());
    }

    public void clear() {
        graphicsContext.clearRect(0.0, 0.0, graphicsContext.getCanvas().getWidth(), graphicsContext.getCanvas().getHeight());
    }

    public WritableImage createImage() {

        WritableImage wi;
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        int imageWidth = (int) this.getBoundsInLocal().getWidth();
        int imageHeight = (int) this.getBoundsInLocal().getHeight();
        wi = new WritableImage(imageWidth, imageHeight);
        this.snapshot(parameters, wi);
        System.out.println("image created");
        return wi;

    }

    public void setImage(WritableImage writableImage) {
        Platform.runLater(() -> {
                clear();
                this.getGraphicsContext2D().drawImage(writableImage, 0, 0);
            });
    }


}
