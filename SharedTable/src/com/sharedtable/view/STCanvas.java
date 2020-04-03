package com.sharedtable.view;

import com.sharedtable.controller.Point;
import com.sharedtable.controller.CanvasController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class STCanvas extends Canvas {

    public STCanvas() {
        setWidth(640);
        setHeight(480);
        graphicsContext = this.getGraphicsContext2D();
    }

    public void initEventHandlers(CanvasController canvasController) {
        this.addEventHandler(MouseEvent.MOUSE_PRESSED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseDown(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_RELEASED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseUp(p);
                });
        this.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                event -> {
                    Point p = new Point(event.getX(), event.getY());
                    canvasController.mouseMove(p);
                });
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
                rectangle.getWidth()/2,
                rectangle.getHeight()/2);
    }

    public void drawRectangle(Rectangle rectangle) {
        graphicsContext.strokeRect(rectangle.getX(),rectangle.getY(),rectangle.getWidth(),rectangle.getHeight());
    }

    public void clear() {
        graphicsContext.clearRect(0.0, 0.0, graphicsContext.getCanvas().getWidth(), graphicsContext.getCanvas().getHeight());
    }

    private GraphicsContext graphicsContext;


}
