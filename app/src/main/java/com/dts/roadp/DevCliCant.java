package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class DevCliCant extends PBase {
	
	private EditText txtCant,lblPrec,txtLote,txtkgs,txtPrecio;
	private RelativeLayout rlCant;
	private TextView lblDesc,lblBU,lblPrecVenta;
	private Spinner spin,cmbum;
	private CheckBox chkTieneLote;

	private AppMethods app;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();

	private ArrayList<Double> cmbumfact= new ArrayList<Double>();
	private ArrayList<String> cmbumlist = new ArrayList<String>();
	
	private String prodid,estado,razon,devrazon,raz;
	private double cant,icant,factor=0.0,precioventa=0.0,pesoprom=0.0,clcpeso=0.0;
	private  String um="", ummin="",umcambiar="";
	private Precio prc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_cli_cant);
		
		super.InitBase();
		addlog("DevCliCant",""+du.getActDateTime(),gl.vend);
		
		setControls();
		
		prodid=gl.prod;
		estado=gl.devtipo;
		raz=gl.devrazon;
		gl.tienelote = 0;

		prc=new Precio(this,mu,gl.peDec);

		gl.dval=-1;
		
		showkeyb();

		app = new AppMethods(this, gl, Con, db);

		gl.dvporpeso = prodPorPeso(prodid);

		fillSpinner();
		fillcmbUM();

		devrazon=gl.devrazon;
		razon=devrazon;

		ummin = getUMminima();

		showData();

		setHandlers();

        txtkgs.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		txtCant.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		txtPrecio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		//Limita cantidad de decimales para los EditText.
        txtPrecio.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(gl.peDec)});
       //txtCant.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(gl.peDec)});
        txtkgs.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(gl.peDec)});

	}

	// Events
	
	public void sendCant(View view) {

		try{

			setCant();

			if (cant<0){
				mu.msgbox("Cantidad incorrecta");return;
			}

			if (estado.equalsIgnoreCase("M")) {
				if (razon.equalsIgnoreCase("0")){
					mu.msgbox("Debe definir una razón de devolución.");return;
				}
			}

			gl.dval=cant;
			gl.devrazon=razon;

			if(txtkgs.getText().toString().trim().equalsIgnoreCase("")){
				gl.dvpeso = 0.0;
			}else{
				gl.dvpeso = Double.parseDouble(txtkgs.getText().toString());
			}

			gl.dvumpeso = gl.umpeso;
			gl.dvumstock = um;
			gl.dvumventa = umcambiar;

			if(txtLote.getText().toString().trim().equalsIgnoreCase("")){
				mu.msgbox("Lote no puede ser vacío, por favor ingrese un lote.");return;
			}else {
				gl.dvlote =txtLote.getText().toString();
			}

			if(txtPrecio.getText().toString().trim().equalsIgnoreCase("")){
				gl.dvprec = 0.0;
				gl.dvpreclista = 0.0;
			}else{
				gl.dvprec =Double.parseDouble(txtPrecio.getText().toString());
				gl.dvpreclista = gl.dvprec;
			}

			gl.dvfactor = factor;

			if(gl.dvporpeso==true){
				gl.dvtotal = gl.dvpeso*gl.dvprec;
			}else{
				gl.dvtotal = cant*gl.dvprec;
			}

			if (chkTieneLote.isChecked()==true){
				gl.tienelote = 0;
			}else{
				gl.tienelote = 1;
			}

			//hidekeyb();
			super.finish();
			
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

			
	}
	
	// Main
	
	private void setHandlers(){
		
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	TextView spinlabel;
		    		
		    	try {
		    		spinlabel=(TextView)parentView.getChildAt(0);
			    	spinlabel.setTextColor(Color.BLACK);
			    	spinlabel.setPadding(5, 0, 0, 0);
			    	spinlabel.setTextSize(18);

			    	razon=spincode.get(position);

		    	} catch (Exception e) {
		    		addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			   	    mu.msgbox( e.getMessage());
		        }
		
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }

		});

		cmbum.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				TextView cmblabel;

				try{

					cmblabel=(TextView)parent.getChildAt(0);
					cmblabel.setTextColor(Color.BLACK);
					cmblabel.setPadding(5, 0, 0, 0);
					cmblabel.setTextSize(18);

					factor=cmbumfact.get(position);
					umcambiar = cmbumlist.get(position);

                    clcpeso=pesoprom*factor;

                    if (gl.dvporpeso==false){
						lblPrec.setText(String.valueOf(mu.round(getPrecio(),gl.peDec)));
                        txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
                    }else{
                        lblPrec.setText(String.valueOf(precioventa));
                        txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
                    }

				}catch (Exception e){
					addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
					mu.msgbox( e.getMessage());
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		chkTieneLote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if (chkTieneLote.isChecked()==false){
					txtLote.setEnabled(true);
				}else{
                    txtLote.setText(gl.lotedf);
					txtLote.setEnabled(false);
				}
			}
		});

		txtCant.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if ((keyCode == KeyEvent.KEYCODE_ENTER)) {

					if (gl.dvporpeso==false){
						if (clcpeso!=0){
							txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
						}else{
							txtkgs.setText(String.valueOf(mu.round(pesoprom*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
						}

					}else{
						txtkgs.setText(String.valueOf(mu.round(clcpeso*Double.parseDouble(txtCant.getText().toString()),gl.peDec)));
					}

					return true;
				}
				return false;
			}
		});

		
	}
	
	private void showData() {
		Cursor DT;
		String ubas;
		int ex=0;
		
		try {

			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA,PESO_PROMEDIO "+
				 " FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);

           	if (DT.getCount()>0){
				DT.moveToFirst();
				ubas=DT.getString(0);
				pesoprom = DT.getDouble(8);
				//lblBU.setText(ubas);
				gl.ubas=ubas;
				lblDesc.setText(DT.getString(7));
			}

		/*precioventa = prc.precio(prodid,1,gl.nivel,um,gl.umpeso,0);
		if (prc.existePrecioEspecial(prodid,1,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
			if (prc.precioespecial>0) precioventa=prc.precioespecial;
		}*/


		if (prodPorPeso(prodid)) {
			precioventa = prc.precio(prodid, 1, gl.nivel, um, gl.umpeso, gl.dpeso);
			if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,gl.dpeso)) {
				if (prc.precioespecial>0) precioventa=prc.precioespecial;
			}
		} else {
			precioventa = prc.precio(prodid, 1, gl.nivel, um, gl.umpeso, 0);
			if (prc.existePrecioEspecial(prodid,cant,gl.cliente,gl.clitipo,um,gl.umpeso,0)) {
				if (prc.precioespecial>0) precioventa=prc.precioespecial;
			}
		}

		precioventa = mu.round(precioventa,gl.peDec);

			sql="SELECT CANT,PESO,PRECIO,UMVENTA,LOTE,TIENE_LOTE FROM T_CxCD WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);

           	if (DT.getCount()>0){

				DT.moveToFirst();
				icant=DT.getDouble(0);
				razon=raz;
				umcambiar = DT.getString(3);
				txtPrecio.setText(String.valueOf(DT.getDouble(2)));
                txtkgs.setText(String.valueOf(DT.getDouble(1)));
				gl.tienelote = DT.getInt(5);
				txtLote.setText(DT.getString(4));

				if(gl.tienelote==1){
					chkTieneLote.setChecked(false);
					txtLote.setEnabled(true);
				}else{
					chkTieneLote.setChecked(true);
					txtLote.setEnabled(false);
				}

				setSpinVal(razon);
				setComboValor(umcambiar);

				if (icant>0) parseCant(icant);

				lblPrecVenta.setText(umcambiar);

			}else{

				setSpinVal(devrazon);
				setComboValor(gl.ubas);

				lblPrecVenta.setText(um);
				txtLote.setEnabled(false);

				if (gl.dvporpeso==true){
					lblPrec.setText(String.valueOf(precioventa));
				}

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			icant=0;
	    }

	    if (estado.equals("B")){
	        spin.setEnabled(false);
        }

	}
	
	private void setCant(){
		try {
			cant=Double.parseDouble(txtCant.getText().toString());
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cant=-1; 
		}
	}
	
	private void parseCant(double c) {
		try{
			DecimalFormat frmdec = new DecimalFormat("#.####");
			double ub;

			ub=c;
			if (ub>0) txtCant.setText(frmdec.format(ub));
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	// Aux
	
	private void fillSpinner(){
		Cursor DT;
		String icode,iname;
			
		spincode.add("0");
		spinlist.add("Seleccione una razón ....");
		  
		try {
				
			sql="SELECT CODIGO,DESCRIPCION FROM P_CODDEV WHERE ESTADO='"+estado+"'  ORDER BY DESCRIPCION";
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
			
		spin.setAdapter(dataAdapter);
			
		try {
			spin.setSelection(0);
		} catch (Exception e) {

			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			spin.setSelection(0);
	    }
		
	}

	private void fillcmbUM(){
		Cursor DT;
		String ifactor,iname;

		cmbumfact.add(0.0);
		cmbumlist.add("Seleccione UM....");

		getUMCliente();

		try {


				sql="SELECT UNIDADSUPERIOR,FACTORCONVERSION FROM P_FACTORCONV WHERE PRODUCTO='"+prodid+"' AND UNIDADSUPERIOR<>'"+gl.umpeso+"'  ORDER BY UNIDADSUPERIOR";
				DT=Con.OpenDT(sql);

				if (DT.getCount()>0) {

					DT.moveToFirst();
					while (!DT.isAfterLast()) {

						iname=DT.getString(0);
						ifactor=DT.getString(1);

						cmbumfact.add(Double.parseDouble(ifactor));
						cmbumlist.add(iname);

						DT.moveToNext();
					}

					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, cmbumlist);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

					cmbum.setAdapter(dataAdapter);


				}

            if(gl.tiponcredito==1){
                txtPrecio.setEnabled(true);

            }

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cmbum.setSelection(0);
		}

	}

	private void setSpinVal(String vc){
		int pos=0;
		String s;
		
		for(int i = 0; i < spincode.size(); i++ ) {
			s=spincode.get(i);
			if (s.equalsIgnoreCase(vc)) {
				pos=i;break;
			}
		}	
		
		try {
			spin.setSelection(pos);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			spin.setSelection(0);
	    }
	}

	public void setComboValor(String um){
		int pos=0;
		String s;

		for(int i = 0; i <cmbumlist.size(); i++ ) {
			s=cmbumlist.get(i);
			if (s.equalsIgnoreCase((um==gl.umpeso?gl.ubas:um))) {
				pos=i;break;
			}
		}

		try {
			cmbum.setSelection(pos);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			cmbum.setSelection(0);
		}
	}

	private void setControls() {

		try{

			txtCant= (EditText) findViewById(R.id.txtMonto);
			rlCant= (RelativeLayout) findViewById(R.id.rlCant);
			lblPrec=(EditText) findViewById(R.id.txtPrecio);
			txtLote = (EditText) findViewById(R.id.txtLote);
			txtkgs = (EditText) findViewById(R.id.txtkgs);
			txtPrecio = (EditText) findViewById(R.id.txtPrecio);

			lblDesc=(TextView) findViewById(R.id.lblFecha);
			lblPrecVenta = (TextView)findViewById(R.id.lblPrecioVenta);
			lblBU=(TextView) findViewById(R.id.lblBU);

			spin = (Spinner) findViewById(R.id.spinner1);
			cmbum = (Spinner) findViewById(R.id.cmbUM);

			prc=new Precio(this,mu,gl.peDec);

			chkTieneLote = (CheckBox) findViewById(R.id.chkTieneLote);

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}

	private void getUMCliente(){

		Cursor dt;

		try {

			sql="SELECT UNIDADMEDIDA FROM P_PRODPRECIO WHERE (CODIGO='"+prodid+"') AND (NIVEL="+gl.nivel+")";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){

				dt.moveToFirst();
				um=dt.getString(0);

			}

		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			mu.msgbox("1-"+ e.getMessage());
		}

	}

	private String getUMminima(){
		Cursor dt;
		String umbas="";

		try{

			sql="SELECT UNIDBAS FROM P_PRODUCTO WHERE CODIGO = '" + prodid +"' AND ES_PROD_BARRA = 0";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0){

				dt.moveToFirst();
				umbas=dt.getString(0);

			}else {

				sql = " SELECT UM_SALIDA FROM P_PRODUCTO "+
				" WHERE CODIGO = '" + prodid + "' AND ES_PROD_BARRA = 1 ";
				dt=Con.OpenDT(sql);

				if (dt.getCount()>0){

					dt.moveToFirst();
					umbas=dt.getString(0);

				}
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		}

		return umbas;

	}

	private Double getPrecio(){
		Double prec=0.0,proprecio=0.0;
		Double fact1,fact2;

		try{

			if (umcambiar!=um){
				fact1 = getFactor(umcambiar);
				fact2 = getFactor(um);

				if (fact2>0)proprecio=fact1/fact2;

			} proprecio = (umcambiar==um?1:proprecio);

			prec = precioventa * proprecio;

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return	prec;
	}

	private Double getFactor(String vUM){
		Cursor DT;
		Double fact=0.0;

		try{

			sql = "SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE UNIDADSUPERIOR = '" + vUM + "' AND PRODUCTO = '"+ prodid +"' AND UNIDADMINIMA='"+ummin+"'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()>0){
				DT.moveToFirst();
				fact = Double.parseDouble(DT.getString(0));
			}

		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

		return  fact;
	}

	private boolean prodPorPeso(String prodid) {
		try {
			return app.ventaPeso(prodid);
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			return false;
		}
	}

    @Override
    public void onBackPressed() {
        try{
            super.onBackPressed();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

}
