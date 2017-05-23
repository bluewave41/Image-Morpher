package com.example.matthew.androidproject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class Main extends AppCompatActivity {

    CanvasView view1;
    CanvasView view2;
    SeekBar bar;
    boolean image = false;
    int frameCount = 5;
    NumberPicker picker;
    ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Project");
        view1 = (CanvasView)findViewById(R.id.view);
        view2 = (CanvasView)findViewById(R.id.view2);
        bar = (SeekBar)findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ImageView view = (ImageView) findViewById(R.id.imageView);
                Drawable display = new BitmapDrawable(getResources(), pictures.get(progress));
                view.setBackground(display);
            }
        });

        view1.setTwin(view2);
        view2.setTwin(view1);
    }

    public void edit(View view) {
        view1.edit = !view1.edit;
    }

    public void addFrame(View view) {
        TextView draw = (TextView)findViewById(R.id.fr);
        frameCount++;
        draw.setText(Integer.toString(frameCount));
    }

    public void minusFrame(View view) {
        if(frameCount>0)
            frameCount--;
        TextView draw = (TextView)findViewById(R.id.fr);
        draw.setText(Integer.toString(frameCount));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.change) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            Intent c = Intent.createChooser(chooseFile, "Choose file");
            startActivityForResult(c, 1);
        }
        else if (id == R.id.change2) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            Intent c = Intent.createChooser(chooseFile, "Choose file");
            startActivityForResult(c, 1);
            image = true;
        }
        else if(id == R.id.clear) {
            view1.clear();
        }
        else if(id == R.id.undo) {
            view1.undo();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri map = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(map);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                BitmapDrawable draw = new BitmapDrawable(null, selectedImage);
                if(!image)
                    view1.setBackground(draw);
                else
                    view2.setBackground(draw);
                image = false;
            }
            catch(FileNotFoundException e) {

            }
        }
    }

    /**Creates bitmaps representing each frame of the warp.
     *
     * @param src Source image view
     * @param dst Destination image view
     * @param difference Difference between line positions
     * @return Arraylist of each generated frame.
     */
    public ArrayList<Bitmap> createBitmap(CanvasView src, CanvasView dst, ArrayList<Difference> difference) {
        Bitmap blank;
        Drawable view1Immutable = src.getBackground(); //get both backgrounds
        Bitmap view1Background = ((BitmapDrawable) view1Immutable).getBitmap();
        ArrayList<Bitmap> frames = new ArrayList<>();

        view1Background = Bitmap.createScaledBitmap(view1Background, 400, 400, false);
        double newX, newY;
        int width = view1.getWidth();
        int height = view1.getHeight();
        for (int i = 1; i <= frameCount; i++) { //for each frame
            ArrayList<Line> temp = new ArrayList();
            blank = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
            for (int q = 0; q < view1.lines.size(); q++) {
                Line srcLine = dst.lines.get(q);
                Difference d = difference.get(q);
                Line l = new Line(srcLine.startX - Math.round(d.X1 * i), srcLine.startY - Math.round(d.Y1 * i), srcLine.stopX - Math.round(d.X2 * i), srcLine.stopY - Math.round(d.Y2 * i));
                temp.add(l);
            }

            for (int x = 0; x < width; x++) { //for each x pixel
                for (int y = 0; y < height; y++) { //for each y pixel
                    double totalWeight = 0;
                    double xDisplacement = 0;
                    double yDisplacement = 0;
                    for (int l = 0; l < view1.lines.size(); l++) { //for each line
                        Line dest = view2.lines.get(l);
                        Line srcLine = temp.get(l);
                        double ptx = dest.startX - x;
                        double pty = dest.startY - y;
                        double nx = dest.yLength * -1;
                        double ny = dest.xLength;

                        double d = ((nx * ptx) + (ny * pty)) / ((Math.sqrt(nx * nx + ny * ny))); //clean up all this shit since half of it probably isn't needed
                        double fp = ((dest.xLength * (ptx * -1)) + (dest.yLength * (pty * -1)));
                        fp = fp / (Math.sqrt(dest.xLength * dest.xLength + dest.yLength * dest.yLength));
                        fp = fp / (Math.sqrt(dest.xLength * dest.xLength + dest.yLength * dest.yLength));

                        nx = srcLine.yLength * -1;
                        ny = srcLine.xLength;

                        newX = ((srcLine.startX) + (fp * srcLine.xLength)) - ((d * nx / (Math.sqrt(nx * nx + ny * ny))));
                        newY = ((srcLine.startY) + (fp * srcLine.yLength)) - ((d * ny / (Math.sqrt(nx * nx + ny * ny))));

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
                }
            }
            frames.add(blank);
        }
        return frames;
    }

    /**Slowly dissolves picture 1 into picture 2.
     *
     * @param frames1 Frames of image 1 to image 2
     * @param frames2 Frames of image2 to image 1
     */
    public void crossDissolve(ArrayList<Bitmap> frames1, ArrayList<Bitmap> frames2) {
        int width = view1.getWidth();
        int height = view1.getHeight();
        int factor1 = 1;
        int factor2 = frameCount-factor1;
        for(int i=0; i < frames1.size();i++) {
            Bitmap view1Background = frames1.get(i);
            Bitmap view2Background = frames2.get(i);
            Bitmap blank = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
            for(int x=0;x<width;x++) {
                for(int y=0;y<height;y++) {
                    int pixel1 = view1Background.getPixel(x, y);
                    int pixel2 = view2Background.getPixel(x, y);
                    int red = Color.red(pixel1)*factor2/frameCount+Color.red(pixel2)*factor1/frameCount;
                    int green = Color.green(pixel1)*factor2/frameCount+Color.green(pixel2)*factor1/frameCount;
                    int blue = Color.blue(pixel1)*factor2/frameCount+Color.blue(pixel2)*factor1/frameCount;
                    blank.setPixel(x, y, Color.rgb(red, green, blue));
                }
            }
            pictures.add(blank);
            factor1++;
            factor2 = frameCount-factor1;
        }
    }

    /**Generate value to increment each line by to create intermediate frames.
     *
     * @param difference  Arraylist to store increment value for destination to source image.
     * @param difference2 Arraylist to store increment value for source to destination image.
     */
    public void generateIntermediateFrames(ArrayList<Difference> difference, ArrayList<Difference> difference2) {
        for(int i=0;i<view1.lines.size();i++) {
            Line dest = view2.lines.get(i);
            Line src = view1.lines.get(i);
            Difference d = new Difference((float)(dest.startX-src.startX)/(frameCount+1), (float)(dest.stopX-src.stopX)/(frameCount+1), (float)(dest.startY-src.startY)/(frameCount+1), (float)(dest.stopY-src.stopY)/(frameCount+1));
            difference.add(d);
            d = new Difference(((float)src.startX-dest.startX)/(frameCount+1), (float)(src.stopX-dest.stopX)/(frameCount+1), (float)(src.startY-dest.startY)/(frameCount+1), (float)(src.stopY-dest.stopY)/(frameCount+1));
            difference2.add(d);
        }
    }

    /**Morphs image 1 into image 2.
     *
     * @param view
     */
    public void morph(View view) {
        pictures.clear();
        ArrayList<Difference> difference = new ArrayList();
        ArrayList<Difference> difference2 = new ArrayList();
        ArrayList<Bitmap> frames1, frames2;
        pictures.add(((BitmapDrawable) view1.getBackground()).getBitmap());
        TextView text = (TextView)findViewById(R.id.textView);
        generateIntermediateFrames(difference, difference2); //generate linear difference for intermediate frames
        frames1 = createBitmap(view1, view2, difference); //generate frames for picture 1 to 2
        frames2 = createBitmap(view2, view1, difference2); //generate frames for picture 2 to 2
        crossDissolve(frames1, frames2); //cross disolve the frames together
        bar.setMax(frameCount);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}