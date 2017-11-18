package com.raspberry.camera;

import com.raspberry.camera.entity.MatEntity;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MatUtils {
    public static int[] extractDataFromMat(Mat mat) {
        int z = 0;
        int[] tab = new int[(int)mat.total() * mat.channels()];
        for(int i = 0;i<mat.rows();i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] temp = mat.get(i, j);
                for(int k = 0;k<temp.length;k++) {
                    tab[z++] = (int)temp[k];
                }
            }
        }
        return tab;
    }

    public static ByteArrayOutputStream writeMat(MatEntity matEntity) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringBuilder builder = new StringBuilder();
        builder.append("%");
        builder.append("YAML:1.0\n" +
                "---\n" +
                "Matrix: !!opencv-matrix\n" +
                "   rows: "+matEntity.getRows()+"\n" +
                "   cols: "+matEntity.getCols()+"\n" +
                "   dt: \"3u\"\n" +
                "   data: [ ");
        int[] data = matEntity.getData();
        for(int i = 0; i<data.length; i++) {
            builder.append(data[i]);
            if(i != data.length - 1)
                builder.append(", ");
            if(i % 10 == 0 && i>5)
                builder.append("\n      ");
        }
        builder.append("]");
        outputStream.write(builder.toString().getBytes());
        return outputStream;
    }
}
