package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsCobro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class Pago extends PBase {

	private ListView listView;
	private EditText txtMonto;
	private TextView lblSaldo,lblTotal;
	
	private ArrayList<clsClasses.clsPago> items= new ArrayList<clsClasses.clsPago>();
	private ListAdaptPago adapter;
	private clsClasses.clsPago selitem;
	
	private AlertDialog.Builder mMenuDlg;
	private ArrayList<String> lcode = new ArrayList<String>();
	private ArrayList<String> lname = new ArrayList<String>();
	private ArrayList<String> bcode = new ArrayList<String>();
	private ArrayList<String> bname = new ArrayList<String>();
	
	private String cliid,tpago,desc1,desc2,desc3,bc,bn;
	private int nivel,cpago,pagomodo;
	private double pago,ttot,saldo,monto,pagolim;
	private boolean cobro;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pago);
		
		super.InitBase();
		
		listView = (ListView) findViewById(R.id.listView1);
		lblSaldo = (TextView) findViewById(R.id.lblpSaldo);
		lblTotal = (TextView) findViewById(R.id.lblPTotal);
		txtMonto = (EditText) findViewById(R.id.txtMonto);
		
		setHandlers();
		
		saldo=gl.pagoval;
		pagolim=gl.pagolim;
		cobro=gl.pagocobro;
		cliid=gl.cliente;
		pagomodo=gl.pagomodo;

		setNivel();
		
		initSession();
		
		listPagos();
		listBancos();
		
		listItems();
	}

	
	// Events
	
	public void addPayment(View view){
		String s;
		
		try {
			s=txtMonto.getText().toString();
			pago=Double.parseDouble(s);
			if (pago<=0) throw new Exception();
		} catch (Exception e) {
			pago=0;
			mu.msgbox("Monto incorrecto");return;
	    }

	    if (pagomodo==0) {
			if (totalPago() + pago > pagolim) {
				mu.msgbox("Total de pagos mayor que total de saldos.");
				return;
			}

			if (totalPago() + pago > saldo) {
				msgAskOverPayd("Total de pagos mayor que saldo\nContinuar");
			} else {
				showPagoDialog();
			}
		} else {
			showPagoDialog();
		}

	}
	
	public void deletePayment(View view){
		
		if (selid==0) {
			mu.msgbox("Debe seleccionar un pago");return;
		}
		
		msgAskDel("Eliminar pago");
	}
	
	public void savePago(View view){
		if (pagomodo==0) {
			finalCheck();
		} else {
			gl.pagado=totalPago()>0;
		}
	}
	
	
	// Main
	
	private void setHandlers(){
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
	        public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				
				try {
					Object lvObj = listView.getItemAtPosition(position);
					selitem = (clsClasses.clsPago)lvObj;
			           	
					adapter.setSelectedIndex(position);

					selid= selitem.id;

		        } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
		        }
			};
	    });
		    
	}

	private void listItems(){
		Cursor DT;
		clsClasses.clsPago vItem;	
				
		items.clear();selidx=-1;
				
		try {
			sql="SELECT T_PAGO.ITEM,P_MEDIAPAGO.NOMBRE,T_PAGO.VALOR,T_PAGO.DESC1,T_PAGO.DESC2 " +
				 "FROM T_PAGO INNER JOIN P_MEDIAPAGO ON P_MEDIAPAGO.CODIGO=T_PAGO.CODPAGO ORDER BY T_PAGO.ITEM";
			
			DT=Con.OpenDT(sql);
			if (DT.getCount()>0) DT.moveToFirst();

			while (!DT.isAfterLast()) {
				  
				vItem = clsCls.new clsPago();
			  		  
				vItem.id=DT.getInt(0);
				vItem.Tipo=DT.getString(1);
				vItem.Valor=mu.frmdec(DT.getDouble(2));
				vItem.Num=DT.getString(3)+" - "+DT.getString(4);
			  
				items.add(vItem);	
			 
				DT.moveToNext();
			}
			
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }
			 
		adapter=new ListAdaptPago(this,items);
		listView.setAdapter(adapter);
	}	
	
	private void addPago(){
		Cursor DT;
		int id;
		
		try {
			sql="SELECT MAX(Item) FROM T_PAGO";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			id=DT.getInt(0);
		} catch (Exception e) {
			id=0;
		}	
		
		id+=1;
		
		try {
			
			ins.init("T_PAGO");
			
			ins.add("ITEM",id);
			ins.add("CODPAGO",cpago);
			ins.add("TIPO",tpago);
			ins.add("VALOR",pago);
			ins.add("DESC1",desc1);
			ins.add("DESC2",desc2);
			ins.add("DESC3",desc3);
			
	    	db.execSQL(ins.sql());
	    	
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		listItems();
		
		actMonto();
		
	}
	
	private void delPago(){
		try {
			sql="DELETE FROM T_PAGO WHERE ITEM="+selid;
	    	db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
		listItems();
		
		actMonto();		
	}
	
	private void initSession(){
		
		try {
			sql="DELETE FROM T_PAGO";
			db.execSQL(sql);
			
			sql="DELETE FROM T_PAGOD";
			db.execSQL(sql);
			
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());
	    }	
		
		lblSaldo.setText(mu.frmdec(saldo));
		actMonto();
	}
	
	private void finalCheck() {
		if (totalPago()< saldo) {
			msgAskSaveEmpty("El saldo es mayor que el monto . Salir");
		} else {
			msgAskSave("Aplicar pagos y continuar?");
		}
	}
	
	
	// Tipo pago
	
	private void selPago(int pidx) {
		String s;
		
		s=lcode.get(pidx);cpago=0;
		
		// Efectivo
		if (s.equalsIgnoreCase("1")) { 
			cpago=1;tpago="E";desc1=" ";desc2=" ";desc3=" ";
			addPago();
			return;
		}
		
		// Cheque
		if (s.equalsIgnoreCase("2")) { 
			cpago=2;tpago="C";
			showBancoDialog();
			return;
		}		
		
		// Tarjeta
		if (s.equalsIgnoreCase("3")) { 
			cpago=3;tpago="K";
			showBancoDialog();
			return;
		}			
		
	}
	
	private void listPagos(){
		Cursor DT;
		String code,name;
			
		lcode.clear();lname.clear();
		
		try {
			
			if (cobro) {
				sql="SELECT Codigo,Nombre FROM P_MEDIAPAGO WHERE (NIVEL<="+nivel+") AND (ACTIVO='S') AND (PORCOBRO='S') ORDER BY Codigo";
			} else {	
				sql="SELECT Codigo,Nombre FROM P_MEDIAPAGO WHERE (NIVEL<="+nivel+") AND (ACTIVO='S') ORDER BY Codigo";
			}
		
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  try {
				  code=String.valueOf(DT.getInt(0));
				  name=DT.getString(1);
				  
				  lcode.add(code);
				  lname.add(name);
			  } catch (Exception e) {
				  mu.msgbox(e.getMessage()); 
			  }
			  DT.moveToNext();
			}
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());return;
	    }
		
	}
	
	public void showPagoDialog() {
		final AlertDialog Dialog;
		   
	    final String[] selitems = new String[lname.size()];
	    for (int i = 0; i < lname.size(); i++) {
	    	selitems[i] = lname.get(i);
	    }
		    
	    mMenuDlg = new AlertDialog.Builder(this);
	    mMenuDlg.setTitle("Tipo Pago");	    	
					
	    mMenuDlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					try {
						selPago(item);
						dialog.dismiss();
					} catch (Exception e) {
				    }
				}
		});
				
	    mMenuDlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
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

	
	// Banco / Numero
	
	private void listBancos(){
		Cursor DT;
		String code,name;
			
		bcode.clear();bname.clear();
		
		try {
			
			sql="SELECT Codigo,Nombre FROM P_BANCO WHERE (TIPO<>'D') ORDER BY Nombre";
		
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {return;}
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
			  try {
				  code=DT.getString(0);
				  name=DT.getString(1);
				  
				  bcode.add(code);
				  bname.add(name);
			  } catch (Exception e) {
				  mu.msgbox(e.getMessage()); 
			  }
			  DT.moveToNext();
			}
		} catch (Exception e) {
		   	mu.msgbox( e.getMessage());return;
	    }
		
	}
	
	public void showBancoDialog() {
		final AlertDialog Dialog;
		   
	    final String[] selitems = new String[bname.size()];
	    for (int i = 0; i < bname.size(); i++) {
	    	selitems[i] = bname.get(i);
	    }
		    
	    mMenuDlg = new AlertDialog.Builder(this);
	    mMenuDlg.setTitle("Banco");	    	
					
	    mMenuDlg.setSingleChoiceItems(selitems , -1,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					try {
						bc=bcode.get(item);
						bn=bname.get(item);
						inputNumero();
						
						dialog.dismiss();
					} catch (Exception e) {
						dialog.dismiss();
				    }
				}
		});
				
	    mMenuDlg.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
			@Override
				public void onClick(DialogInterface dialog, int which) {
				}
		});
			
	    Dialog = mMenuDlg.create();
		Dialog.show();
	}
	
	private void inputNumero() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		if (cpago==2) {
			alert.setTitle("Numero de Cheque");
		} else {
			alert.setTitle("Numero de tarjeta");
		}
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER);	
		input.setText("");input.requestFocus();
		
		alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			   	//closekeyb();
		    	checkNum(input.getText().toString());
		  	}
		});

		alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				closekeyb();
			}
		});

		alert.show();
	}
	
	private void checkNum(String s) {
		
		if (mu.emptystr(s)) {
			showkeyb();
			inputNumero(); 
			mu.msgbox("Numero incorrecto");showkeyb();
			return;
		}
		
		desc1=s;desc2=bn;desc3=bc;
		
		addPago();
	}
	
	// Aux
	
	private void setNivel(){
		Cursor DT;
		String s;
		
		try {
			sql="SELECT MEDIAPAGO FROM P_CLIENTE WHERE Codigo='"+cliid+"'";
		
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			s=DT.getString(0).trim();
			nivel=Integer.parseInt(s);
		} catch (Exception e) {
			nivel=1;return;
	    }	
	}

	private double totalPago() {
		Cursor DT;
		
		try {
			sql="SELECT SUM(Valor) FROM T_PAGO";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			return DT.getDouble(0);
		} catch (Exception e) {
			return -1;
		}		
	}
	
	private void actMonto(){
		double tp=totalPago();
		
		monto=saldo-tp;
		if (monto<0) monto=0;
		monto=mu.round2(monto);
		
		txtMonto.setText(""+monto);if (monto==0) txtMonto.setText("");
		lblTotal.setText(mu.frmdec(tp));

		if (pagomodo==0) {
			if (monto==0) finalCheck();
		}

	}
	
	private void doExit(){
		closekeyb();
		super.finish();
	}
	
	// MsgDialogs
	
	private void msgAskOverPayd(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	showPagoDialog();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	
	
	private void msgAskExit(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	msgAskExit2("Está seguro");
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	
	
	private void msgAskExit2(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {	
		    	((appGlobals) vApp).pagado=false;
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
	
	private void msgAskSave(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	gl.pagado=totalPago()>0;
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
	
	private void msgAskSaveEmpty(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		   		((appGlobals) vApp).pagado=false;
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
	
	private void msgAskDel(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg  + " ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	delPago();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	;
		    }
		});
		
		dialog.show();
			
	}	
	
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		
		if (totalPago()==0) {
			((appGlobals) vApp).pagado=false;
			super.onBackPressed();
		} else {	
			msgAskExit("Salir sin aplicar pago");
		}
		
	}	
	
}
