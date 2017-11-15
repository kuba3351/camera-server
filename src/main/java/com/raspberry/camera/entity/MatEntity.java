package com.raspberry.camera.entity;


public class MatEntity {
    private int rows;
    private int cols;
    private String dt;

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public String getDt() {
        return dt;
    }

    public int[] getData() {
        return data;
    }

    private int[] data;

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public MatEntity() {
        dt = "3u";
    }
}
