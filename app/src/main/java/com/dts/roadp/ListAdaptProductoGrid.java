package com.dts.roadp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ListAdaptProductoGrid extends BaseAdapter {

    private static ArrayList<clsClasses.clsCD> items;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptProductoGrid(Context context, ArrayList<clsClasses.clsCD> results) {
        items = results;
        l_Inflater = LayoutInflater.from(context);
        selectedIndex = -1;
    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public void refreshItems() {
        notifyDataSetChanged();
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdaptProductoGrid.ViewHolder holder;
        String prodimg="";

        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.activity_list_adapt_producto_grid, null);
            holder = new ViewHolder();

            holder.imgEst = (ImageView) convertView.findViewById(R.id.imgNext2);
            holder.lblDesCorta = (TextView) convertView.findViewById(R.id.lblDesc);
            holder.lblCodigo = (TextView) convertView.findViewById(R.id.lblCod);
            holder.lblDesLarga = (TextView) convertView.findViewById(R.id.lblTrat2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblDesLarga.setText(items.get(position).DesLarga);
        holder.lblDesCorta.setText(items.get(position).Desc);
        holder.lblCodigo.setText(items.get(position).Cod);
        holder.imgEst.setImageResource(R.drawable.blank256);

        prodimg = items.get(position).Cod;

        try {

            prodimg = Environment.getExternalStorageDirectory()+ "/RoadFotos/"+prodimg+".jpg";
            File file = new File(prodimg);
            if (file.exists()) {
                try {
                    Bitmap bmImg = BitmapFactory.decodeFile(prodimg);
                    holder.imgEst.setImageBitmap(bmImg);
                } catch (Exception e) {

                }
            }

        } catch (Exception e) {

        }

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;

    }

    static class ViewHolder {
        ImageView imgEst;
        TextView lblDesLarga,lblDesCorta,lblCodigo;
    }

}