package com.dts.roadp;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.roadp.clsClasses.clsCD;

import java.util.ArrayList;

public class Producto extends PBase {

	private ListView listView;
	private GridView gridView;
	private EditText txtFilter;
	private Spinner spinFam;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();
	
	private ArrayList<clsCD> items;
	private ListAdaptProd adapter;
	private ListAdaptProductoGrid adaptergrid;
	private AppMethods app;
	
	private String famid,itemid,pname,prname,um,ubas;
	private int act,prodtipo;
	private double disp_und;
	private double disp_peso;
	boolean ordPorNombre;
	private boolean horizpos;
	private float scale = 1f;
	private ScaleGestureDetector detector;
	private boolean mTwoFingersTapped=false;

	private  float xBegin=0;
	private  float yBegin=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_producto);
		
		super.InitBase();
		addlog("Producto",""+du.getActDateTime(),gl.vend);
		
		listView = (ListView) findViewById(R.id.listView1);
		txtFilter = (EditText) findViewById(R.id.editText1);
		spinFam = (Spinner) findViewById(R.id.spinner1);
		gridView = (GridView) findViewById(R.id.gridPrd);

		xBegin = gridView.getScaleX();
		yBegin = gridView.getScaleY();

		detector = new ScaleGestureDetector( this,new ScaleListener(gridView,this,gl.cols));

		prodtipo=gl.prodtipo;
		gl.prodtipo=0;
		this.setTitle("Producto");
		if (prodtipo==1) this.setTitle("Producto con existencia");

        app = new AppMethods(this, gl, Con, db);

        items = new ArrayList<clsCD>();
		
		act=0;
		ordPorNombre=gl.peOrdPorNombre;

		if (gl.fotos){
			listView.setVisibility(View.INVISIBLE);
			gridView.setVisibility(View.VISIBLE);
			int ori=this.getResources().getConfiguration().orientation; // 1 - portrait , 2 - landscape
			horizpos=ori==2;

			if (horizpos) {
				gridView.setNumColumns(gl.cols);
			} else {
				gridView.setNumColumns(gl.cols);
			}
		}else{
			gridView.setVisibility(View.INVISIBLE);
		}

		fillSpinner();
		
		setHandlers();
		
		listItems();
		
		//spinFam.requestFocus();
		
	}

	public void reset (View view){
		gridView.setScaleX(xBegin);
		gridView.setScaleY(yBegin);
		detector = new ScaleGestureDetector(this, new ScaleListener(gridView,this,gl.cols));
	}

    // Events

	public void porCodigo(View view) {
		try{
			ordPorNombre=false;
			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	public void porNombre(View view) {
		try{
			ordPorNombre=true;
			listItems();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

    private void setHandlers() {

	    try{

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					try {

						Object lvObj = listView.getItemAtPosition(position);
						clsCD item = (clsCD) lvObj;

						adapter.setSelectedIndex(position);

						switch (prodtipo) {

							case  1:

								if (prodBarra(item.Cod)) {
									toastcent("Producto tipo barra, no se puede ingresar la cantidad");
									finish();return;
								}

						}

						itemid = item.Cod;
						prname = item.Desc;
						gl.um = item.um;
						gl.pprodname = prname;

						appProd();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}
				}

				;
			});

			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

					try {
						Object lvObj = listView.getItemAtPosition(position);
						clsCD item = (clsCD) lvObj;

						adapter.setSelectedIndex(position);

						if (prodBarra(item.Cod)) {
							toastcent("Producto tipo barra, no se puede ingresar la cantidad");
							finish();return true;
						}

						itemid = item.Cod;
						prname = item.Desc;
						gl.um = item.um;
						gl.pprodname = prname;

						appProd();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}
					return true;
				}
			});

			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					try {

						Object lvObj = gridView.getItemAtPosition(position);
						clsCD item = (clsCD) lvObj;

						int pos=position;
						long iid=id;

						adaptergrid.setSelectedIndex(position);

						switch (prodtipo) {

							case  1:

								if (prodBarra(item.Cod)) {
									toastcent("Producto tipo barra, no se puede ingresar la cantidad");
									finish();return;
								}

						}

						itemid = item.Cod;
						prname = item.Desc;
						gl.um = item.um;
						gl.pprodname = prname;

						appProd();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}
				}

				;
			});

			gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

					try {
						Object lvObj = gridView.getItemAtPosition(position);
						clsCD item = (clsCD) lvObj;

						adaptergrid.setSelectedIndex(position);

						if (prodBarra(item.Cod)) {
							toastcent("Producto tipo barra, no se puede ingresar la cantidad");
							finish();return true;
						}

						itemid = item.Cod;
						prname = item.Desc;
						gl.um = item.um;
						gl.pprodname = prname;

						appProd();
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
					}
					return true;
				}
			});

			gridView.setOnTouchListener(new View.OnTouchListener() {
											@Override
											public boolean onTouch(View v, MotionEvent event) {
												detector.onTouchEvent(event);
												//return  detector.onTouchEvent(event);
												gl.cols = gridView.getNumColumns();
												return true;
											}
										}

			);



			txtFilter.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {
					int tl;

					tl = txtFilter.getText().toString().length();

					if (tl == 0 || tl > 1) {
						listItems();
					}
				}
			});

			spinFam.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					TextView spinlabel;
					String scod, idposition;

					try {
						spinlabel = (TextView) parentView.getChildAt(0);
						spinlabel.setTextColor(Color.BLACK);
						spinlabel.setPadding(5, 0, 0, 0);
						spinlabel.setTextSize(18);
						spinlabel.setTypeface(spinlabel.getTypeface(), Typeface.BOLD);

						scod = spincode.get(position);
						famid = scod;

						listItems();

						spinFam.requestFocus();
						//if (act>0) {hidekeyb();}
						hidekeyb();

						act += 1;
					} catch (Exception e) {
						addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
						mu.msgbox(e.getMessage());
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

    // Main
	
	private void listItems() {
		Cursor DT;
		clsCD vItem;
		int cantidad;
		String vF,cod,name,um;

		ArrayList<clsCD> vitems = new ArrayList<clsCD>();;

		items.clear();itemid="*";//famid="0";

		vF=txtFilter.getText().toString().replace("'","");

		String sql = "";

		try {
					
			switch (prodtipo) {

				case 0: // Preventa
					sql="SELECT CODIGO,DESCCORTA,UNIDBAS,DESCLARGA FROM P_PRODUCTO WHERE 1=1 ";
					if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (LINEA='"+famid+"') ";
					if (vF.length()>0) {sql=sql+"AND ((DESCCORTA LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}

					if (ordPorNombre) sql+="ORDER BY DESCCORTA"; else sql+="ORDER BY CODIGO";
					break;
					
				case 1:  // Venta

					sql="SELECT DISTINCT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA,P_PRODPRECIO.UNIDADMEDIDA,P_PRODUCTO.DESCLARGA " +
						"FROM P_PRODUCTO INNER JOIN	P_STOCK ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO INNER JOIN " +
						"P_PRODPRECIO ON (P_STOCK.CODIGO=P_PRODPRECIO.CODIGO)  " +
						"WHERE (P_STOCK.CANT > 0) AND (P_PRODPRECIO.NIVEL = " + gl.nivel +")";
					if (!mu.emptystr(famid)){
						if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P_PRODUCTO.LINEA='"+famid+"') ";
					}
					if (vF.length()>0) sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";
					sql+="UNION ";

					sql+="SELECT DISTINCT P_PRODUCTO.CODIGO, P_PRODUCTO.DESCCORTA, P_PRODPRECIO.UNIDADMEDIDA,P_PRODUCTO.DESCLARGA " +
						"FROM P_PRODUCTO INNER JOIN	P_STOCKB ON P_STOCKB.CODIGO=P_PRODUCTO.CODIGO INNER JOIN " +
						"P_PRODPRECIO ON (P_STOCKB.CODIGO=P_PRODPRECIO.CODIGO)  " +
						"WHERE (P_STOCKB.CANT > 0) AND (P_PRODPRECIO.NIVEL = " + gl.nivel +")";

					sql+="UNION ";
					sql+="SELECT DISTINCT P_PRODUCTO.CODIGO,P_PRODUCTO.DESCCORTA,'',P_PRODUCTO.DESCLARGA  " +
							"FROM P_PRODUCTO "  +
							"WHERE (P_PRODUCTO.TIPO ='S') ";
					if (!mu.emptystr(famid)){
						if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P_PRODUCTO.LINEA='"+famid+"') ";
					}
					if (vF.length()>0) sql=sql+"AND ((P_PRODUCTO.DESCCORTA LIKE '%" + vF + "%') OR (P_PRODUCTO.CODIGO LIKE '%" + vF + "%')) ";

					if (ordPorNombre) {
						sql += "ORDER BY P_PRODUCTO.DESCCORTA";
					} else {
						sql += "ORDER BY P_PRODUCTO.CODIGO";
					}

					sql=sql+"";

					break;	
					
				case 2: // Mercadeo propio
					sql="SELECT CODIGO,DESCCORTA,'',DESCLARGA FROM P_PRODUCTO WHERE 1=1 ";
					if (!mu.emptystr(famid)){
						if (!famid.equalsIgnoreCase("0")) sql=sql+"AND (P_PRODUCTO.LINEA='"+famid+"') ";
					}
					if (vF.length()>0) {sql=sql+"AND ((DESCCORTA LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}

					if (ordPorNombre) sql+="ORDER BY DESCCORTA"; else sql+="ORDER BY CODIGO";
					break;
					
				case 3:  // Mercadeo comp
					sql="SELECT CODIGO,NOMBRE,'' FROM P_MERPRODCOMP WHERE 1=1 ";
					if (!mu.emptystr(famid)) {sql=sql+"AND (MARCA='"+famid+"') ";}
					if (vF.length()>0) {sql=sql+"AND ((NOMBRE LIKE '%" + vF + "%') OR (CODIGO LIKE '%" + vF + "%')) ";}

                    if (ordPorNombre) sql+="ORDER BY NOMBRE"; else sql+="ORDER BY CODIGO";
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
			  vItem.DesLarga = DT.getString(3);
			  //#EJC20181127: En aprof. no tienen un viene vacío, colocar por defecto un.
			  if (um.equalsIgnoreCase(""))  um="UN";

			  vItem.um=um;
			  vItem.Text="";

			  items.add(vItem);
			  vitems.add(vItem);

			  DT.moveToNext();
			}
		} catch (Exception e)
		{
		   //	mu.msgbox( e.getMessage());
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			Log.d("prods",e.getMessage());
	    }

	    items = (ArrayList<clsCD>) vitems.clone();

		if (!gl.fotos){
			adapter=new ListAdaptProd(this,vitems);
			listView.setAdapter(adapter);
		}else{
			adaptergrid=new ListAdaptProductoGrid(this, items);
			gridView.setAdapter(adaptergrid);
		}

		if (prodtipo==1) dispUmCliente();
		
	}
	
	private void appProd(){
		try{
			gl.gstr=itemid;
			super.finish();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}
	
	private void dispUmCliente() {
		String sdisp;
		try{
			for (int i = items.size()-1; i >=0; i--) {
				if (getDisp(items.get(i).Cod)) {
					sdisp=mu.frmdecimal(disp_und,gl.peDecImp)+" "+ltrim(um,6)+"  "+mu.frmdecimal(disp_peso,gl.peDecImp)+" "+ltrim(gl.umpeso,6);
					items.get(i).Text=sdisp;
				} else {
					items.remove(i);
				}
			}

			adapter.notifyDataSetChanged();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
	}
	
	private boolean getDisp(String prodid) {
		Cursor dt;
		String umstock = "";
		double umf1,umf2,umfactor;
        boolean porpeso=prodPorPeso(prodid);

        disp_und =0;
		
		try {			
			//sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			sql=" SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"') " +
			    " UNION \n" +
				" SELECT UNIDADMEDIDA FROM P_STOCKB WHERE (CODIGO='" + prodid + "') ";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();
				um=dt.getString(0);
			}

			dt.close();
			
			sql="SELECT UNIDBAS	FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();
				ubas=dt.getString(0);
			}

			dt.close();

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			return false;
		}
		
		try {
			sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
				" FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
				" FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')" +
			    " UNION \n" +
			    " SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
				" FROM P_STOCKB WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')) AS A";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {

				dt.moveToFirst();
				disp_und =dt.getDouble(0);
				disp_peso =dt.getDouble(1);
			}

			dt.close();

			if (disp_und ==0) {

				sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
					" FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
					" FROM P_STOCK WHERE (CODIGO='"+prodid+"')" +
					" UNION \n" +
					" SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
					" FROM P_STOCKB WHERE (CODIGO='"+prodid+"')) AS A";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0) {
					dt.moveToFirst();
					disp_und =dt.getDouble(0);
					disp_peso =dt.getDouble(1);
				}
			}

			dt.close();

			if (disp_und >0)	return true;

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
	    }
		
		try {
			sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"')" +
				" UNION \n" +
				" SELECT UNIDADMEDIDA FROM P_STOCKB WHERE (CODIGO='"+prodid+"')";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){
				dt.moveToFirst();

				umstock=dt.getString(0);
			}

			dt.close();

			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
				"WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf1=dt.getDouble(0);
			} else {	
				return false;
			}

			dt.close();

			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV " +
				"WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf2=dt.getDouble(0);
			} else {	
				return false;
			}

			dt.close();

			umfactor=umf1/umf2;

			sql=" SELECT  IFNULL(SUM(A.CANT),0) AS CANT, IFNULL(SUM(A.PESO),0) AS PESO " +
				" FROM(SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
				" FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')" +
				" UNION \n" +
				" SELECT IFNULL(SUM(CANT),0) AS CANT, IFNULL(SUM(PESO),0) AS PESO " +
				" FROM P_STOCKB WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')) AS A";
			dt=Con.OpenDT(sql);
			if(dt.getCount()>0) {
				dt.moveToFirst();
				disp_und =dt.getDouble(0);
				disp_peso =dt.getDouble(1);
			}

			dt.close();

			/*if (disp_und ==0) {
				sql="SELECT SUM(CANT),SUM(PESO) FROM P_STOCKB WHERE (CODIGO='"+prodid+"')";
				dt=Con.OpenDT(sql);
				if(dt.getCount()>0) {
					dt.moveToFirst();
					disp_und =dt.getDouble(0);
				}
			}

			if (!porpeso) disp_und = disp_und /umfactor; else disp_und =dt.getDouble(1);*/

			return true;
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
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
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox(e.getMessage());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		   	mu.msgbox( e.getMessage());
	    }
					
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
		spinFam.setAdapter(dataAdapter);
			
		try {
			spinFam.setSelection(0);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			spinFam.setSelection(0);
	    }
		
	}	

	public String ltrim(String ss,int sw) {
		try{
			int l=ss.length();
			if (l>sw) {
				ss=ss.substring(0,sw);
			} else {
				String frmstr="%-"+sw+"s";
				ss=String.format(frmstr,ss);
			}


		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}
		return ss;
	}

    private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

	private boolean prodBarra(String prodid) {
		try {
			return app.prodBarra(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

    public void limpiaFiltro(View view) {
		try{
			txtFilter.setText("");
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

    }


	// Activity Events
	
	protected void onResume() {
		try{
			super.onResume();
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

}
