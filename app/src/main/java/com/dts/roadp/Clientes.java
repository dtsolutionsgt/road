package com.dts.roadp;

import java.util.ArrayList;
import java.util.List;

import com.dts.roadp.clsClasses.clsCDB;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.widget.Toast;


public class Clientes extends PBase {

	private ListView listView;
	private Spinner spinList;
	private EditText txtFiltro;
	private TextView lblCant;
	
	private ArrayList<clsCDB> items= new ArrayList<clsCDB>();
	private ArrayList<String> cobros= new ArrayList<String>();
	private ArrayList<String> ppago= new ArrayList<String>();

	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> listcode = new ArrayList<String>();
	private ArrayList<String> listname = new ArrayList<String>();

	private ListAdaptCliList adapter;
	private clsCDB selitem;
	
	private int selidx,fecha,dweek,browse;
	private String selid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clientes);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
		spinList = (Spinner) findViewById(R.id.spinner1);
		txtFiltro = (EditText) findViewById(R.id.txtMonto);
		lblCant= (TextView) findViewById(R.id.lblCant);
				
		setHandlers();

		selid="";selidx=-1;
		
		dweek=mu.dayofweek();
		
		fillSpinners();
		
		listItems();
				
		closekeyb();

		gl.escaneo="N";
	}

	
	// Events
	
	public void applyFilter(View view){
		//listItems();
		//hidekeyb();
		txtFiltro.setText("");
	}
	
	public void showVenta(View view) {
		
		gl.tcorel=mu.getCorelBase();//gl.ruta/
				
		//Intent intent = new Intent(this,CliNuevoApr.class);
		Intent intent = new Intent(this,CliNuevo.class);
		startActivity(intent);
	}
	
	//#HS_20181211 Agregue funcion que lista las opciones de incidencia de no lectura
	private void listNoLectura(){
		Cursor DT;
		String code,name;

		listcode.clear();listname.clear();

		try {

			sql="SELECT Codigo,Nombre FROM P_CODNOLEC ORDER BY Nombre";

			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				try {
					code=String.valueOf(DT.getInt(0));
					name=DT.getString(1);

					listcode.add(code);
					listname.add(name);
				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
				DT.moveToNext();
			}
		} catch (Exception e) {
			mu.msgbox( e.getMessage());return;
		}

		showIncNoLectura();

	}
	//#HS_20181211 Funcion que abre el dialogo, opciones de incidencia de no lectura
	public void showIncNoLectura() {
		final AlertDialog Dialog;

		final String[] selitems = new String[listname.size()];

		for (int i = 0; i < listname.size(); i++) {
			selitems[i] = listname.get(i);
		}

		mMenuDlg = new AlertDialog.Builder(this);
		mMenuDlg.setTitle("Incidencia de no lectura");

		mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				try {
					//String opcion=listcode.get(item);
					showCliente();

				} catch (Exception e) {
				}
			}
		});

		mMenuDlg.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		Dialog = mMenuDlg.create();
		Dialog.show();

		Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
		nbutton.setTextColor(Color.WHITE);
	}

	// Main
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
							
				Object lvObj = listView.getItemAtPosition(position);
	           	clsCDB sitem = (clsCDB)lvObj;
	           	selitem=sitem;
	           
				selid=sitem.Cod;selidx=position;
				adapter.setSelectedIndex(position);

				//#HS_20181211 Carga la lista de incidencias por no lectura y muestra el dialogo
				if(gl.incNoLectura == true) {
					listNoLectura();
				}else {
					showCliente();
				}

			};
	    });
	
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				boolean pedit,pbor;
				try {
					Object lvObj = listView.getItemAtPosition(position);
					clsClasses.clsCDB item = (clsClasses.clsCDB) lvObj;

					selid=item.Cod;selidx=position;
					adapter.setSelectedIndex(position);
					
					pedit=puedeeditarse();
					pbor=puedeborrarse();
					
					if (pbor && pedit) {
						showItemMenu();							
					} else {	
						if (pbor)  msgAskBor("Eliminar cliente nuevo");	
						if (pedit) msgAskEdit("Cambiar datos de cliente nuevo");
					}						
					
				} catch (Exception e) {
				}
				return true;
			}
		});

		spinList.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TextView spinlabel;
		    	String scod;
		    	
		    	try {
		    		spinlabel=(TextView)parentView.getChildAt(0);
			    	spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(18);
			    
			    	dweek=position;
			    	
			    	listItems();

		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
		
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});	
				
	    txtFiltro.addTextChangedListener(new TextWatcher() {
			 
	    	public void afterTextChanged(Editable s) {}
		 
	    	public void beforeTextChanged(CharSequence s, int start,int count, int after) { }
		 
	    	public void onTextChanged(CharSequence s, int start,int before, int count) {
	    		int tl;
	    		
	    		tl=txtFiltro.getText().toString().length();

	    		if (tl>1) gl.escaneo="";
	    		if (tl==0 || tl>1) listItems();
	    	}
	    });	
	}	
	
	public void listItems() {
		Cursor DT;
		clsCDB vItem;	
		int vP;
		String id,filt,ss;
			
		items.clear();
		
		selidx=-1;vP=0;
		filt=txtFiltro.getText().toString().replace("'","");
		
		try {

			cobros.clear();
			sql="SELECT DISTINCT  CLIENTE FROM D_COBRO WHERE (ANULADO='N') ";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {
				DT.moveToFirst();
				for (int i = 0; i <DT.getCount(); i++) {
					try {
						cobros.add(DT.getString(0));
						DT.moveToNext();
					} catch (Exception e) {
					}
				}
			}

			ppago.clear();
			sql="SELECT D_FACTURA.CLIENTE " +
					"FROM D_FACTURA INNER JOIN  D_FACTURAP ON  D_FACTURA.COREL = D_FACTURAP.COREL " +
					"GROUP BY  D_FACTURA.CLIENTE, D_FACTURA.COREL, D_FACTURA.ANULADO " +
					"HAVING  (D_FACTURA.ANULADO='N') AND (SUM(D_FACTURAP.VALOR=0))";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0) {
				DT.moveToFirst();
				for (int i = 0; i <DT.getCount(); i++) {
					try {
						ss=DT.getString(0);
						if (!ppago.contains(ss)) {
							ppago.add(ss);toast(ss);
						}
						DT.moveToNext();
					} catch (Exception e) {
					}
				}
			}

			sql="SELECT DISTINCT P_CLIRUTA.CLIENTE,P_CLIENTE.NOMBRE,P_CLIRUTA.BANDERA "+
				 "FROM P_CLIRUTA INNER JOIN P_CLIENTE ON P_CLIRUTA.CLIENTE=P_CLIENTE.CODIGO "+
				 "WHERE (1=1) ";
			
			if (mu.emptystr(filt)) {
				 if (dweek!=0) sql+="AND (P_CLIRUTA.DIA ="+dweek+") ";
			}
           
            if (!mu.emptystr(filt)) {
            	sql+="AND ((P_CLIRUTA.CLIENTE LIKE '%"+filt+"%') OR (P_CLIENTE.NOMBRE LIKE '%"+filt+"%')) ";
            }
            sql+="ORDER BY P_CLIRUTA.SECUENCIA,P_CLIENTE.NOMBRE";
			
			DT=Con.OpenDT(sql);
			
			lblCant.setText(""+DT.getCount()+"");
			
			if (DT.getCount()>0) {
			
				DT.moveToFirst();
				while (!DT.isAfterLast()) {
				  
					id=DT.getString(0);
					
					vItem =clsCls.new clsCDB();
			  	
					vItem.Cod=DT.getString(0);ss=DT.getString(0);
					vItem.Desc=DT.getString(1);
					vItem.Bandera=DT.getInt(2);

					if (cobros.contains(ss)) vItem.Cobro=1;else vItem.Cobro=0;
					if (ppago.contains(ss)) vItem.ppend=1;else vItem.ppend=0;

					items.add(vItem);	
			 
					if (id.equalsIgnoreCase(selid)) {selidx=vP;}
					vP+=1;
			  
					DT.moveToNext();
				}	
			}
			
		} catch (Exception e) {
		   	mu.msgbox(e.getMessage());
	    }
			 
		adapter=new ListAdaptCliList(this, items);
		listView.setAdapter(adapter);
		
		if (selidx>-1) {
			adapter.setSelectedIndex(selidx);
			listView.setSelection(selidx);
		}
	    	    
	}
	
	public void showCliente() {
		
		gl.cliente=selid;
		
		gl.closeCliDet=false;
		gl.closeVenta=false;
		
		Intent intent;
		intent = new Intent(this,CliDet.class);
		startActivity(intent);
	}
	
	private void editCliente() {
		gl.tcorel=selid;
		startActivity(new Intent(this,CliNuevoAprEdit.class));
	}
	
	
	// Aux

    private void showItemMenu() {
        final AlertDialog Dialog;
        final String[] selitems = {"Eliminar cliente","Cambiar datos"};
     
        AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
        menudlg.setTitle("Cliente nuevo");

        menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
		            switch (item) {
		                case 0:
		                	msgAskBor("Eliminar cliente");break;
		                case 1:
		                	editCliente();break;
		            }

		            dialog.cancel();
	            }
        });

        menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
        @Override
	            public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
	            }
        });

        Dialog = menudlg.create();
        Dialog.show();
    }

	private void fillSpinners(){
		List<String> dlist = new ArrayList<String>();
			
		dlist.add("Todos");
		dlist.add("Lunes");
		dlist.add("Martes");
		dlist.add("Miercoles");
		dlist.add("Jueves");
		dlist.add("Viernes");
		dlist.add("Sabado");
		dlist.add("Domingo");		
				
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, dlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinList.setAdapter(dataAdapter);
		spinList.setSelection(dweek);
	
	}

	private boolean puedeborrarse() {
		Cursor dt;
		String sql;

		try {
			sql="SELECT * FROM D_CLINUEVO WHERE CODIGO='"+selid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

			sql="SELECT * FROM D_FACTURA WHERE CLIENTE='"+selid+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) return false;
		
		} catch (Exception e) {
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private boolean puedeeditarse() {
		Cursor dt;
		String sql;

		try {
			sql="SELECT * FROM D_CLINUEVO WHERE CODIGO='"+selid+"' AND STATCOM='N'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;
		
		} catch (Exception e) {
			mu.msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}
	
	private void msgAskBor(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setMessage("¿" + msg + "?");

		dialog.setNegativeButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {	
				borraCliNuevo();
			}
		});

		dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});

		dialog.show();

	}

	private void msgAskEdit(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setMessage("¿" + msg + "?");

		dialog.setNegativeButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {	
				editCliente();			
			}
		});

		dialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}
		});

		dialog.show();

	}
	
	private void borraCliNuevo() {
		try {
			db.beginTransaction();

			db.execSQL("DELETE FROM D_CLINUEVO WHERE CODIGO='"+selid+"'");
			db.execSQL("DELETE FROM P_CLIRUTA WHERE CLIENTE='"+selid+"'");
					
			db.setTransactionSuccessful();
			db.endTransaction();
			
			listItems();
		} catch (Exception e) {
			db.endTransaction();
			mu.msgbox(e.getMessage());
		}
	}
	
	
	// Activity Events
	
	protected void onResume() {
	   super.onResume();
	    
	   listItems();
	}

	

}
