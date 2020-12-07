package com.dts.roadp;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListAdaptPedRec extends BaseAdapter {

    private static ArrayList<clsClasses.clsPedRec> itemDetailsrrayList;

    private int selectedIndex;

    private LayoutInflater l_Inflater;

    public ListAdaptPedRec(Context context, ArrayList<clsClasses.clsPedRec> results) {
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

            convertView = l_Inflater.inflate(R.layout.activity_list_view_ped_rechazados, null);
            holder = new ViewHolder();

            holder.lblDFactura  = (TextView) convertView.findViewById(R.id.lblDFactura);
            holder.lblDMontoPR = (TextView) convertView.findViewById(R.id.lblDMontoPR);
            holder.lblDFechaPR = (TextView) convertView.findViewById(R.id.lblDFechaPR);
            holder.lblRazonRechazado = (TextView) convertView.findViewById(R.id.lblRazonRechazado);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblDFactura.setText(itemDetailsrrayList.get(position).Factura);
        holder.lblDMontoPR.setText(itemDetailsrrayList.get(position).Valor);
        holder.lblDFechaPR.setText(itemDetailsrrayList.get(position).Fecha);
        holder.lblRazonRechazado.setText(itemDetailsrrayList.get(position).Razon);

        LinearLayout encabezado = (LinearLayout) convertView.findViewById(R.id.llEnc);

        if (position>0){
            encabezado.setVisibility(View.GONE);
        }else{
            encabezado.setVisibility(View.VISIBLE);
        }

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(33, 150, 243));
        }else{
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    static class ViewHolder {
        TextView  lblDFactura,lblDMontoPR,lblDFechaPR, lblRazonRechazado;
    }

}
