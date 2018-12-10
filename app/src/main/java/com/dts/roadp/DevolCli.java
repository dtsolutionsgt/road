package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCFDV;
import com.dts.roadp.clsClasses.clsVenta;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class DevolCli extends PBase {

	private ListView listView;
	
	private ArrayList<clsClasses.clsCFDV> items= new ArrayList<clsClasses.clsCFDV>();
	private ListAdaptDevCli adapter;
	private clsClasses.clsCFDV selitem;
	
	private String cliid,itemid,prodid;
	private double cant;
	private String emp,estado;
	private int itempos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devol_cli);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
	
		emp=((appGlobals) vApp).emp;
		estado=((appGlobals) vApp).devtipo;
		cliid=((appGlobals) vApp).cliente;
		
		setHandlers();
		
		browse=0;
		fecha=du.getActDateTime();
		((appGlobals) vApp).devrazon="0";
		
		clearData();
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
			mu.msgbox("No puede continuar, no ha agregado ninguno producto !");return;
		}
		
		msgAskComplete("Aplicar la devolucion");
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
		    		
					updCant(vItem.id);
					
		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
			};
	    });
	    
	}
	
	private void listItems(){
		Cursor DT;
		clsClasses.clsCFDV vItem;	
		String s;
				
		items.clear();
		
		try {
			
			sql="SELECT T_CxCD.CODIGO, T_CxCD.CANT, P_CODDEV.DESCRIPCION, P_PRODUCTO.DESCCORTA, T_CxCD.ITEM "+
			     "FROM T_CxCD INNER JOIN P_PRODUCTO ON P_PRODUCTO.CODIGO=T_CxCD.CODIGO "+
				 "INNER JOIN P_CODDEV ON (P_CODDEV.CODIGO=T_CxCD.CODDEV AND P_CODDEV.ESTADO='"+estado+"') "+
			     "ORDER BY P_PRODUCTO.DESCCORTA";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  vItem = clsCls.new clsCFDV();
			  
			  vItem.Cod=DT.getString(0);
			  vItem.Desc=DT.getString(3);
			  vItem.Valor=DT.getString(2);
			  s=mu.frmdec(DT.getDouble(1));
			  vItem.Fecha=s;
			  vItem.id=DT.getInt(4);
			  
			  items.add(vItem);	
			 
			  DT.moveToNext();
			}
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptDevCli(this,items);
		listView.setAdapter(adapter);
	}
	
	private void processItem(){
		String pid;
		
		pid=((appGlobals) vApp).gstr;
		if (mu.emptystr(pid)) {return;}
		
		prodid=pid;
		
		setCant();
	}
	
	private void setCant(){
		browse=2;
		
		itempos=-1;
		((appGlobals) vApp).prod=prodid;
		((appGlobals) vApp).gstr="";
		Intent intent = new Intent(this,DevCliCant.class);
		startActivity(intent);
	}
	
	private void updCant(int item){
		Cursor DT;
		String prid,rz;
		double pcant;
		
		try {
			sql="SELECT CODIGO,CODDEV,CANT FROM T_CxCD WHERE Item="+item;	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			
			prid=DT.getString(0);
			rz=DT.getString(1);
			
			
		} catch (Exception e) {
			return;
		}	
		
		browse=2;
		
		itempos=item;
		((appGlobals) vApp).prod=prid;
		((appGlobals) vApp).gstr=rz;
		//((appGlobals) vApp).dval=pcant;
		
		Intent intent = new Intent(this,DevCliCant.class);
		startActivity(intent);
	}
	
	private void processCant(){
		double cnt;
		String raz;
		
		cnt=((appGlobals) vApp).dval;
		if (cnt<0) return;

		raz=((appGlobals) vApp).devrazon;
		cant=cnt;
		
		addItem(raz);
	}
	
	private void addItem(String raz){
		Cursor DT;
		int id;
		
		try {
			sql="DELETE FROM T_CxCD WHERE item="+itempos;
			db.execSQL(sql);
		} catch (SQLException e) {
		}
		
		try {
			sql="DELETE FROM T_CxCD WHERE CODIGO='"+prodid+"' AND CODDEV='"+raz+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
		}
		
		try {
			sql="SELECT MAX(Item) FROM T_CxCD";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			id=DT.getInt(0);
		} catch (Exception e) {
			id=0;
		}	
		
		id+=1;
		
		try {
			
			ins.init("T_CxCD");
			
			ins.add("Item",id);
			ins.add("CODIGO",prodid);
			ins.add("CANT",cant);
			ins.add("CODDEV",raz);
			ins.add("TOTAL",0);
			ins.add("PRECIO",0);
			ins.add("PRECLISTA",0);
			ins.add("REF","");
			
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		try {
			sql="DELETE FROM T_CxCD WHERE CANT=0";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
		
		listItems();
				
	}
	
	private void saveDevol(){
		Cursor DT;
		String corel,pcod;
		Double pcant;
		
		corel=((appGlobals) vApp).ruta+"_"+mu.getCorelBase();
		
		try {
			
			db.beginTransaction();
			
			ins.init("D_CxC");
			
			ins.add("COREL",corel);
			ins.add("RUTA",((appGlobals) vApp).ruta);
			ins.add("CLIENTE",((appGlobals) vApp).cliente);
			ins.add("FECHA",fecha);
			ins.add("ANULADO","N");
			ins.add("EMPRESA",((appGlobals) vApp).emp);
			ins.add("TIPO",estado);
			ins.add("REFERENCIA","");
			ins.add("IMPRES",0);
			ins.add("STATCOM","N");
			ins.add("VENDEDOR",((appGlobals) vApp).vend);
			ins.add("TOTAL",0);
		
			db.execSQL(ins.sql());
			
			sql="SELECT Item,CODIGO,CANT,CODDEV FROM T_CxCD WHERE CANT>0";
			DT=Con.OpenDT(sql);
	
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
			
				pcod=DT.getString(1);
				pcant=DT.getDouble(2);
				
			  	ins.init("D_CxCD");
				ins.add("COREL",corel);
				ins.add("ITEM",DT.getInt(0));
				ins.add("CODIGO",DT.getString(1));
				ins.add("CANT",DT.getDouble(2));
				ins.add("CODDEV",DT.getString(3));
				ins.add("ESTADO",estado);
				ins.add("TOTAL",0);
				ins.add("PRECIO",0);
				ins.add("PRECLISTA",0);
				ins.add("REF","");
				ins.add("PESO",0);
				ins.add("FECHA_CAD",0);
				ins.add("LOTE","");
			
			    db.execSQL(ins.sql());
			    
			    try {
			    	sql="INSERT INTO P_STOCK VALUES ('"+pcod+"',0,0,0)";
			    	db.execSQL(sql);
			    } catch (Exception e) {
			    }
			    
			    if (estado.equalsIgnoreCase("M")) {
			    	 sql="UPDATE P_STOCK SET CANTM=CANTM+"+pcant+" WHERE CODIGO='"+pcod+"'";	
			    } else {	
			    	 sql="UPDATE P_STOCK SET CANT=CANT+"+pcant+" WHERE CODIGO='"+pcod+"'";	
			    }
			    db.execSQL(sql);
					
			    DT.moveToNext();
			}

			db.setTransactionSuccessful();
				
			db.endTransaction();
			
			Toast.makeText(this,"Devolucion guardada", Toast.LENGTH_SHORT).show();
			
			super.finish();
		} catch (Exception e) {
			db.endTransaction();
		   	mu.msgbox( e.getMessage());
		}	
		
	}
	
	// Aux 
	
	private void clearData(){
		try {
			sql="DELETE FROM T_CxCD";
			db.execSQL(sql);
		} catch (SQLException e) {
		}	
	}
	
	private void msgAskExit(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	doExit();
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

	private boolean hasProducts(){
		Cursor DT;
		
		try {
			sql="SELECT CODIGO FROM T_CxCD";	
			DT=Con.OpenDT(sql);
				
			return DT.getCount()>0;
		} catch (Exception e) {
			return false;
		}	
	}
	
	private void doExit(){
		super.finish();
	}
	
	// Activity Events
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    if (((appGlobals) vApp).closeVenta) super.finish();
	    
	    if (browse==1) {
	    	browse=0;
	    	processItem();return;
	    }
	    
	    if (browse==2) {
	    	browse=0;
	    	processCant();return;
	    }
	
	}

	@Override
	public void onBackPressed() {
		msgAskExit("Salir sin terminar devolucion");
	}
}
