package com.dts.roadp;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptCFDVB extends BaseAdapter {

	private static ArrayList<clsClasses.clsCFDV> itemDetailsrrayList;

	private int selectedIndex;

	private LayoutInflater l_Inflater;

	public ListAdaptCFDVB(Context context, ArrayList<clsClasses.clsCFDV> results) {
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

			convertView = l_Inflater.inflate(R.layout.activity_list_view_cfdvb, null);
			holder = new ViewHolder();

			holder.lblFecha  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblPValor);
            holder.img1 =  convertView.findViewById(R.id.imageView27);
			holder.lblCufe = (TextView) convertView.findViewById(R.id.textCufe);
			holder.lblCertificada = (TextView) convertView.findViewById(R.id.textCertificada);
			holder.lblEstado = (TextView) convertView.findViewById(R.id.txtEstado);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.lblFecha.setText(itemDetailsrrayList.get(position).Fecha);
		holder.lblDesc.setText(itemDetailsrrayList.get(position).Desc);
		holder.lblValor.setText(itemDetailsrrayList.get(position).Valor);
		holder.lblCufe.setText(itemDetailsrrayList.get(position).Cufe);
		holder.lblCertificada.setText(itemDetailsrrayList.get(position).Certificada_DGI);
		holder.lblEstado.setText(itemDetailsrrayList.get(position).Estado);

		if (itemDetailsrrayList.get(position).bandera==1) {
            holder.img1.setVisibility(View.VISIBLE);
        } else {
            holder.img1.setVisibility(View.INVISIBLE);
        }

		if (selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
		} else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}

		return convertView;
	}


	static class ViewHolder {
		TextView  lblFecha,lblDesc,lblValor, lblCufe, lblEstado, lblCertificada;
		ImageView img1;
	}

}