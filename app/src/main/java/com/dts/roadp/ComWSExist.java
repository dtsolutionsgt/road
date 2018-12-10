package com.dts.roadp;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ComWSExist extends PBase {

	private TextView lblInfo,lblParam;
	private ProgressBar barInfo;
	private EditText txtRuta,txtWS,txtEmp;
	
	private int isbusy,reccnt;
	private String ruta;
	
	private SQLiteDatabase dbT;
	private BaseDatos ConT;
	private BaseDatos.Insert insT;

	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayList<String> results=new ArrayList<String>();


	// Web Service

	public AsyncCallRec wsRtask;

	private static String sstr,fstr,fprog,ferr,idbg,dbg;
	private int scon;
	private String gEmpresa;
	
	private final String NAMESPACE ="http://tempuri.org/";
	private String METHOD_NAME,URL;

	private clsDataBuilder dbld;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_wsexist);
		
		super.InitBase();
		
		System.setProperty("line.separator","\r\n");
			
		lblInfo= (TextView) findViewById(R.id.lblETipo);
		lblParam= (TextView) findViewById(R.id.lblProd);
		barInfo= (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta= (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
		txtWS= (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
		txtEmp= (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);	
		
		isbusy=0;
		
		lblInfo.setText("");lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);
		
		ruta=gl.ruta;
	    gEmpresa=gl.emp;
			
		getWSURL();
			
		Handler mtimer = new Handler();	
		Runnable mrunner=new Runnable() {
	        @Override
	        public void run() {
	        	runRecep();
		    }
	    };
		mtimer.postDelayed(mrunner,1000); 	
		
	}
	

	// Events
	
	public void askRec(View view)
	{

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setTitle("Recepcion");
		dialog.setMessage("Recibir datos nuevos ?");
					
		dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	runRecep();
		    }
		});
		
		dialog.setNegativeButton("Cancelar", null);
		
		dialog.show();
			
	}	
	
	
	// Main
	
	private void runRecep() {
			
		if (isbusy==1) return;
		
		if (!setComParams()) return;
		
		isbusy=1;
			
		barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
		lblInfo.setText("Conectando ...");
			
		wsRtask = new AsyncCallRec();
		wsRtask.execute();
			
	}
		
	
	// Web Service Methods
	
	public int fillTable(String value,String delcmd) {
		int rc;
		String s,ss;
	
		METHOD_NAME = "getIns";
		sstr="OK";
		
		try {
			
			idbg=idbg+" filltable ";
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(value);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        idbg=idbg+" rec " +rc +"  ";
	           
	        s="";
	         
	        // if (delcmd.equalsIgnoreCase("DELETE FROM P_COBRO")) {
	        // 	idbg=idbg+" RC ="+rc+"---";
	        //}
	        
	        for (int i = 0; i < rc; i++) {
	        	
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	//s=s+str+"\n";
	        	
	        	if (i==0) {
	        		
	        		 idbg=idbg+" ret " +str +"  ";
	        		
	        		if (str.equalsIgnoreCase("#")) {
	        			listItems.add(delcmd);
	        		} else {
	        			idbg=idbg+str;
	        			sstr=str;return 0;
	        		}
	        	} else {
	        		try {
	    			    sql=str;	
	    			    listItems.add(sql);
	    			    sstr=str;
		    		} catch (Exception e) {
		    		   	sstr=e.getMessage();
		    	    }	
	        	}
	        }
	        
	        return 1;
	    } catch (Exception e) {
	    	
	    	idbg=idbg+" ERR "+e.getMessage();
	    	return 0;
	    }
	}
	
	public int OpenDTt(String sql) {
		int rc;
	
		METHOD_NAME = "OpenDT";
		
		results.clear();
		
		try {
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(sql);
			
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapObject resSoap =(SoapObject) envelope.getResponse();
	        SoapObject result = (SoapObject) envelope.bodyIn;
	        
	        rc=resSoap.getPropertyCount()-1;
	        
	        for (int i = 0; i < rc+1; i++)
	        {
	        	String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);
	        	
	        	if (i==0) {
	        		sstr=str;
	        		if (!str.equalsIgnoreCase("#")) {
	        			sstr=str;
	        			return 0;
	        		}
	        	} else {	
	        		results.add(str);
	        	}
	        }
	 
	        return 1;
	    } catch (Exception e) {
	    	sstr=e.getMessage(); 
	    }
		
		return 0;
		
	}	

	public int getTest() {
		
		METHOD_NAME = "TestWS";
		
		try {
		
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
	    
			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("Value");param.setValue("OK");
	    
			request.addProperty(param);
	 		envelope.setOutputSoapObject(request);
	    
			HttpTransportSE transport = new HttpTransportSE(URL);
			
	        transport.call(NAMESPACE+METHOD_NAME, envelope);
	        
	        SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
	        
	        sstr = response.toString()+"..";
	        	        
	        return 1;
	    } catch (Exception e) {
		    sstr=e.getMessage();          
	    }
		
		return 0;
	}	
		
	
	// WEB SERVICE - RECEPCION

	private boolean getData()
	{
		Cursor DT;
		int rc,prn,jj;
		String s,val="";
	
		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			
			val=DT.getString(0);

		} catch (Exception e) {
			val="N";
		}	
		
		if (val.equalsIgnoreCase("S"))gl.peStockItf=true; else gl.peStockItf=false;
				
		
		listItems.clear();
		idbg="";
		
		try {

			if (!AddTable("P_STOCKINV")) return false;

		} catch (Exception e) {
			return false;
		}
		
		ferr="";
		
		try {
			
			rc=listItems.size();reccnt=rc;
			if (rc==0) return true;
			
			fprog="Procesando ...";
			wsRtask.onProgressUpdate();
			
			ConT = new BaseDatos(this);
			dbT = ConT.getWritableDatabase();
		 	ConT.vDatabase =dbT;
		    insT=ConT.Ins;
			
		    prn=0;jj=0;

		    for (int i = 0; i < rc; i++)
		    {
		    	sql=listItems.get(i);
		    	dbT.execSQL(sql); 
		    	jj++;
		    	if (jj>=100) {
		    		//if (prn==0) fprog="Procesando ..."; else fprog="Procesando ... "+(prn*100)+" / "+rc;
		    		prn++;jj=0;
		    		fprog="Procesando ... "+(prn*100);
		    		wsRtask.onProgressUpdate();		    		
		    		SystemClock.sleep(50);	
		    	}
		    }

			fprog="Registrando el documento recibido de inventario en BOF...";
			wsRtask.onProgressUpdate();

			Actualiza_Documentos();

			fprog="Fin de actualización";
			wsRtask.onProgressUpdate();

		} catch (Exception e) {
			
			try {
				ConT.close();  
			} catch (Exception ee) { }
			
			sstr=e.getMessage();
			ferr=sstr+"\n"+sql;
			
			return false;
		}
		
		try {
			ConT.close();  
		} catch (Exception e) { }
		
		return true;
	}

	private String fterr;

	//#EJC20181120: Inserta los documentos que bajaron a la HH
	private boolean Actualiza_Documentos()
	{

		DateUtils DU = new DateUtils();
		int Now=du.getFechaActual();

		String ruta = txtRuta.getText().toString().trim();

		try{

			String SQL = " INSERT INTO P_DOC_ENVIADOS_HH " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK WHERE FECHA = '" +  Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCKB WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)" +
					" UNION " +
					" SELECT DISTINCT DOCUMENTO, RUTA, FECHA, 1 " +
					" FROM P_STOCK_PALLET WHERE FECHA = '" + Now + "' AND RUTA = '" + ruta + "' " +
					" AND DOCUMENTO NOT IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH)";

			dbld.clear();
			dbld.add(SQL);

			if (commitSQL()==1)
			{
				return  true;
			} else
			{
				fterr+="\n"+sstr;
				dbg=sstr;
				return  false;
			}

		}catch (Exception e)
		{
			Log.e("Error",e.getMessage());
			return  false;
		}

	}

	public int commitSQL() {
		int rc;
		String s,ss;

		METHOD_NAME = "Commit";
		sstr="OK";

		if (dbld.size()==0) return 1;

		s="";
		for (int i = 0; i < dbld.size(); i++) {
			ss=dbld.items.get(i);
			s=s+ss+"\n";
		}

		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;

			PropertyInfo param = new PropertyInfo();
			param.setType(String.class);
			param.setName("SQL");param.setValue(s);

			request.addProperty(param);
			envelope.setOutputSoapObject(request);

			HttpTransportSE transport = new HttpTransportSE(URL);
			transport.call(NAMESPACE+METHOD_NAME, envelope);

			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			s = response.toString();

			sstr = "#";
			if (s.equalsIgnoreCase("#")) return 1;

			sstr = s;
			return 0;
		} catch (Exception e) {
			sstr=e.getMessage();
		}

		return 0;
	}

	private boolean AddTable(String TN) {
		String SQL;
		
		try {
			
			fprog=TN;idbg=TN;fprog="";
			wsRtask.onProgressUpdate();
			SQL=getTableSQL(TN);
			if (fillTable(SQL,"DELETE FROM "+TN)==1) {
				idbg=idbg +SQL+"#"+"PASS OK";
				return true;	
			} else {	
				idbg=idbg +SQL+"#"+" PASS FAIL  ";
				fstr=sstr;
				return false;	
			}
		} catch (Exception e) {
			fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
			return false;
		}
	}
	
	private String getTableSQL(String TN) {
       String SQL="";
      
       if (TN.equalsIgnoreCase("P_STOCKINV")) {
         SQL = "SELECT * FROM P_STOCKINV";
         return SQL;  
       }      
         
       return SQL;
	}   
			
	
	// Web Service handling Methods
	
	public void wsExecute(){
				
		fstr="No connect";scon=0;
					
		try {
					
			if (getTest()==1) {
				scon=1;
			} else {
			}
				
			idbg=idbg + sstr;
			
			if (scon==1) {
				fstr="Sync OK";
				if (!getData()) fstr="Recepcion incompleta : "+sstr;
			} else {	
				fstr="No se puede conectar al web service : "+sstr;
			}
				
		} catch (Exception e) {
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
		}
		
	}
			
	public void wsFinished(){
			
		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);
			
		if (fstr.equalsIgnoreCase("Sync OK")) {
			lblInfo.setText(" ");
			s="Actualización completa.";
			toastcent(s);
			super.finish();
		} else {	
			lblInfo.setText(fstr);	
			mu.msgbox("Ocurrio error : \n"+fstr+" ("+reccnt+") " + ferr);
		}
		
		isbusy=0;
		//mu.msgbox("::"+dbg);
		
	}
	
	private class AsyncCallRec extends AsyncTask<String, Void, Void> {

		@Override
        protected Void doInBackground(String... params) {
			try {
				wsExecute();
			} catch (Exception e) {
			}
            
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
        	wsFinished();
        }
 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {
    		}
        }
 
        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(fprog);
    		} catch (Exception e) {
    		}
        }
 
    }	
	
		
	// Aux
	
	public void comManual(View view) {
		Intent intent = new Intent(this,ComDrop.class);
		startActivity(intent);	
	}
	
	public void getWSURL() {
		Cursor DT;
		String wsurl;
		
		txtRuta.setText(ruta);
		txtEmp.setText(gEmpresa);
		
		try {
			sql="SELECT WLFOLD,FTPFOLD FROM P_RUTA WHERE CODIGO='"+ruta+"'";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
					
			wsurl=DT.getString(0);
			
			URL=wsurl;
			txtWS.setText(URL);
		} catch (Exception e) {
			//MU.msgbox(e.getMessage());
			URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
			//txtWS.setText("");
			return;
		}
		
	}
	
	private boolean setComParams() {
		String ss;
		
		ss=txtRuta.getText().toString().trim();
		if (mu.emptystr(ss)) {
			mu.msgbox("La ruta no esta definida.");return false;
		}	
		
		ss=txtEmp.getText().toString().trim();
		if (mu.emptystr(ss)) {
			mu.msgbox("La empresa no esta definida.");return false;
		}
		gEmpresa=ss;
		
		ss=txtWS.getText().toString().trim();
		if (mu.emptystr(ss) || ss.equalsIgnoreCase("*")) {
			mu.msgbox("La direccion de Web service no esta definida.");return false;
		}
		URL=ss;
		
		return true;
	}
	
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
	   if (isbusy==0) {
		   super.onBackPressed();
	   }
	}	
		
}
