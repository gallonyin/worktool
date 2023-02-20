package org.yameida.worktool.utils.capture;

public class Point {

    private int x = 0;
    private int y = 0;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isEmpty() {
        return x < 0 || y < 0;
    }


    @Override
    public String toString() {
        return "{" + x + "," + y + "}";
    }
}
