package com.example.matthew.androidproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class CanvasView extends View {
    static boolean drag = false;
    static boolean move = false;
    static boolean edit = false;
    Context context;
    int dragging = -1;
    ArrayList<Line> lines = new ArrayList();
    private Paint mPaint;
    ArrayList<Pair> points = new ArrayList();
    private CanvasView twin;

    public CanvasView(Context context, AttributeSet set) {
        super(context, set);
        this.context = context;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(Color.RED);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    /**Clears both screens of any lines and points.*/
    public void clear() {
        this.lines.clear();
        this.points.clear();
        twin.lines.clear();
        twin.points.clear();
    }

    /**Removes the last line drawn by the user from the screen.*/
    public void undo() {
        if(this.lines.size() > 0) {
            this.lines.remove(this.lines.size() - 1);
            this.points.remove(this.points.size() - 1);
            twin.lines.remove(twin.lines.size() - 1);
            twin.points.remove(twin.points.size() - 1);
        }
    }

    protected void onDraw(Canvas canvas) {
        for(Line l: lines)
            canvas.drawLine(l.startX, l.startY, l.stopX, l.stopY, mPaint);

        if(edit)
            for(Pair p: points) {
                RectF start = p.getStart();
                RectF middle = p.getMiddle();
                RectF end = p.getEnd();
                canvas.drawArc(start.left, start.top, start.right, start.bottom, 0, 360, true, mPaint);
                canvas.drawArc(middle.left, middle.top, middle.right, middle.bottom, 0, 360, true, mPaint);
                canvas.drawArc(end.left, end.top, end.right, end.bottom, 0, 360, true, mPaint);
            }
        this.twin.invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(edit) {
                for (int i = 0; i < points.size(); i++) {
                    Pair p = points.get(i);
                    if (p.getEnd().contains(event.getX(), event.getY())) {
                        userSelection(i, true, false, false);
                    } else if (p.getStart().contains(event.getX(), event.getY())) {
                        userSelection(i, true, false, true);
                    } else if (p.getMiddle().contains(event.getX(), event.getY())) {
                        userSelection(i, false, true, false);
                    }
                }
            }

            if (!edit) { //run if user doesn't select te beginning or end of line
                this.lines.add(new Line((int)event.getX(), (int)event.getY())); //add line to the current canvas
                this.twin.lines.add(lines.get(lines.size()-1)); //add a reference to that line to the twin so it moves as well
                dragging = this.lines.size()-1;
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if(edit && dragging == -1)
                return false;
            Line current = this.lines.get(dragging);
            current.stopX = (int)event.getX();
            current.stopY = (int)event.getY();
            current.xLength = current.stopX-current.startX;
            current.yLength = current.stopY-current.startY;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(edit && dragging == -1)
                return false;

            Line current = this.lines.get(dragging);
            RectF start = new RectF(current.startX - 30, current.startY - 30, current.startX + 30, current.startY + 30);
            RectF middle = new RectF((current.startX + current.stopX) / 2 - 30, (current.startY + current.stopY) / 2 - 30, (current.startX + current.stopX) / 2 + 30, (current.startY + current.stopY) / 2 + 30);
            RectF end = new RectF(current.stopX - 30, current.stopY - 30, current.stopX + 30, current.stopY + 30);
            if(!drag && !move) { //if not dragging or moving so user is drawing a line
                twin.points.add(dragging, new Pair(start, middle, end)); //only add points to the twin if initially drawing a line
                twin.lines.remove(twin.lines.size() - 1); //remove the last line from the twin which is a reference to the one in the first
                twin.lines.add(new Line(current.startX, current.startY, current.stopX, current.stopY)); //add a new line so it can move independently
            }
            points.add(dragging, new Pair(start, middle, end));
            dragging = -1;
            drag = false;
            move = false;
        }
        return false;
    }

    /**Function run when user selects inside the beginning or end of a line.
     *
     * @param index Index of line user selected
     * @param drag Sets dragging mode
     * @param move Sets move mode (not implemented yet)
     * @param reverse Reverses the line, used when dragging the beginning of the line
     */
    public void userSelection(int index, boolean drag, boolean move, boolean reverse) {
        this.drag = drag;
        this.move = move;
        this.points.remove(index);
        invalidate();
        dragging = index;
        if(reverse) {
            Line current = lines.get(dragging);
            current.startX = current.stopX;
            current.startY = current.stopY;
        }
    }

    /**Adds a reference to the other canvas window.
     *
     * @param view CanvasView to to referenced
     */
    public void setTwin(CanvasView view) {
        this.twin = view;
    }
}