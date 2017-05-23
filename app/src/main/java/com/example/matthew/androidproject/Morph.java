package com.example.matthew.androidproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matthew on 2016-01-21.
 */
public class Morph extends AsyncTask<Void, Void, Bitmap> {

    CanvasView view1, view2;
    Context c;
    static int x = 0;
    static int y = 0;
    Bitmap view1Background;
    Bitmap blank;
    Lock l = new ReentrantLock();

    public Morph(CanvasView view1, CanvasView view2, Context c) {
        this.view1 = view1;
        this.view2 = view2;
        this.c = c;
        view1Background = ((BitmapDrawable)view1.getBackground()).getBitmap();
        view1Background = Bitmap.createScaledBitmap(view1Background, 400, 400, false);
    }

    public void onPostExecute(Bitmap map) {
        Drawable d = new BitmapDrawable(c.getResources(), map);
        view2.setBackground(d);
    }
    public Bitmap doInBackground(Void... unused) {
        double newX, newY;
        Bitmap blank = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        while(x < 400) {
            while(y < 400) {
                l.lock();
                if(y >= 400) {
                    x++;
                    y=0;
                }
                if(y<400)
                    y++;
                try {
                    double totalWeight = 0;
                    double xDisplacement = 0;
                    double yDisplacement = 0;
                    for (int l = 0; l < view1.lines.size(); l++) { //for each line
                        Line dest = view2.lines.get(l);
                        Line src = view1.lines.get(l);
                        double ptx = dest.startX - x;
                        double pty = dest.startY - y;
                        double pqx = dest.stopX - dest.startX;
                        double pqy = dest.stopY - dest.startY;
                        double nx = dest.yLength * -1;
                        double ny = dest.xLength;

                        double d = ((nx * ptx) + (ny * pty)) / ((Math.sqrt(nx * nx + ny * ny)));
                        double fp = ((pqx * (ptx * -1)) + (pqy * (pty * -1)));
                        fp = fp / (Math.sqrt(pqx * pqx + pqy * pqy));
                        fp = fp / (Math.sqrt(pqx * pqx + pqy * pqy));

                        ptx = x - src.startX;
                        pty = y - src.startY;
                        nx = src.yLength * -1;
                        ny = src.xLength;

                        newX = ((src.startX) + (fp * src.xLength)) - ((d * nx / (Math.sqrt(nx * nx + ny * ny))));
                        newY = ((src.startY) + (fp * src.yLength)) - ((d * ny / (Math.sqrt(nx * nx + ny * ny))));

                        double weight = (1 / (0.01 + Math.abs(d)));
                        totalWeight += weight;
                        xDisplacement += (newX - x) * weight;
                        yDisplacement += (newY - y) * weight;
                    }

                    newX = x + (xDisplacement / totalWeight);
                    newY = y + (yDisplacement / totalWeight);

                    if (xDisplacement == x - newX)
                        newX = x - xDisplacement;

                    if (yDisplacement == y - newY)
                        newY = y - yDisplacement;

                    if (newX < 0)
                        newX = 0;
                    if (newY < 0)
                        newY = 0;
                    if (newY >= 400)
                        newY = 399;
                    if (newX >= 400)
                        newX = 399;

                    blank.setPixel(x, y, view1Background.getPixel((int) Math.abs(newX), (int) Math.abs(newY)));
                } finally {
                    l.unlock();
                }
            }
        }
        return blank;
    }
}
