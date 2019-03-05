package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsMenu;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.widget.Spinner;
import android.widget.TextView;

public class Menu extends PBase {

	private GridView gridView;
	private RelativeLayout relbotpan;
	private TextView lblVendedor,lblRuta;
	
	private ArrayList<clsMenu> items= new ArrayList<clsMenu>();

	//#HS_20181207 Controles para seleccionar el ayudante y vehiculo.
	private Spinner Ayudante,Vehiculo;
	private TextView lblAyudante,lblVehiculo;
	private ArrayList<String> listIDAyudante = new ArrayList<String>();
	private ArrayList<String> listAyudante = new ArrayList<String>();
	private ArrayList<String> listIDVehiculo = new ArrayList<String>();
	private ArrayList<String> listVehiculo = new ArrayList<String>();

	private ListAdaptMenuGrid adaptergrid;
	private AlertDialog.Builder menudlg;
	
	private int selId,selIdx,menuid,iicon;
	private String rutatipo,sdoc;
	private boolean rutapos,horizpos;
	
	private final int mRequestCode = 1001;
	private Exist Existencia = new Exist();

	@Override
	protected void onCreate(Bundle savedInstanceState) 	{

		try {

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_menu);

			super.InitBase();
			addlog("Menu",""+du.getActDateTime(),gl.vend);

			gridView = (GridView) findViewById(R.id.gridView1);
			relbotpan = (RelativeLayout) findViewById(R.id.relbotpan);

			//#HS_20181206_0945 Agregue lblVendedor que se muestra en el menú.
			lblVendedor = (TextView) findViewById(R.id.lblVendedor);
			lblRuta = (TextView) findViewById(R.id.textView9);

			Ayudante = new Spinner(this);
			Vehiculo = new Spinner(this);
			lblAyudante = new TextView(this);
			lblAyudante.setText("Ayudante:");
			lblVehiculo = new TextView(this);
			lblVehiculo.setText("Vehículo:");
			///

			vApp=this.getApplication();
			rutatipo=gl.rutatipog;

			rutapos=false;gl.rutapos=false;

			iicon=1;

			if (rutatipo.equalsIgnoreCase("T")) {
				sdoc="Factura + Pedido";
			} else {
				if (rutatipo.equalsIgnoreCase("V")) {
					sdoc="Venta";iicon=102;
				} else {
					sdoc="Preventa";iicon=101;
				}
			}

			if (rutatipo.equalsIgnoreCase("R")) {
				sdoc="Venta";
				rutapos=true;
				gl.rutapos=true;
			}

			selId=-1;selIdx=-1;

			setHandlers();

			int ori=this.getResources().getConfiguration().orientation; // 1 - portrait , 2 - landscape
			horizpos=ori==2;

			if (horizpos) {
				gridView.setNumColumns(3);relbotpan.setVisibility(View.GONE);
			} else {
				gridView.setNumColumns(3);relbotpan.setVisibility(View.VISIBLE);
			}

			this.setTitle("ROAD");
			listItems();

			if (gl.peVehAyud) {
				AyudanteVehiculo();
			} else {
				gl.ayudanteID="";gl.vehiculoID="";
			}

			ConfImpresora();

		}catch (Exception e)
		{
			Log.e("Mnu", e.getMessage());
		}

	}

	//region  Main
	
	public void showClients(View view) {
		try{
			Intent intent;
			intent = new Intent(this,Clientes.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void listItems() {
		try{
			clsMenu item;

			items.clear();selIdx=-1;

			try {

				item = clsCls.new clsMenu();
				item.ID=1;item.Name=sdoc;item.Icon=iicon;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=2;item.Name="Comunicación";item.Icon=2;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=3;item.Name="Reimpresión";item.Icon=3;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=4;item.Name="Anulación";item.Icon=4;
				items.add(item);

				if (!rutapos) {
					item = clsCls.new clsMenu();
					item.ID=5;item.Name="Consultas";item.Icon=5;
					items.add(item);
				}

				item = clsCls.new clsMenu();
				item.ID=6;item.Name="Depósito";item.Icon=6;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=7;item.Name="Inventario";item.Icon=7;
				items.add(item);

			//#HS_20181211 Agregue nuevo boton en menu: Inicio de dia.
			//item = clsCls.new clsMenu();
			//item.ID=11;item.Name="Inicio de día";item.Icon=8;
			//items.add(item);

				item = clsCls.new clsMenu();
				item.ID=8;item.Name="Cierre del día";item.Icon=8;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=9;item.Name="Utilerias";item.Icon=9;
				items.add(item);

				item = clsCls.new clsMenu();
				item.ID=10;item.Name="Cambio usuario";item.Icon=10;
				items.add(item);
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			}

			adaptergrid=new ListAdaptMenuGrid(this, items);
			gridView.setAdapter(adaptergrid);
			adaptergrid.setSelectedIndex(selIdx);

			//#HS_20181206 agrega el vendedor en lblVendedor y la ruta.
			lblVendedor.setText(gl.vendnom);
			lblRuta.setText(gl.ruta);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}
		
	public void setHandlers(){
	    try{
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

					clsMenu vItem = (clsMenu) adaptergrid.getItem(position);
					menuid=vItem.ID;

					adaptergrid.setSelectedIndex(position);

					showMenuItem();
				}
			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}

	private void showMenuItem() {
		int prtype;
		boolean epssetflag = false;
		Float cantidad;
		//toast("menu id : "+menuid);

		try{
			prtype = getPrinterType();
			if (prtype == 2) {
				if (gl.mPrinterSet) epssetflag = false;
				else epssetflag = true;
			}

			if (menuid == 2) epssetflag = false;
			if (menuid == 9) epssetflag = false;

			if (epssetflag) {
				Intent intent = new Intent(this, UtilPrint.class);
				startActivity(intent);
				return;
			}


			switch (menuid) {

				case 1:

					if (rutapos) {
						Intent intentp = new Intent(this, CliPos.class);
						startActivity(intentp);
					} else {
						gl.filtrocli=-1;
						Intent intent = new Intent(this, Clientes.class);
						//Asigna conexión actual al siguiente activity.

						//#HS_201811 Verifica si hay existencias disponibles.
						Existencia.Con = Con;
						cantidad = Existencia.CantExistencias();

						//#HS_20181206 Verifica el usuario si es DTS.
						if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")){
							mu.msgbox("No puede realizar esta acción");
						}else {

							if(gl.vnivel == 2){
								msgAskSupervisor1();
							}else {
								startActivity(intent);
							}
						}

					}

					break;

				case 2:  // Comunicacion
					gl.findiaactivo=false;
					gl.tipo = 0;
					gl.autocom = 0;
					gl.modoadmin = false;
					Intent intent2 = new Intent(this, ComWS.class);
					startActivity(intent2);
					break;


				case 3:  // Reimpresion

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutatipo.equalsIgnoreCase("T")) {
							showPrintMenuTodo();
						} else {
							if (rutatipo.equalsIgnoreCase("V")) {
								if (gl.peAceptarCarga) {
									showPrintMenuVentaApr();
								} else {
									showPrintMenuVenta();
								}
							} else {
								showPrintMenuPreventa();
							}
						}
					}

					break;

				case 4:  // Anulacion

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutapos) {
							showVoidMenuVenta();
						} else {
							if (rutatipo.equalsIgnoreCase("T")) {
								showVoidMenuTodo();
							} else {
								if (rutatipo.equalsIgnoreCase("V")) showVoidMenuVenta();
								else showVoidMenuPreventa();
							}
						}
					}

					break;

				case 5:  // Consultas

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						showConsMenu();
					}

					break;

				case 6:  // Deposito

					getDepTipo();

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						Intent intent6 = new Intent(this, Deposito.class);
						startActivity(intent6);
					}

					break;

				case 7:  // Inventario

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						if (rutatipo.equalsIgnoreCase("P")) {
							showInvMenuPreventa();
						} else {
							showInvMenuVenta();
						}
					}

					break;

				case 8:  // Fin Dia

					//#HS_20181206 Verifica el usuario si es DTS.
					if(gl.vendnom.equalsIgnoreCase("DTS") && gl.vend.equalsIgnoreCase("DTS")) {
						mu.msgbox("No puede realizar esta acción");
					}else {
						Intent intent8 = new Intent(this, FinDia.class);
						startActivity(intent8);
					}

					break;

				case 9:  // Utilerias
					showInvMenuUtils();
					break;

				case 10:  // Cambio usuario
					askCambUsuario();
					break;

				case 11:	// Inicio día
					Intent intent11 = new Intent(this, InicioDia.class);
					startActivity(intent11);
					break;

			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	//endregion

	//region Reimpresion
	
	public void showPrintMenuTodo() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Pedido","Recibo","Deposito","Recarga","Devoluci�n a bodega","Cierre de dia"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(0);break;
						case 2:
							menuImprDoc(1);break;
						case 3:
							menuImprDoc(2);break;
						case 4:
							menuImprDoc(4);break;
						case 5:
							menuImprDoc(5);break;
						case 6:
							menuImprDoc(99);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuVenta() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega","Cierre de dia"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
						case 3:
							menuImprDoc(4);break;
						case 4:
							menuImprDoc(5);break;
						case 5:
							menuImprDoc(99);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuVentaApr() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega","Cierre de dia","Nota de credito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresión");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(3);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
						case 3:
							menuImprDoc(4);break;
						case 4:
							menuImprDoc(5);break;
						case 5:
							menuImprDoc(99);break;
						case 6:
							menuImprDoc(6);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void showPrintMenuPreventa() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Pedido","Recibo","Deposito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.reimpresion48);
			menudlg.setTitle("Reimpresi�n");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuImprDoc(0);break;
						case 1:
							menuImprDoc(1);break;
						case 2:
							menuImprDoc(2);break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	public void menuImprDoc(int doctipo) {
		try{
			gl.tipo=doctipo;

			Intent intent = new Intent(this,Reimpresion.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
	
	//region Anulacion
	
	public void showVoidMenuTodo() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Pedido","Recibo","Deposito","Recarga","Devolución a bodega"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.anulacion48);
			menudlg.setTitle("Anulaci�n");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							gl.tipo=3;break;
						case 1:
							gl.tipo=0;break;
						case 2:
							gl.tipo=1;break;
						case 3:
							gl.tipo=2;break;
						case 4:
							gl.tipo=4;break;
						case 5:
							gl.tipo=5;break;
					}

					menuAnulDoc();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void showVoidMenuVenta() {
		try{

			final AlertDialog Dialog;
			final String[] selitems = {"Factura","Recibo","Deposito","Recarga","Devolución a bodega"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.anulacion48);
			menudlg.setTitle("Anulación");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							gl.tipo=3;break;
						case 1:
							gl.tipo=1;break;
						case 2:
							gl.tipo=2;break;
						case 3:
							gl.tipo=4;break;
						case 4:
							gl.tipo=5;break;
					}

					menuAnulDoc();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);

		}catch(Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void showVoidMenuPreventa() {

		try{

		}catch (Exception e){final AlertDialog Dialog;
			final String[] selitems = {"Pedido","Recibo","Deposito"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.anulacion48);
			menudlg.setTitle("Anulaci�n");

			menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							gl.tipo=0;break;
						case 1:
							gl.tipo=1;break;
						case 2:
							gl.tipo=2;break;
					}

					menuAnulDoc();
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuAnulDoc() {
		try{
			Intent intent = new Intent(this,Anulacion.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion
	
	//region Consultas
	
	public void showConsMenu() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Objetivos por producto","Objetivos por familia","Objetivo por ruta","Objetivo por cobro","Inventario bodega","Consulta de precios"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setTitle("Consultas");

			menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuObjProd();break;
						case 1:
							menuObjFamilia();break;
						case 2:
							menuObjRuta();break;
						case 3:
							menuObjCobro();break;
						case 4:
							menuInvBod();dialog.cancel();break;
						case 5:
							menuPrecios();dialog.cancel();break;
					}

					//dialog.cancel();
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
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjProd() {
		try{
			gl.tipo=0;

			Intent intent = new Intent(this,ObjProd.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjFamilia() {
		try{
			gl.tipo=1;

			Intent intent = new Intent(this,ObjProd.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjRuta() {
		try{
			gl.tipo=0;

			Intent intent = new Intent(this,ObjRuta.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuObjCobro() {
		try{
			gl.tipo=1;

			Intent intent = new Intent(this,ObjRuta.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuInvBod() {
		try{
			Intent intent = new Intent(this,InvBodega.class);
			startActivity(intent);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	private void menuPrecios() {
		try{
			Intent intent = new Intent(this,ConsPrecio.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	//endregion

	//region Inventario
	
	public void showInvMenuVenta() 	{

		try{
			final AlertDialog Dialog;
			int itemcnt=1,itempos=0;

			if (gl.peAceptarCarga) 	{
				itemcnt+=1;
			} else {
				if (gl.peStockItf) {
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						itemcnt+=1;
					} else {
						itemcnt+=1;
					}
				} else {
					itemcnt+=2;
				}

			}
			if (gl.peSolicInv) itemcnt++;

			final String[] selitems = new String[itemcnt];

			selitems[itempos]="Existencias";itempos++;
			if (gl.peAceptarCarga) {
				selitems[itempos]="Aceptar Inventario";itempos++;
			} else {
				if (gl.peStockItf) {
					if (gl.peModal.equalsIgnoreCase("TOL")) {
						//selitems[itempos]="Recarga manual";itempos++;
						selitems[itempos]="Devolucion a bodega";itempos++;
					} else {
						selitems[itempos]="Devolucion a bodega";itempos++;
					}
				} else {
					selitems[itempos]="Devolucion a bodega";itempos++;
					selitems[itempos]="Recarga manual";itempos++;
				}
			}

			if (gl.peSolicInv) {
				selitems[itempos]="Solicitud de inventario";itempos++;
			}

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String mt=selitems[item];

					if (mt.equalsIgnoreCase("Existencias")) menuExist();
					if (mt.equalsIgnoreCase("Devolucion a bodega")) menuDevBod();
					if (mt.equalsIgnoreCase("Recarga manual")) menuRecarga();
					if (mt.equalsIgnoreCase("Aceptar Inventario")) menuRecargaAuto();
					if (mt.equalsIgnoreCase("Solicitud de inventario")) menuSolicInv();

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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void showInvMenuPreventa() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Existencias","Devolucion a bodega"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuExist();break;
						case 1:
							menuDevBod();break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch	(Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
	public void showInvMenuPos() {

		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Existencias","Devolucion","Recarga"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.inventario48);
			menudlg.setTitle("Inventario");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					//mu.msgbox(item);

					switch (item) {
						case 0:
							menuExist();break;
						case 1:
							menuDevBod();break;
						case 2:
							menuRecarga();break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuExist() {
		try{
			gl.tipo=0;
			Intent intent = new Intent(this,Exist.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuRecarga() {

		try{
			Intent intent = new Intent(this,Recarga.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuRecargaAuto() {
		try{
			Intent intent = new Intent(this,RecargaAuto.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuDevBod() {
		try{
			if (gl.peModal.equalsIgnoreCase("TOL")) {
				startActivity(new Intent(this,DevolBodTol.class));
			} else {
				startActivity(new Intent(this,DevolBod.class));
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuSolicInv() {
		Cursor dt;
		boolean newflag=false;
		String flag,corel;
		try{
			try {

				sql="SELECT STATCOM FROM D_SOLICINV";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0) {
					dt.moveToFirst();

					flag=dt.getString(0);
					if (flag.equalsIgnoreCase("S")) {
						msgbox("Falta enviar solicitud actual y realizar cierre del día antes de crear una nueva.");return;
					}
				} else {
					newflag=true;
				}

			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			}

			if (newflag) {

				try {
					db.beginTransaction();

					sql="DELETE FROM D_SOLICINVD";
					db.execSQL(sql);

					corel=gl.ruta+"_"+mu.getCorelBase();

					ins.init("D_SOLICINV");

					ins.add("COREL",corel);
					ins.add("ANULADO","N");
					ins.add("RUTA",gl.ruta);
					ins.add("FECHA",fecha);
					ins.add("USUARIO",gl.vend);
					ins.add("REFERENCIA","");
					ins.add("STATCOM","P");

					db.execSQL(ins.sql());

					db.setTransactionSuccessful();
					db.endTransaction();
				} catch (Exception e) {
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
					db.endTransaction();msgbox("No se puede crear una nueva solicitud.\n"+e.getMessage());return;
				}

			}

			Intent intent = new Intent(this,SolicInv.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	//endregion

	//region Utilerias
	
	public void showInvMenuUtils() {
		try{
			final AlertDialog Dialog;
			final String[] selitems = {"Configuracion de impresora","Tablas","Correlativo CierreZ","Soporte"};

			menudlg = new AlertDialog.Builder(this);
			menudlg.setIcon(R.drawable.utils48);
			menudlg.setTitle("Utilerias");

			menudlg.setItems(selitems ,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					switch (item) {
						case 0:
							menuConfImpres();break;
						case 1:
							startActivity(new Intent(Menu.this,Tablas.class));break;
						case 2:
							menuCorelZ();break;
						case 3:
							startActivity(new Intent(Menu.this,Soporte.class));break;
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

			Button nbutton = Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
			nbutton.setBackgroundColor(Color.parseColor("#1A8AC6"));
			nbutton.setTextColor(Color.WHITE);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}		
	
	private void menuConfImpres() {
		try{
			Intent intent = new Intent(this,UtilPrint.class);
			startActivity(intent);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void menuCorelZ() {
		Cursor DT;
		int coract;

		try{
			try {
				sql="SELECT Corel FROM FinDia";
				DT=Con.OpenDT(sql);
				DT.moveToFirst();
				coract=DT.getInt(0);
			} catch (Exception e) {
				addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
				mu.msgbox(e.getMessage());
				coract=0;
			}

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Nuevo correlativo");
			alert.setMessage("Actual : "+coract);

			final EditText input = new EditText(this);
			alert.setView(input);

			input.setInputType(InputType.TYPE_CLASS_NUMBER );
			input.setText("");
			input.requestFocus();

			alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						String s=input.getText().toString();
						int icor=Integer.parseInt(s);

						if (icor<0) throw new Exception();
						askApplyCor(icor);
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox("Correlativo incorrecto");
						return;
					}
				}
			});

			alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}


		
	}
	
	private void askApplyCor(int ncor) {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Aplicar nuevo correlativo ?");

			final int fncor=ncor;

			dialog.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						sql="UPDATE FinDia SET Corel="+fncor;
						db.execSQL(sql);
					} catch (SQLException e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
						mu.msgbox("Error : " + e.getMessage());
					}
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

	}

	private void askCambUsuario() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle(R.string.app_name);
			dialog.setMessage("¿Cambiar usuario?");

			dialog.setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Menu.super.finish();
				}
			});

			dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//region Supervisor ayudante

	private void msgAskSupervisor1() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Este usuario es ADMINISTRADOR. ¿Está seguro de realizar la venta con este usuario?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					msgAskSupervisor2();
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskSupervisor2() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Usted no debería realizar ventas con el Rol de ADMINISTRADOR. ¿Esta 100% seguro de realizar la venta con este usuario?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Menu.this, Clientes.class));
				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//#HS_20181207 Mensaje que muestra los ayudantes y vehiculos disponibles.

	private void AyudanteVehiculo() {

		try{

			/*inputAyudanteVehiculo();

			getlistAyudante();
			getlistVehiculo();
			getAyudante();
			getVehiculo();
            */

            startActivity(new Intent(this,ayudante_vehiculo.class));

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void inputAyudanteVehiculo() {

		try{

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("AYUDANTE Y VEHÍCULO");

			final LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);


		//layout.addView(new TextView(this));
			layout.addView(lblAyudante);
			layout.addView(Ayudante);
			//layout.addView(new TextView(this));
			layout.addView(lblVehiculo);
			layout.addView(Vehiculo);

			alert.setView(layout);

			alert.setPositiveButton("Asignar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					getAyudanteVehiculo();
				}
			});

			alert.setNegativeButton("Omitir", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					gl.ayudante = "";gl.ayudanteID = "";
					gl.vehiculo = "";gl.vehiculoID= "";
					closekeyb();
				}
			});

			alert.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void getAyudante(){

		try{
			Ayudante.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

							gl.ayudanteID=listIDAyudante.get(position);
							gl.ayudante=listAyudante.get(position);

					} catch (Exception e) {
						mu.msgbox( e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	private void getVehiculo(){

		try{
			Vehiculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;

					try {
						spinlabel=(TextView)parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);

						gl.vehiculoID=listIDVehiculo.get(position);
						gl.vehiculo=listVehiculo.get(position);

					} catch (Exception e) {
						mu.msgbox( e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					return;
				}

			});
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}



	}

	//#HS_20181207 Obtiene el ayudante y vehiculo asignado a la ruta.
	private void getAyudanteVehiculo(){

		try
		{
			if(gl.ayudanteID.equals("")){
				msgAskAyudante();
			}else if(gl.vehiculoID.equals("")){
				msgAskVehiculo();
			}else{
				mu.msgbox("Ayudante y Vehículo asignado a la ruta");
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Log.d("getAyudanteVehiculo error: ", e.getMessage());
		}
	}

	//#HS_20181207 Lista los ayudantes de la ruta.
	private void getlistAyudante(){
		Cursor DT;

		try {
			sql="SELECT CODIGO,NOMBRE FROM P_VENDEDOR WHERE RUTA = '" + gl.ruta + "' AND NIVEL = 5 " +
			    "Union\n" +
					" Select '','Seleccione un ayudante' from P_VENDEDOR";

			DT=Con.OpenDT(sql);
			if(DT.getCount()>0) {

				DT.moveToFirst();

				while (!DT.isAfterLast())
				{
					listIDAyudante.add(DT.getString(0));
					listAyudante.add(DT.getString(1));
					DT.moveToNext();
				}

			}else if(DT.getCount() == 0){
				listIDVehiculo.add("");
				listVehiculo.add("No hay ayudantes disponibles");
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listAyudante);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			Ayudante.setAdapter(dataAdapter);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

	private void getlistVehiculo(){
		Cursor DT;

		try {
			sql="SELECT CODIGO, MARCA, PLACA FROM P_VEHICULO " +
			"Union \n" +
					" Select '','Seleccione un vehículo','' from P_VEHICULO";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0) {
				DT.moveToFirst();

				while (!DT.isAfterLast()) {
					listIDVehiculo.add(DT.getString(0));
					listVehiculo.add(DT.getString(1) + "-" + DT.getString(2));
					DT.moveToNext();
				}
			}else if(DT.getCount() == 0){
				listIDVehiculo.add("");
				listVehiculo.add("No hay vehiculos disponibles");
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listVehiculo);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			Vehiculo.setAdapter(dataAdapter);

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		}

	}

	private void msgAskAyudante() {
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Esta seguro de continuar sin asignar un ayudante?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//inputAyudanteVehiculo();

				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	private void msgAskVehiculo() {

		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("¿Esta seguro de continuar sin asignar un vehículo?");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//region Aux
	
	private void setPrintWidth() {
		Cursor DT;
		int prwd=32;
		
		try {
			sql="SELECT COL_IMP FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			prwd=DT.getInt(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			prwd=32;
		}
		
		gl.prw=prwd;
		
	}
	
	private void getDepTipo() {
		Cursor DT;
		
		try {
			sql="SELECT BOLETA_DEPOSITO,DEPOSITO_PARCIAL FROM P_EMPRESA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			gl.boldep=DT.getInt(0);
			gl.depparc=DT.getInt(1)==1;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			gl.boldep=0;
			gl.depparc=false;
		}
		
	}	
		
	private int getPrinterType() {
		Cursor DT;
		String prtipo;
		int prid=0;
		
		
		try {
			sql="SELECT TIPO_IMPRESORA FROM P_ARCHIVOCONF";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			
			prtipo=DT.getString(0);
				
			if (prtipo.equalsIgnoreCase("DATAMAX")) prid=1;
			if (prtipo.equalsIgnoreCase("EPSON")) prid=2;
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			prid=0;
	    }
		
		return prid;
				
	}

	//#HS_20181122_1527 Se agrego la funcion GetTipoImpresora().
	public String GetTipoImpresora() {
		Cursor DT;
		String Tipo = "";

		try{

			sql="SELECT TIPO_IMPRESORA FROM P_ARCHIVOCONF";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			if(DT.getCount()> 0)
			{
				Tipo = DT.getString(0);
				gl.tipoImpresora = Tipo;
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			msgbox("GetTipoImpresora: " + e.getMessage());
		}

		return Tipo;
	}

	//#HS_20181122_1513 Se agrego la funcion ConfImpresora()
	public void ConfImpresora(){
		try{
			if(GetTipoImpresora().equalsIgnoreCase("SIN IMPRESORA") || GetTipoImpresora().equalsIgnoreCase("BLUETOOTH")){
				msgAskImpresora();
			}
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//#HS_20181122_1517 Se agrego el mensaje de configuracion de impresora.
	private void msgAskImpresora() 	{
		try{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);

			dialog.setTitle("Road");
			dialog.setMessage("Debe configurar la impresora");

			dialog.setIcon(R.drawable.ic_quest);

			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					menuConfImpres();
				}
			});

			dialog.show();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}


	}

	//endregion

	//region Activity Events
	
	@Override
 	protected void onResume() {
		try{
			super.onResume();
			setPrintWidth();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}



	//endregion
}
