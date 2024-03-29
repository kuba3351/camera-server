package com.raspberry.camera.other;

/**
 * Encja Hibernate służąca do zapisu macierzy Mat w bazie
 */
public class MatContainer {
    private int rows;
    private int cols;
    private String dt;
    private int[] data;

    public MatContainer() {
        dt = "3u";
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }
}
