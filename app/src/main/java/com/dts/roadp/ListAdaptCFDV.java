package com.dts.roadp;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdaptCFDV extends BaseAdapter {

	private static ArrayList<clsClasses.clsCFDV> itemDetailsrrayList;

	private int selectedIndex;

	private LayoutInflater l_Inflater;

	public ListAdaptCFDV(Context context, ArrayList<clsClasses.clsCFDV> results) {
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

			convertView = l_Inflater.inflate(R.layout.activity_list_view_cfdv, null);
			holder = new ViewHolder();

			holder.lblFecha  = (TextView) convertView.findViewById(R.id.lblETipo);
			holder.lblDesc = (TextView) convertView.findViewById(R.id.lblPNum);
			holder.lblValor = (TextView) convertView.findViewById(R.id.lblPValor);
			holder.relConsPedido = (RelativeLayout) convertView.findViewById(R.id.relConsPedido);
			holder.lblComunicado = (TextView) convertView.findViewById(R.id.lblComunicado);
			holder.iconComunicado = (ImageView) convertView.findViewById(R.id.iconComunicado);
			holder.lblAnulado = (TextView) convertView.findViewById(R.id.lblAnulado);
			holder.iconAnulado = (ImageView) convertView.findViewById(R.id.iconAnulado);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.lblFecha.setText(itemDetailsrrayList.get(position).Fecha);
		holder.lblDesc.setText(itemDetailsrrayList.get(position).Desc);
		holder.lblValor.setText(itemDetailsrrayList.get(position).Valor);

		if (itemDetailsrrayList.get(position).Flag == 2) {
			holder.relConsPedido.setVisibility(View.VISIBLE);

			if (itemDetailsrrayList.get(position).Statuscom.equalsIgnoreCase("N")) {
				holder.iconComunicado.setImageResource(R.drawable.del_48);
			}else {
				holder.iconComunicado.setImageResource(R.drawable.icok);
			}

			if (itemDetailsrrayList.get(position).Anulado.equalsIgnoreCase("N")) {
				holder.iconAnulado.setImageResource(R.drawable.icok);
			}else{
				holder.iconAnulado.setImageResource(R.drawable.del_48);
			}
		}

		if(selectedIndex!= -1 && position == selectedIndex) {
			convertView.setBackgroundColor(Color.rgb(26,138,198));
		} else {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}

		return convertView;
	}


	static class ViewHolder {
		RelativeLayout relConsPedido;
		ImageView iconComunicado, iconAnulado;
		TextView  lblFecha,lblDesc,lblValor,lblComunicado,lblAnulado;
	}

}