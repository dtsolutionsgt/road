package com.dts.roadp;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class DevCliCant extends PBase {
	
	private EditText txtCant;
	private RelativeLayout rlCant;
	private TextView lblDesc,lblPrec,lblBU;
	private Spinner spin;
	
	private ArrayList<String> spincode= new ArrayList<String>();
	private ArrayList<String> spinlist = new ArrayList<String>();
	
	private String prodid,estado,razon,devrazon,raz;
	private double cant,icant;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev_cli_cant);
		
		super.InitBase();
		addlog("DevCliCant",""+du.getActDateTime(),gl.vend);
		
		setControls();
		
		prodid=((appGlobals) vApp).prod;
		estado=((appGlobals) vApp).devtipo;
		raz=((appGlobals) vApp).gstr;
	
		setHandlers();
		
		((appGlobals) vApp).dval=-1;
		
		showkeyb();
		
		fillSpinner();
		
		devrazon=((appGlobals) vApp).devrazon;
		razon=devrazon;
		setSpinVal(devrazon);
		
		showData();
		
	}

	// Events
	
	public void sendCant(View view) {

		try{
			setCant();

			if (cant<0){
				mu.msgbox("Cantidad incorrecta");return;
			}

			if (razon.equalsIgnoreCase("0")){
				mu.msgbox("Debe definir una razón de devolución.");return;
			}

			((appGlobals) vApp).dval=cant;
			((appGlobals) vApp).devrazon=razon;

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
		
	}
	
	private void showData() {
		Cursor DT;
		String ubas;
		int ex=0;
		
		try {
			sql="SELECT UNIDBAS,UNIDMED,UNIMEDFACT,UNIGRA,UNIGRAFACT,DESCCORTA,IMAGEN,DESCLARGA "+
				 "FROM P_PRODUCTO WHERE CODIGO='"+prodid+"'";
           	DT=Con.OpenDT(sql);
			DT.moveToFirst();
							  
			ubas=DT.getString(0);
			
			lblBU.setText(ubas);((appGlobals) vApp).ubas=ubas;
			lblDesc.setText(DT.getString(7));
			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
		   mu.msgbox("1-"+ e.getMessage());
	    }	
		
		lblPrec.setText("");
		
		try {
			sql="SELECT CANT FROM T_CxCD WHERE CODIGO='"+prodid+"' AND CODDEV='"+raz+"'";
           	DT=Con.OpenDT(sql);
           	
       		DT.moveToFirst();
  			icant=DT.getDouble(0);
  			razon=raz;
  			
  			setSpinVal(razon);
  			
		} catch (Exception e) {
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
			icant=0;
	    }	

		if (icant>0) parseCant(icant);
		
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
	
	private void setControls() {
		try{
			txtCant= (EditText) findViewById(R.id.txtMonto);
			rlCant= (RelativeLayout) findViewById(R.id.rlCant);

			lblDesc=(TextView) findViewById(R.id.lblFecha);
			lblPrec=(TextView) findViewById(R.id.lblPNum);

			lblBU=(TextView) findViewById(R.id.lblBU);

			spin = (Spinner) findViewById(R.id.spinner1);
		}catch (Exception e){
			addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
		}

	}	
	
}
