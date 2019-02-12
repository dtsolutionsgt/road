package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SolicInv extends PBase {

	private ListView listView;
	private TextView lblTot;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;
	
	private String itemid,prodid;
	private double cant,tot;
	private String emp,estado,ubas,corel;
	private int itempos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solic_inv);
		
		super.InitBase();

		listView = (ListView) findViewById(R.id.listView1);
		lblTot = (TextView) findViewById(R.id.textView1);

		setHandlers();
		
		browse=0;fecha=du.getActDateTime();prodid="";
		listItems();
		getCorel();
	}
	
	
	// Events
	
	public void showProd(View view) {
		((appGlobals) vApp).gstr="";
		browse=1;
		itempos=-1;
		Intent intent = new Intent(this,Producto.class);
		startActivity(intent);
	}	
	
	public void finishDevol(View view){
		if (!hasProducts()) {
			mu.msgbox("No se puede continuar, no ha agregado ninguno producto !");return;
		}
		
		msgAskComplete("Completar la solicitud y preparar para envio");
	}
	
	public void doExit(View view){
		super.finish();
	}
	
	public void doClear(View view){		
		msgAskClear("Borrar la solicitud");
	}
	
	
	// Main
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				try {
					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCFDV vItem = (clsClasses.clsCFDV)lvObj;
		           	
					adapter.setSelectedIndex(position);
		    		
					updCant(vItem.Cod);
					
		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
			};
		});


		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

				try {
					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCFDV item = (clsClasses.clsCFDV) lvObj;

					adapter.setSelectedIndex(position);
					prodid=item.Cod;
					
					msgAskDelete("Eliminar registro");
				} catch (Exception e) {
				}
				return true;
			}
		});


	    
	}
	
	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		String s;
		int selidx=-1,pp=0;
				
		items.clear();tot=0;
		
		try {
			
			sql="SELECT D_SOLICINVD.PRODUCTO, D_SOLICINVD.CANT, D_SOLICINVD.TOTAL, P_PRODUCTO.DESCCORTA, 0 "+
			     "FROM D_SOLICINVD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=D_SOLICINVD.PRODUCTO "+
				 "ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()>0) DT.moveToFirst();
			
			while (!DT.isAfterLast()) {

				vItem = clsCls.new clsCFDV();

				vItem.Cod=DT.getString(0);if (vItem.Cod.equalsIgnoreCase(prodid)) selidx=pp;
				vItem.Desc=DT.getString(3);
				vItem.Valor=mu.frmdec(DT.getDouble(2));tot+=DT.getDouble(2);
				s=mu.frmdec(DT.getDouble(1));
				vItem.Fecha="";
				vItem.Valor=mu.frmint(DT.getDouble(1));
				vItem.id=DT.getInt(4);

				items.add(vItem);	

				DT.moveToNext();pp++;
			}
		} catch (Exception e) {
			mu.msgbox( e.getMessage());
		}
			 
		adapter=new ListAdaptDevCli(this,items);
		listView.setAdapter(adapter);
		
		lblTot.setText(mu.frmdec(tot));
		
		if (selidx>-1) {	
	        adapter.setSelectedIndex(selidx);
	        listView.smoothScrollToPosition(selidx);
		} 

		
	}
	
	private void processItem(){
		String pid;
		
		pid=((appGlobals) vApp).gstr;
		if (mu.emptystr(pid)) return;
		
		prodid=pid;
		
		setCant();
	}
	
	private void setCant(){
		browse=2;
		
		itempos=-1;
		((appGlobals) vApp).prod=prodid;
		((appGlobals) vApp).gstr="";
		Intent intent = new Intent(this,RecargCant.class);
		startActivity(intent);
	}
	
	private void updCant(String itemid){
		browse=2;	
		prodid=itemid;
		((appGlobals) vApp).prod=itemid;		
		startActivity(new Intent(this,RecargCant.class));
	}
	
	private void processCant(){
		double cnt;
		String raz;
		
		cnt=((appGlobals) vApp).dval;
		if (cnt<0) return;
		
		//if (cnt==0) {
		//	toastcent("No puede ser guardada ni enviada solicitud de inventario en cero");return;
		//}

		raz=((appGlobals) vApp).devrazon;
		ubas=((appGlobals) vApp).ubas;
		cant=cnt;
		
		addItem(raz);
	}
	
	private void addItem(String raz){
		
		try {
			sql="DELETE FROM D_SOLICINVD WHERE PRODUCTO='"+prodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
		}
	
		try {
			
			ins.init("D_SOLICINVD");
			
			ins.add("COREL",corel);
			ins.add("PRODUCTO",prodid);
			ins.add("COSTO",gl.costo);
			ins.add("CANT",cant);
			ins.add("TOTAL",cant*gl.costo);
			ins.add("UM",ubas);
					
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		try {
			sql="DELETE FROM D_SOLICINVD WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
		
		listItems();
				
	}
	
	private void saveDevol(){
			
		try {			
			sql="UPDATE D_SOLICINV SET STATCOM='N'";
			db.execSQL(sql);
			
			toastcent("Solicitud preparada para envio.");			
			super.finish();
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
		}			
	}
	
		
	// Aux 
	
	private void clearData(){
		try {
			sql="DELETE FROM D_SOLICINVD";
			db.execSQL(sql);
			
			super.finish();
			toastcent("Solicitud borrada.");
		} catch (SQLException e) {
		}	
	}
	
	private void msgAskClear(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	clearData();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	
	
	private void msgAskComplete(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	saveDevol();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	
	
	private void msgAskDelete(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	try {
					sql="DELETE FROM D_SOLICINVD WHERE PRODUCTO='"+prodid+"'";
					db.execSQL(sql);
					
					prodid="";
					listItems();
				} catch (SQLException e) {
					msgbox(e.getMessage());
				}
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	

	private boolean hasProducts(){
		Cursor DT;
		
		try {
			sql="SELECT * FROM D_SOLICINVD";	
			DT=Con.OpenDT(sql);
				
			return DT.getCount()>0;
		} catch (Exception e) {
			return false;
		}	
	}
	
	private void getCorel() {
		Cursor dt;
		
		try {
			sql="SELECT COREL FROM D_SOLICINV";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();		
			corel=dt.getString(0);
		} catch (Exception e) {
			corel="0";
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
	}
	
	
	// Activity Events
	
	@Override
	protected void onResume() {
	    super.onResume();	
	 
	    if (browse==1) {
	    	browse=0;
	    	processItem();return;
	    }
	   
	    if (browse==2) {
	    	browse=0;
	    	processCant();return;
	    }
	    
	}
		
	
}
