package com.dts.roadp;

import java.io.File;
import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;

public class ProdCant extends PBase {

	private EditText txtCant;
	private TextView lblDesc,lblCant,lblPrec,lblDisp,lblBU,lblTot,lblDispLbl;
	private ImageView imgProd,imgUpd,imgDel;	
	
	private Precio prc;
	
	private String prodid,prodimg,proddesc,rutatipo,um,umstock,ubas;
	private int nivel,browse=0,deccant;
	private double cant,prec,icant,idisp,umfactor;
	private boolean pexist,esdecimal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prod_cant);
		
		super.InitBase();
		
		setControls();
				
		prodid=gl.prod;
		um=gl.um;
		nivel=gl.nivel;
		rutatipo=gl.rutatipo;

		if (rutatipo.equalsIgnoreCase("V")) imgUpd.setVisibility(View.INVISIBLE);
		imgUpd.setVisibility(View.INVISIBLE);
		
		prc=new Precio(this,mu,gl.peDec);
		
		setHandlers();

		if (gl.peDecCant==0) {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
		} else {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		gl.dval=-1;
		gl.gstr="";
			
		showData();
		
	}

	
	// Events
	
	public void sendCant(View view) {
		if (setCant()<1) applyCant();
	}
	
	public void showPromo(View view){
		gl.gstr=prodid;
		
		Intent intent = new Intent(this,ListaPromo.class);
		startActivity(intent);
	}
	
	public void showPic(View view){
		gl.gstr=proddesc;
		gl.imgpath=prodimg;
		
		Intent intent = new Intent(this,PicView.class);
		startActivity(intent);
	}
	
	public void doDelete(View view) {
		msgAskDel("Borrar producto");
	}
		
	public void askExist(View view) {
				
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setTitle("Existencias bodega");
		dialog.setMessage("Actualizar existencias ?");
					
		dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	browse=1;
		    	startActivity(new Intent(ProdCant.this,ComWSExist.class));
		    }
		});
		
		dialog.setNegativeButton("Cancelar", null);
		
		dialog.show();
			
	}
	
 	private void setHandlers(){

		txtCant.addTextChangedListener(new TextWatcher() {
			 
		   	public void afterTextChanged(Editable s) {}
			 
		   	public void beforeTextChanged(CharSequence s, int start,int count, int after) { }
			 
		   	public void onTextChanged(CharSequence s, int start,int before, int count) {
		   		//setCant();
		   	}
		});	
		
		txtCant.setOnKeyListener(new OnKeyListener() {
			@Override 
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	sendCant(v);
		          return true;
		        }
		        return false;
		    }
		});
	
	}
		
	
	// Main
 	
	private void showData() {
		Cursor dt;
		
		try {
			
			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();			
			um=dt.getString(0);ubas=um;
			lblBU.setText(um);gl.ubas=ubas;
		} catch (Exception e) {
			mu.msgbox("1-"+ e.getMessage());
		}
	
		try {
							
			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,TIPO "+
				 "FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	dt=Con.OpenDT(sql);
			dt.moveToFirst();
				
			ubas=dt.getString(0);gl.ubas=ubas;			
			lblDesc.setText(dt.getString(7));
			
			//prodimg=DT.getString(6);
			prodimg=prodid;
			proddesc=dt.getString(7);
			
			if (dt.getString(7).equalsIgnoreCase("P")) pexist=true; else pexist=false;
			
		} catch (Exception e) {
		    mu.msgbox("1-"+ e.getMessage());
	    }

		try {
			sql="SELECT DECIMALES FROM P_NIVELPRECIO WHERE CODIGO="+nivel;
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			deccant=dt.getInt(0);
		} catch (Exception e) {
			deccant=0;
		}

			if (deccant==0) {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
		} else {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		try {
			imgProd.setVisibility(View.INVISIBLE);
			if (!mu.emptystr(prodimg)) {
				try {
					prodimg = Environment.getExternalStorageDirectory()+ "/SyncFold/productos/"+prodimg+".jpg";
					File file = new File(prodimg); 
					if (file.exists()) imgProd.setVisibility(View.VISIBLE);
				} catch (Exception e) {
					imgProd.setVisibility(View.INVISIBLE);
				   	mu.msgbox("2-"+ e.getMessage());	
				}
			}
		} catch (Exception e) {
			mu.msgbox("3-"+ e.getMessage());
		}

		prec=prc.precio(prodid,0,nivel,um);
		
		lblPrec.setText("Precio: "+mu.frmcur(0));

		if (prec==0) {
			hidekeyb();
			msgSinPrecio("El producto no tiene definido precio");return;
		}
		
		if (gl.sinimp) prec=prc.precsin;
		
		try {
			sql="SELECT CANT FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
           	dt=Con.OpenDT(sql);
           	
       		dt.moveToFirst();
  			icant=dt.getDouble(0);
		} catch (Exception e) {
			icant=0;
	    }	

		if (icant>0) {
			parseCant(icant);
			imgDel.setVisibility(View.VISIBLE);
		} else {
			imgDel.setVisibility(View.INVISIBLE);
		}
		
		if (rutatipo.equalsIgnoreCase("V")) {
			idisp=getDisp();	
		} else {
			idisp=getDispInv();	
		}
		
		lblPrec.setText(mu.frmcur(prec));
		lblDisp.setText(mu.frmdec(idisp));
			
		if (pexist) lblDisp.setText(""+((int) idisp)); else lblDisp.setText("");
		lblDisp.setText(mu.frmdecimal(idisp, gl.peDecImp));
		
		if (rutatipo.equalsIgnoreCase("P") && (idisp==0)) {
			lblDisp.setText("");lblDispLbl.setText("");
		}
		
		try {
			txtCant.setSelection(txtCant.getText().length());
		} catch (Exception e) {
		}	
		
	}
	
	private double getDisp()
	{

		Cursor dt;
		double disp;
		double umf1 =1;
		double umf2 =1;
			
		try {

			sql="SELECT SUM(CANT) FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			umstock=um;umfactor=1;
			disp=dt.getDouble(0);

			if (disp>0) return disp;
		} catch (Exception e){ }
		
		try {
			sql="SELECT UNIDADMEDIDA FROM P_STOCK WHERE (CODIGO='"+prodid+"')";	
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			umstock=dt.getString(0);
			
			/*
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+umstock+"')";	
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umfactor=dt.getDouble(0);
			} else {	
				umfactor=0;
				msgFactor("No existe factor de conversi�n para "+um);return 0;
			}			
			*/
			
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+um+"') AND (UNIDADMINIMA='"+ubas+"')";	
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf1=dt.getDouble(0);
			} else
				{
					umf1=1;
					//#EJC20181127: No mostrar mensaje por versión de aprofam.
				//msgFactor("No existe factor de conversión para "+um);return 0;
			}	
					
			sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+prodid+"') AND (UNIDADSUPERIOR='"+umstock+"') AND (UNIDADMINIMA='"+ubas+"')";	
			dt=Con.OpenDT(sql);
			if (dt.getCount()>0) {
				dt.moveToFirst();			
				umf2=dt.getDouble(0);
			} else {
				umf2=1;
				//#EJC20181127: No mostrar mensaje por versión de aprofam.
				//msgFactor("No existe factor de conversión para "+um);return 0;
			}
			
			umfactor=umf1/umf2;			
			
			sql="SELECT SUM(CANT) FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			disp=dt.getDouble(0);disp=disp/umfactor;
			
			return disp;
		} catch (Exception e) {
	    }	
		
		return 0;
	}
	
	private void delItem(){	
		try {
	    	db.execSQL("DELETE FROM T_VENTA WHERE PRODUCTO='"+prodid+"'");    	
	    	gl.dval=0;
	    	super.finish();
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
	}

	private void applyCant() {

		if (cant<0){
			mu.msgbox("Cantidad incorrecta");txtCant.requestFocus();return;
		}

		if (rutatipo.equalsIgnoreCase("V")) {
			if (cant>idisp) {
				mu.msgbox("Cantidad mayor que disponible.");txtCant.requestFocus();return;
			}
		}

		gl.dval=cant;
		gl.um=um;
		gl.umstock=umstock;
		gl.umfactor=umfactor;

		hidekeyb();
		super.finish();
	}
	
	// Update Disp
	
	public void updDisp(){
		
		gl.gstr=prodid;
		browse=1;
		
		Intent intent = new Intent(this,ActDisp.class);
		startActivity(intent);
	}
	
	
	// Aux
	
	private void setControls() {
		
		txtCant= (EditText) findViewById(R.id.txtMonto);
		
		lblDesc=(TextView) findViewById(R.id.lblFecha);
		lblCant=(TextView) findViewById(R.id.lblCant);
		lblPrec=(TextView) findViewById(R.id.lblPNum);
		lblDisp=(TextView) findViewById(R.id.lblDisp);		
		lblBU=(TextView) findViewById(R.id.lblBU);
		lblTot=(TextView) findViewById(R.id.textView1);lblTot.setText("");
		lblDispLbl=(TextView) findViewById(R.id.textView8);
					
		imgProd=(ImageView) findViewById(R.id.imgPFoto);	
		imgUpd=(ImageView) findViewById(R.id.imageView1);
		imgDel=(ImageView) findViewById(R.id.imageView2);
		
	}
	
	private int setCant(){
		double cu,tv,corig,cround,fruni,frcant,adcant;
		boolean ajust=false;
		
		lblTot.setText("");

		try {
			cu=Double.parseDouble(txtCant.getText().toString());		
			cant=cu;corig=cant;cround=Math.floor(cant);
			esdecimal=corig!=cround;
			cant=mu.round(cant,deccant);
		} catch (Exception e) {
			cant=-1;return -1;
		}

		// ajuste a unidades menores
		if (esdecimal) {

			if (umfactor==0) {
				//msgbox("Factor de conversion incorrecto");return -1;
			}

			fruni=1/umfactor;
			frcant=corig/fruni;
			adcant=Math.floor(frcant)+1;
			adcant=adcant*fruni;

			if (adcant!=cant) {
				ajust=true;cant=adcant;
			}

		}

		cant=mu.round(cant, gl.peDecImp);

		try {
			if (cant<0) {
				lblCant.setText("");
			} else {	
				lblCant.setText(String.valueOf(cant));
			}
			
			tv=prec*cant;
			lblTot.setText(mu.frmcur(tv));
		} catch (Exception e) {
			 mu.msgbox(e.getMessage()); 
		}

		if (ajust) {
			msgAskAjust("Cantidad ajustada a : "+mu.frmdecimal(cant, gl.peDecImp)+". ¿Aplicar?");
			return 1;
		} else {
			return 0;
		}
	}
	
	private void parseCant(double c) {
		DecimalFormat frmdec = new DecimalFormat("#.####"); 
		double ub;
			
		ub=c;
		if (ub>0) txtCant.setText(frmdec.format(ub));
			
	}
	
	private double getDispInv(){
		Cursor DT;
		
		try {
			sql="SELECT CANT FROM P_STOCKINV WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
			return DT.getDouble(0);
		} catch (Exception e) {
			return 0;
	    }		
	}
		
	private void msgSinPrecio(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
					
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	forceClose();
		    }
		});
		dialog.show();
			
	}	
	
	public void msgAskUpd(View view) {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage("Actualizar disponible ?");
				
		dialog.setIcon(R.drawable.ic_quest);
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	updDisp();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) { }
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
		    	delItem();
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) { }
		});
		
		dialog.show();
			
	}

	private void msgAskAjust(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
		dialog.setIcon(R.drawable.ic_quest);

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				applyCant();
			}
		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { }
		});

		dialog.show();

	}

	private void msgFactor(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle("Advertencia");
		dialog.setMessage("¡" + msg + "!");

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {	
				forceClose();
			}
		});

		dialog.show();

	}
	
	private void forceClose() {	
		super.finish();
	}
	
	
	// Activity Events
	
	protected void onResume() {
		
	    super.onResume();
	    
	    if (browse==1) {
	    	browse=0;
	    	lblDisp.setText(mu.frmdec(getDispInv()));
	    }
	}

}
