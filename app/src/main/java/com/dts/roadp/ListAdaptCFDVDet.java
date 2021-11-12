package com.dts.roadp;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptCFDVDet extends BaseAdapter {

    private static ArrayList<clsClasses.clsCFDVDet> itemDetailsrrayList;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptCFDVDet(Context context, ArrayList<clsClasses.clsCFDVDet> results) {
        itemDetailsrrayList = results;
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
        return itemDetailsrrayList.size();
    }

    public Object getItem(int position) {
        return itemDetailsrrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.con_pedido_detalle_list, null);
            holder = new ViewHolder();

            holder.lblCodigo  = (TextView) convertView.findViewById(R.id.lblCodigo);
            holder.lblDescripcion = (TextView) convertView.findViewById(R.id.lblDescripcion);
            holder.lblCant = (TextView) convertView.findViewById(R.id.lblCant);
            holder.lblUm = (TextView) convertView.findViewById(R.id.lblUm);
            holder.lblPrecio = (TextView) convertView.findViewById(R.id.lblPrecio);
            holder.lblValor = (TextView) convertView.findViewById(R.id.lblValor);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblCodigo.setText(String.valueOf(itemDetailsrrayList.get(position).Producto));
        holder.lblDescripcion.setText(itemDetailsrrayList.get(position).Desclarga);
        holder.lblCant.setText(String.valueOf(itemDetailsrrayList.get(position).Cant));
        holder.lblUm.setText(itemDetailsrrayList.get(position).Umventa);
        holder.lblPrecio.setText(String.valueOf(itemDetailsrrayList.get(position).PrecioDoc));
        holder.lblValor.setText(String.valueOf(itemDetailsrrayList.get(position).Total));


        return convertView;
    }


    static class ViewHolder {
        TextView lblCodigo, lblDescripcion, lblCant, lblUm, lblPrecio, lblValor;
    }

}