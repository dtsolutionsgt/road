package com.dts.roadp;

import android.content.Context;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.widget.GridView;
import android.widget.Toast;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private GridView gridView;
    private float scale=1f;

    private Context cont;
    private int ncol=1;

    public ScaleListener(GridView gridView, Context context,int numcolum){
        this.gridView = gridView;
        cont=context;
        ncol=numcolum;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        scale = detector.getScaleFactor();

        if (scale>1){
            ncol+=1;

            if (ncol<10){
                gridView.setNumColumns(ncol);
            }else{
                ncol=9;
                gridView.setNumColumns(ncol);
            }

        }

        if (scale<1){
            ncol-=1;
            if (ncol>=1){
                gridView.setNumColumns(ncol);
            }else{
                ncol=1;
                gridView.setNumColumns(ncol);
            }

        }

        return true;
    }


}
