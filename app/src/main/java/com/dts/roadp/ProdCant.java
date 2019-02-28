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

	private EditText txtCant,txtPeso;
	private TextView lblDesc,lblCant,lblPrec,lblDisp,lblBU,lblTot,lblCodProd;
	private TextView lblDispLbl,lblPesoLbl,lblFactor,lblCantPeso,lblPesoUni;
	private ImageView imgProd,imgUpd,imgDel;	
	
	private Precio prc;
	
	private String prodid,prodimg,proddesc,rutatipo,um,umstock,ubas,upres,umfact;
	private int nivel,browse=0,deccant;
	private double cant,prec,icant,idisp,ipeso,umfactor,pesoprom,pesostock;
	private boolean pexist,esdecimal,porpeso,esbarra;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prod_cant);
		
		super.InitBase();
		
		setControls();
				
		prodid=gl.prod;lblCodProd.setText(prodid);
		um=gl.um;
		nivel=gl.nivel;
		rutatipo=gl.rutatipo;

		if (rutatipo.equalsIgnoreCase("V")) imgUpd.setVisibility(View.INVISIBLE);
		imgUpd.setVisibility(View.INVISIBLE);
		
		prc=new Precio(this,mu,gl.peDec);
		getDisp();

		setHandlers();

		if (gl.peDecCant==0) {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER );
		} else {
			txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		gl.dval=-1;
		gl.gstr="";

        paramProd();

		showData();
		
	}

	
	// Events
	
	public void sendCant(View view) {
		if (setCant(false)<1) applyCant();
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
		   		setCant(true);
		   	}
		});	
		
		txtCant.setOnKeyListener(new OnKeyListener() {
			@Override 
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			        	if (porpeso) {
			        		txtPeso.requestFocus();
							txtPeso.setSelection(0,txtPeso.length());
						} else {
			        		sendCant(v);
						}
		          return true;
		        }
		        return false;
		    }
		});

        txtPeso.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,int count, int after) { }

            public void onTextChanged(CharSequence s, int start,int before, int count) {
                setPrecio();
            }
        });


        txtPeso.setOnKeyListener(new OnKeyListener() {
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
		double ippeso=0;
		
		try {
			
			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();			
			um=dt.getString(0);ubas=um;umfact=um;
			lblBU.setText(ubas);gl.ubas=ubas;upres=ubas;
		} catch (Exception e) {
			mu.msgbox("1-"+ e.getMessage());
		}
	
		try {
							
			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,TIPO,PESO_PROMEDIO "+
				 "FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	dt=Con.OpenDT(sql);
			dt.moveToFirst();
				
			ubas=dt.getString(0);gl.ubas=ubas;			
			lblDesc.setText(dt.getString(7));
			
			//prodimg=DT.getString(6);
			prodimg=prodid;
			proddesc=dt.getString(7);
			pesoprom = dt.getDouble(9);

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


		prec=prc.precio(prodid,0,nivel,um,gl.umpeso,0);

		lblPrec.setText("Precio: "+mu.frmcur(0));

		if (prec==0) {
			hidekeyb();
			msgSinPrecio("El producto no tiene definido precio");return;
		}
		
		if (gl.sinimp) prec=prc.precsin;
		
		try {
			sql="SELECT CANT,PESO FROM T_VENTA WHERE PRODUCTO='"+prodid+"'";
           	dt=Con.OpenDT(sql);
           	
       		dt.moveToFirst();
  			icant=dt.getDouble(0);
  			ippeso=dt.getDouble(1);
		} catch (Exception e) {
			icant=0;ippeso=0;
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

		idisp=mu.trunc(idisp);

		if (porpeso) {
			lblBU.setText(umstock);
			upres = umstock;

			lblPrec.setText(mu.frmcur(prec)+" x "+gl.umpeso);
		} else {
			lblPrec.setText(mu.frmcur(prec)+" x "+upres);
		}


		if (pexist) lblDisp.setText(""+((int) idisp)); else lblDisp.setText("");
		lblDisp.setText(mu.frmdecimal(idisp, gl.peDecImp)+" "+upres);
		lblFactor.setText("x "+mu.frmdecimal(umfactor, gl.peDecImp));
		lblPesoUni.setText(gl.umpeso);

		if (rutatipo.equalsIgnoreCase("P") && (idisp==0)) {
			lblDisp.setText("");lblDispLbl.setText("");
		}

		try {
			txtCant.setSelection(txtCant.getText().length());
		} catch (Exception e) {
		}

        txtPeso.setText(mu.frmdecimal(ippeso, gl.peDecImp));
	}
	
	private double getDisp() {
		Cursor dt;
		double disp;
		double umf1 =1;
		double umf2 =1;
			
		try {

			sql="SELECT SUM(CANT),SUM(PESO) FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+um+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			umstock=um;umfactor=1;
			disp=dt.getDouble(0);
			ipeso=dt.getDouble(1);
			pesostock=ipeso/disp;
			if (disp>0) return disp;
		} catch (Exception e){ }
		
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
			} else 	{
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
			
			sql="SELECT SUM(CANT),SUM(PESO) FROM P_STOCK WHERE (CODIGO='"+prodid+"') AND (UNIDADMEDIDA='"+umstock+"')";
			dt=Con.OpenDT(sql);
			dt.moveToFirst();
			
			disp=dt.getDouble(0);
            if (!porpeso) disp=disp/umfactor;
            ipeso=dt.getDouble(1);
			pesostock = ipeso/disp;
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
		double ppeso;

		if (cant<0){
			mu.msgbox("Cantidad incorrecta");txtCant.requestFocus();return;
		}

		if (rutatipo.equalsIgnoreCase("V")) {
			if (cant>idisp) {
				mu.msgbox("Cantidad mayor que disponible.");txtCant.requestFocus();return;
			}
		}

		if (porpeso) {

			String spp=txtPeso.getText().toString();

			try {
				ppeso=Double.parseDouble(spp);
				if (ppeso<=0) throw new Exception();
			} catch (Exception e) {
				mu.msgbox("Peso incorrect");txtPeso.requestFocus();return;
			}
		} else {
			if (pesoprom==0) ppeso = pesostock*cant;else ppeso=pesoprom*cant;
		}

		gl.dval=cant;
		gl.dpeso=ppeso;
		gl.um=upres;
		gl.umpres=upres;
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
		txtPeso= (EditText) findViewById(R.id.txtPeso);txtPeso.setVisibility(View.INVISIBLE);
		lblDesc=(TextView) findViewById(R.id.lblFecha);
		lblCant=(TextView) findViewById(R.id.lblCant);
		lblPrec=(TextView) findViewById(R.id.lblPNum);
		lblDisp=(TextView) findViewById(R.id.lblDisp);		
		lblBU=(TextView) findViewById(R.id.lblBU);
		lblTot=(TextView) findViewById(R.id.textView1);lblTot.setText("");
		lblDispLbl=(TextView) findViewById(R.id.textView8);
		lblPesoUni=(TextView) findViewById(R.id.textView25);lblPesoUni.setVisibility(View.INVISIBLE);
        lblPesoLbl=(TextView) findViewById(R.id.textView24); lblPesoLbl.setVisibility(View.INVISIBLE);
		lblFactor=(TextView) findViewById(R.id.textView22);lblFactor.setVisibility(View.INVISIBLE);
		lblCantPeso=(TextView) findViewById(R.id.textView21);lblCantPeso.setText("");lblCantPeso.setVisibility(View.INVISIBLE);
		lblCodProd=(TextView) findViewById(R.id.txtRoadTit);
		imgProd=(ImageView) findViewById(R.id.imgPFoto);
		imgUpd=(ImageView) findViewById(R.id.imageView1);
		imgDel=(ImageView) findViewById(R.id.imageView2);
		
	}
	
	private int setCant(boolean mode){
		double cu,tv,corig,cround,fruni,frcant,adcant,vpeso,opeso;
		boolean ajust=false;
		
		lblTot.setText("***");
		if (mode) txtPeso.setText("0");

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
		if (porpeso) {
			prec=prc.precio(prodid,0,nivel,um,gl.umpeso,umfactor*cant);
		} else {
			prec=prc.precio(prodid,0,nivel,um,gl.umpeso,0);
		}

		try {
			if (cant<0)	lblCant.setText(""); else lblCant.setText(String.valueOf(cant));
			if (porpeso) {
                tv=prec*cant;
            } else {
                tv=prec*cant;
            }

		} catch (Exception e) {
			tv=0;mu.msgbox(e.getMessage());
		}

        lblTot.setText(mu.frmcur(tv));

		opeso=umfactor*cant;
		try {
			tv=umfactor*cant;
			lblCantPeso.setText(mu.frmdecimal(tv,gl.peDecImp)+" "+gl.umpeso);
		} catch (Exception e) {
			lblCantPeso.setText("");
			mu.msgbox(e.getMessage());
		}

		if (mode) txtPeso.setText(mu.frmdecimal(cant*umfactor, gl.peDecImp));

		try {
			if (mu.emptystr(txtPeso.getText().toString())) return 2;
			vpeso=Double.parseDouble(txtPeso.getText().toString());
		} catch (Exception e) {
			mu.msgbox("Peso incorrecto");return 2;
		}

		if (porpeso) {
			if (!checkLimits(vpeso,opeso)) return 2;
		}

		if (ajust) {
			msgAskAjust("Cantidad ajustada a : "+mu.frmdecimal(cant, gl.peDecImp)+". ¿Aplicar?");
			return 1;
		} else {
			return 0;
		}
	}

	private boolean checkLimits(double vpeso,double opeso) {

		Cursor dt;
		double pmin,pmax;
		String ss;

		try {

			sql="SELECT PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE PRODUCTO='"+prodid+"'";
			dt=Con.OpenDT(sql);

			if (dt.getCount() == 0) {
				msgbox("No está definido rango de repesaje para el producto, no se podrá modificar el peso");
				//#EJC20190226: Si no está definido repesaje no se puede modificar el peso según observación de Carolina se debe dejar vender.
				txtPeso.setEnabled(false);
				return true;
			}

			dt.moveToFirst();

			pmin = opeso - dt.getDouble(0) * opeso / 100;
			pmax = opeso + dt.getDouble(1) * opeso / 100;

			if (vpeso<pmin) {
				ss="El repesaje ("+mu.frmdecimal(vpeso, gl.peDecImp)+") está por debajo de los percentajes permitidos," +
						" minimo : "+mu.frmdecimal(pmin, gl.peDecImp)+", no se puede aplicar.";
				msgbox(ss);return false;
			}

			if (vpeso>pmax) {
				ss="El repesaje ("+mu.frmdecimal(vpeso, gl.peDecImp)+") está por debajo de los percentajes permitidos," +
						" máximo : "+mu.frmdecimal(pmax, gl.peDecImp)+", no se puede aplicar.";
				msgbox(ss);return false;
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		return true;
	}

	private void setPrecio() {
	    double cu,tv,corig,cround,fruni,frcant,adcant,ppeso;

        try {
            ppeso=Double.parseDouble(txtPeso.getText().toString());
        } catch (Exception e) {
            lblTot.setText("***");
            if (!mu.emptystr(txtPeso.getText().toString())) mu.msgbox("Peso incorrecto");
            return;
        }

        prec=prc.precio(prodid,0,nivel,um,gl.umpeso,ppeso);

        try {
            tv=prec*ppeso;
        } catch (Exception e) {
            tv=0;mu.msgbox(e.getMessage());
        }

        lblTot.setText(mu.frmcur(tv));

        try {
            tv=umfactor*cant;
            lblCantPeso.setText(mu.frmdecimal(tv,gl.peDecImp)+" "+gl.umpeso);
        } catch (Exception e) {
            lblCantPeso.setText("");
            mu.msgbox(e.getMessage());
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

    private void paramProd() {
        try {
            AppMethods app = new AppMethods(this, gl, Con, db);

            porpeso=app.ventaPeso(prodid);
            esbarra=app.prodBarra(prodid);
        } catch (Exception e) {
            porpeso=false;esbarra=false;
            msgbox(e.getMessage());
        }

        if (porpeso) {
            txtPeso.setVisibility(View.VISIBLE);
            lblPesoLbl.setVisibility(View.VISIBLE);
            lblFactor.setVisibility(View.VISIBLE);
			lblCantPeso.setVisibility(View.VISIBLE);
        }
    }

    private void forceClose() {
        super.finish();
    }


    // Msg

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

	
	// Activity Events
	
	protected void onResume() {
		
	    super.onResume();
	    
	    if (browse==1) {
	    	browse=0;
	    	lblDisp.setText(mu.frmdec(getDispInv()));
	    }
	}

}
