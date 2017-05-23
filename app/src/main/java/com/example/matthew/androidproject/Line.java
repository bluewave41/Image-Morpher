package com.example.matthew.androidproject;

/**
 * Created by Matthew on 2016-01-10.
 */
class Line
{
    int startX;
    int startY;
    int stopX;
    int stopY;
    int xLength;
    int yLength;

    public Line(int startX, int startY, int stopX, int stopY) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
        this.xLength = stopX-startX;
        this.yLength = stopY-startY;
    }
    public Line(int startX, int startY) { // for convenience
        this(startX, startY, startX, startY);
    }
}
