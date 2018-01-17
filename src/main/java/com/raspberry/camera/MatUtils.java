package com.raspberry.camera;

import com.raspberry.camera.other.MatContainer;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Klasa z narzędziami służącymi do obsługi macierzy Mat
 */
public class MatUtils {
    public static int[] extractDataFromMat(Mat mat) {
        int z = 0;
        int[] tab = new int[(int) mat.total() * mat.channels()];
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] temp = mat.get(i, j);
                for (double aTemp : temp) {
                    tab[z++] = (int) aTemp;
                }
            }
        }
        return tab;
    }

    public static ByteArrayOutputStream writeMat(MatContainer matContainer) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder builder = new StringBuilder();
        builder.append("%");
        builder.append("YAML:1.0\n" + "---\n" + "Matrix: !!opencv-matrix\n" + "   rows: ").append(matContainer.getRows()).append("\n").append("   cols: ").append(matContainer.getCols()).append("\n").append("   dt: \"3u\"\n").append("   data: [ ");
        int[] data = matContainer.getData();
        for (int i = 0; i < data.length; i++) {
            builder.append(data[i]);
            if (i != data.length - 1)
                builder.append(", ");
            if (i % 10 == 0 && i > 5)
                builder.append("\n      ");
        }
        builder.append("]");
        outputStream.write(builder.toString().getBytes());
        return outputStream;
    }

    public static MatContainer generateMatFromBufferedImage(BufferedImage bufferedImage) {
        MatContainer matContainer = new MatContainer();
        matContainer.setCols(bufferedImage.getHeight());
        matContainer.setRows(bufferedImage.getWidth());
        int[] data = new int[matContainer.getCols()*matContainer.getRows()*3];
        int z = 0;
        for(int i = 0;i<matContainer.getCols();i++) {
            for(int j = 0;j<matContainer.getRows();j++) {
                Color pixel = new Color(bufferedImage.getRGB(i, j));
                data[z++] = pixel.getRed();
                data[z++] = pixel.getGreen();
                data[z++] = pixel.getBlue();
            }
        }
        matContainer.setData(data);
        return matContainer;
    }
}
