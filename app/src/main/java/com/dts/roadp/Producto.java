package com.dts.roadp;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class Producto extends PBase {

	private ListView listView;
	private EditText txtFilter;
	private Spinner spinFam;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();
	
	private ArrayList<clsCD> items;
	private ListAdaptProd adapter;
	
	private String famid,itemid,pname,prname,um,ubas;
	private int act,prodtipo;
	private double disp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_producto);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
		txtFilter = (EditText) findViewById(R.id.editText1);
		spinFam = (Spinner) findViewById(R.id.spinner1);
			
		prodtipo=gl.prodtipo;
		gl.prodtipo=0;
		this.setTitle("Producto");
		if (prodtipo==1) this.setTitle("Producto con existencia");
			
		items = new ArrayList<clsCD>();
		
		act=0;
		fillSpinner();
		
		setHandlers();
		
		listItems();
		
		//spinFam.requestFocus();
		
	}


	// Main
	
	public void limpiaFiltro(View view) {
		txtFilter.setText("");	
	}
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				try {

					Object lvObj = listView.getItemAtPosition(position);
		           	clsCD vItem = (clsCD)lvObj;
		           	
					itemid=vItem.Cod;
					prname=vItem.Desc;
					gl.um=vItem.um;
					gl.pprodname=prname;
					
					adapter.setSelectedIndex(position);
		    		
					appProd();
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
		           	clsCD vItem = (clsCD)lvObj;
		           	
					itemid=vItem.Cod;
					prname=vItem.Desc;
					gl.um=vItem.um;
					gl.pprodname=prname;
					
					adapter.setSelectedIndex(position);
		    		
					appProd();
		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
		    	return true;
		      }
		});
	
	    txtFilter.addTextChangedListener(new TextWatcher() {
		 
	    	public void afterTextChanged(Editable s) {}
		 
	    	public void beforeTextChanged(CharSequence s, int start,int count, int after) { }
		 
	    	public void onTextChanged(CharSequence s, int start,int before, int count) {
	    		int tl;
	    		
	    		tl=txtFilter.getText().toString().length();
	    		
	    		if (tl==0 || tl>1) {listItems();}
	    	}
	    });	
	    
		spinFam.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TextView spinlabel;
		    	String scod,idposition;
		    	
		    	try {
		    		spinlabel=(TextView)parentView.getChildAt(0);
			    	spinlabel.setTextColor(Color.BLACK);spinlabel.setPadding(5,0,0,0);
			    	spinlabel.setTextSize(18);spinlabel.setTypeface(spinlabel.getTypeface(), Typeface.BOLD);
			    
			    	scod=spincode.get(position);
		    		famid=scod;
		    		
		    		listItems();
		    		
		    		spinFam.requestFocus();
		    		//if (act>0) {hidekeyb();}
		    		hidekeyb();
		    		
		    		act+=1;
		         } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
		
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});	
		
	}
	
	private void listItems()
	{
		Cursor DT;
		clsCD vItem;
		int cantidad;
		String vF,cod,name,um;

		ArrayList<clsCD> vitems = new ArrayList<clsCD>();;

		items.clear();itemid="*";famid="0";

		vF=txtFilter.getText().toString().replace("'","");

		String sql = "";

		try {
					
			switch (prodtipo) {  
				case 0: // Preventa
					sql="SELECT CODIGO,DESCCORTA,'' FROM P_PRODUCTO WHERE 1=1 ";
					if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (LINEA='"+famid+"') ";
					if (vF.length()>0) {sql=sql+"AND ((DESCCORTA LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}
					sql+="ORDER BY DESCCORTA";				
					break;
					
				case 1:  // Venta   
					//sql="SELECT P_PRODUCTO.CODIGO,P_PRODUCTO.DESCCORTA,P_STOCK.UNIDADMEDIDA " +
					//	 "FROM  P_PRODUCTO INNER JOIN P_STOCK ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO " +
					//	 "WHERE P_STOCK.CANT>0 ";
					
					//sql="SELECT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA, P_PRODPRECIO.UNIDADMEDIDA " +
					//    "FROM P_PRODUCTO INNER JOIN	P_STOCK ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO INNER JOIN " +
					//    "P_PRODPRECIO ON (P_STOCK.CODIGO=P_PRODPRECIO.CODIGO) AND (P_STOCK.UNIDADMEDIDA = P_PRODPRECIO.UNIDADMEDIDA ) " +
					//    "WHERE (P_STOCK.CANT > 0) AND (P_PRODPRECIO.NIVEL = " + gl.nivel +")";

					sql="SELECT DISTINCT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA, P_PRODPRECIO.UNIDADMEDIDA " +
						    "FROM P_PRODUCTO INNER JOIN	P_STOCK ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO INNER JOIN " +
						    "P_PRODPRECIO ON (P_STOCK.CODIGO=P_PRODPRECIO.CODIGO)  " +
						    "WHERE (P_STOCK.CANT > 0) AND (P_PRODPRECIO.NIVEL = " + gl.nivel +")";

					
					if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P_PRODUCTO.LINEA='"+famid+"') ";
					if (vF.length()>0) sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
					
					sql+="UNION ";
					
					sql+="SELECT DISTINCT P_PRODUCTO.CODIGO,P_PRODUCTO.DESCCORTA,''  " +
							"FROM P_PRODUCTO "  +
							"WHERE (P_PRODUCTO.TIPO ='S') ";	
					if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P_PRODUCTO.LINEA='"+famid+"') ";
					if (vF.length()>0) sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
			
					sql+="ORDER BY P_PRODUCTO.DESCCORTA";		
					
					break;	
					
				case 2: // Mercadeo propio
					sql="SELECT CODIGO,DESCCORTA,'' FROM P_PRODUCTO WHERE 1=1 ";
					if (!mu.emptystr(famid)) {sql=sql+"AND (LINEA='"+famid+"') ";}
					if (vF.length()>0) {sql=sql+"AND ((DESCCORTA LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}
					sql+="ORDER BY DESCCORTA";				
					break;
					
				case 3:  // Mercadeo comp
					sql="SELECT CODIGO,NOMBRE,'' FROM P_MERPRODCOMP WHERE 1=1 ";
					if (!mu.emptystr(famid)) {sql=sql+"AND (MARCA='"+famid+"') ";}
					if (vF.length()>0) {sql=sql+"AND ((NOMBRE LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}
					sql+="ORDER BY NOMBRE";				
					break;	
			}
				
			DT=Con.OpenDT(sql);

			cantidad = DT.getCount();

			if (cantidad==0) return;
			
			DT.moveToFirst();

			while (!DT.isAfterLast()) {
				  
			  cod=DT.getString(0);	
			  name=DT.getString(1);
			  um=DT.getString(2);
			
			  vItem = clsCls.new clsCD();
			  
			  vItem.Cod=cod;
			  vItem.Desc=name;

			  //#EJC20181127: En aprof. no tienen un viene vacío, colocar por defecto un.
			  if (um.equalsIgnoreCase("")){
				  um = "UN";
			  }

			  vItem.um=um;
			  vItem.Text="";

			  items.add(vItem);
			  vitems.add(vItem);

			  DT.moveToNext();
			}
		} catch (Exception e)
		{
		   //	mu.msgbox( e.getMessage());
			Log.d("prods",e.getMessage());
	    }

	    items = (ArrayList<clsCD>) vitems.clone();

		adapter=new ListAdaptProd(this,vitems);
		listView.setAdapter(adapter);
		
		if (prodtipo==1) dispUmCliente();
		
	}
	
	private void appProd(){	
		gl.gstr=itemid;
		super.finish();
	}
	
	private void dispUmCliente() {
		String sdisp;
	 
		for (int i = items.size()-1; i >=0; i--) {
			if (getDisp(items.get(i).Cod)) {
				sdisp=mu.frmdecimal(disp,gl.peDecImp)+" "+ltrim(um,6);
				items.get(i).Text=sdisp;
			} else {	
				items.remove(i);
			}
		}
		
		adapter.notifyDataSetChanged();
		
	}
	
	private boolean getDisp(String prodid) {
		Cursor dt;
		String umstock;
		double umf1,umf2,umfactor;
		
		disp=0;
		
		try {			
			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();			
			um=dt.getString(0);
			
			sql="SELECT UNIDBAS	FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();

			ubas=dt.getString(0);
		} catch (Exception e) {
			return false;
		}
		
		try {
			sql="SELECT SUM(CANT) FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
				
  			disp=dt.getDouble(0);
  			return true;
		} catch (Exception e) {
	    }
		
		try {
			sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"')";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			umstock=dt.getString(0);
				
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";	
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf1=dt.getDouble(0);
			} else {	
				return false;
			}	
					
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";	
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf2=dt.getDouble(0);
			} else {	
				return false;
			}
			
			umfactor=umf1/umf2;			
			
			sql="SELECT CANT FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			disp=dt.getDouble(0);disp=disp/umfactor;
			
			return true;
		} catch (Exception e) {
			return false;
	    }	
		
	}
	
	
	// Aux
	
	private void fillSpinner(){
		Cursor DT;
		String icode,iname;
			
		spincode.add("0");
		spinlist.add("< TODAS >");
		  
		try {
			
			switch (prodtipo) {  
			case 0: // Preventa
				sql="SELECT Codigo,Nombre FROM P_LINEA ORDER BY Nombre";break;
			case 1:  // Venta   
				sql="SELECT DISTINCT P_PRODUCTO.LINEA,P_LINEA.NOMBRE "+
				     "FROM P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO " +
				     "INNER JOIN P_LINEA ON P_PRODUCTO.LINEA=P_LINEA.CODIGO " +
				     "WHERE (P_STOCK.CANT > 0) ORDER BY P_LINEA.NOMBRE";
				break;	
			case 2: // Mercadeo propio
				sql="SELECT Codigo,Nombre FROM P_LINEA ORDER BY Nombre";break;
			case 3:  // Mercadeo comp
				sql="SELECT Codigo,Nombre FROM P_MERMARCACOMP ORDER BY Nombre";break;	
			}			
						
			DT=Con.OpenDT(sql);					
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
					  
			  icode=DT.getString(0);
			  iname=DT.getString(1);
				  
			  spincode.add(icode);
			  spinlist.add(iname);
			  
			  DT.moveToNext();
			}
					
		} catch (SQLException e) {
			mu.msgbox(e.getMessage());
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }
					
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
		spinFam.setAdapter(dataAdapter);
			
		try {
			spinFam.setSelection(0);
		} catch (Exception e) {
			spinFam.setSelection(0);
	    }
		
	}	

	public String ltrim(String ss,int sw) {
		int l=ss.length();
		if (l>sw) {
			ss=ss.substring(0,sw);	
		} else {
			String frmstr="%-"+sw+"s";	
			ss=String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	
	// Activity Events
	
	protected void onResume() {
	    super.onResume();
	}


}
