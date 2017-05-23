package com.example.matthew.androidproject;

import android.graphics.RectF;

/**
 * Created by Matthew on 2016-01-06.
 */
public class Pair<L,M, R> {

    private final RectF left;
    private final RectF middle;
    private final RectF right;

    public Pair(RectF left, RectF middle, RectF right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public RectF getStart() { return left; }
    public RectF getMiddle() { return middle; }
    public RectF getEnd() { return right; }
}