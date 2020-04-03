package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import com.sharedtable.model.ArrayPrinter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class DrawImageCommand extends DrawRectangleCommand {

    public DrawImageCommand(CanvasController canvasController, UUID creatorID,
                            Rectangle rectangle, Image image)
    {
        super(canvasController, creatorID, rectangle, Color.BLACK, 0);
        this.image = image;
    }

    public DrawImageCommand(String[] input) {
        super(input);
        ArrayList<String> stringArrayList = new ArrayList<String>();
        stringArrayList.addAll(Arrays.asList(input));
        for(int i=0; i<9; i++) {
            stringArrayList.remove(0);
        }
        image = byteArrayToImage(stringToByteArray(ArrayPrinter.printStringArray((String[])stringArrayList.toArray())));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(creatorID.toString()).append(";")
                .append(CommandTypeID.DrawImageCommand.ordinal()).append(";")
                .append(canvasID).append(";")
                .append(rectangle.getX()).append(";")
                .append(rectangle.getY()).append(";")
                .append(rectangle.getWidth()).append(";")
                .append(rectangle.getHeight()).append(";")
                .append(lineWidth).append(";")
                .append(color).append(";")
                .append(byteArrayToString(imageToByteArray(image)));
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvasController.drawImage(image,rectangle);
    }

    private byte[] imageToByteArray(Image input) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, "png", s);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        byte[] res  = s.toByteArray();
        try {
            s.close();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return res;
    }

    private String byteArrayToString(byte[] input) {
        return new String(input, StandardCharsets.UTF_8);
    }

    private byte[] stringToByteArray(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    private Image byteArrayToImage(byte[] input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        BufferedImage bImage2 = null;
        try {
            bImage2 = ImageIO.read(bis);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return SwingFXUtils.toFXImage(bImage2, null);
    }

    private Image image;
}
