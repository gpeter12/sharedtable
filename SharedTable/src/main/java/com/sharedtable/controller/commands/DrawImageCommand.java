package com.sharedtable.controller.commands;

import com.sharedtable.controller.CanvasController;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class DrawImageCommand extends DrawRectangleCommand {

    private Image image = null;
    private int imageSize = -1;
    private UUID mementoID;

    public DrawImageCommand(CanvasController canvasController, UUID creatorID,
                            Rectangle rectangle, Image image, UUID mementoID)
    {
        super(canvasController, creatorID, rectangle, Color.BLACK, 0);
        this.image = image;
        //printByteArray(imageBytes);
        this.mementoID = mementoID;
    }

    public void printByteArray(byte[] inp) {
        for(byte act : inp) {
            System.out.print(act);
        }
        System.out.print("\n");
    }

    public DrawImageCommand(String[] input) {
        super(input);
        imageSize = Integer.parseInt(input[9]);
        mementoID = UUID.fromString(input[10]);
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
                .append(imageSize).append(";")
                .append(mementoID);
        return stringBuilder.toString();
    }

    @Override
    public void execute() {
        canvasController.drawImage(image,rectangle);
    }

    public Image getImage() {
        if(image == null)
            throw new RuntimeException("object has no image");
        return image;
    }

    public int getImageSize() {
        return imageSize;
    }

    public byte[] getImageBytes() {
        return imageToByteArray(getImage());
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }

    public void setImage(byte[] imageBytes) {
        this.image = byteArrayToImage(imageBytes);
    }

    public UUID getMementoID() {
        return mementoID;
    }

    private byte[] imageToByteArray(Image input) {
        BufferedImage bImage = SwingFXUtils.fromFXImage(input, null);
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

    private Image byteArrayToImage(byte[] input) {
        //printByteArray(input);
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


}
