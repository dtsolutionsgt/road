package com.dts.roadp;

import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ComWS extends PBase {
	
	private TextView lblInfo,lblParam,lblRec,lblEnv;
	private ProgressBar barInfo;
	private EditText txtRuta,txtWS,txtEmp;
	private ImageView imgRec,imgEnv;
	private RelativeLayout relExist,relPrecio,relStock;
	
	private int isbusy,fecha,lin,reccnt,ultcor,ultcor_ant;
	private String err,ruta,rutatipo,sp,docstock,ultSerie,ultSerie_ant;
	private boolean fFlag,showprogress,pendientes;
	
	private SQLiteDatabase dbT;
	private BaseDatos ConT;
	private BaseDatos.Insert insT;
	
	private ArrayList<String> listItems=new ArrayList<String>();
	private ArrayList<String> results=new ArrayList<String>();
	
	private ArrayList<clsClasses.clsEnvio> items=new ArrayList<clsClasses.clsEnvio>();
	private ListAdaptEnvio adapter;
	
	private clsDataBuilder dbld;
	private clsLicence lic;
	private clsFinDia claseFindia;
    private DateUtils DU;
	
	// Web Service -

	public AsyncCallRec wsRtask;
	public AsyncCallSend wsStask;
	public AsyncCallConfirm wsCtask;
	
	private static String sstr,fstr,fprog,finf,ferr,fterr,idbg,dbg,ftmsg;
	private int scon,running,pflag,stockflag,conflag;
	private String ftext,slsync,senv,gEmpresa,ActRuta,mac;
	private boolean rutapos,ftflag,esvacio;
	
	private final String NAMESPACE ="http://tempuri.org/";
	private String METHOD_NAME,URL;

	@Override
	protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_com_ws);
		
		super.InitBase();
		
		System.setProperty("line.separator","\r\n");
		
		dbld=new clsDataBuilder(this);
		
		lblInfo= (TextView) findViewById(R.id.lblETipo);
		lblParam= (TextView) findViewById(R.id.lblProd);
		barInfo= (ProgressBar) findViewById(R.id.progressBar2);
		txtRuta= (EditText) findViewById(R.id.txtRuta);txtRuta.setEnabled(false);
		txtWS= (EditText) findViewById(R.id.txtWS);txtWS.setEnabled(false);
		txtEmp= (EditText) findViewById(R.id.txtEmp);txtEmp.setEnabled(false);	
		lblRec= (TextView) findViewById(R.id.btnRec);
		imgRec= (ImageView) findViewById(R.id.imageView5);
		lblEnv= (TextView) findViewById(R.id.btnSend);
		imgEnv= (ImageView) findViewById(R.id.imageView6);
		relExist=(RelativeLayout) findViewById(R.id.relExist);
		relPrecio=(RelativeLayout) findViewById(R.id.relPrecio);
		relStock=(RelativeLayout) findViewById(R.id.relStock);
		
		isbusy=0;
//Its working

		lblInfo.setText("");lblParam.setText("");
		barInfo.setVisibility(View.INVISIBLE);
		
		ruta=gl.ruta;
		ActRuta=ruta;
	    gEmpresa=gl.emp;
	    rutatipo=gl.rutatipog;
	    rutapos=gl.rutapos;
		
		if (gl.tipo==0) {
			this.setTitle("Comunicación");
		} else {	
			this.setTitle("Comunicación Local");
		}
			
		getWSURL();
		
		mac=getMac();
		
		lic=new clsLicence(this);
		
		pendientes=validaPendientes();
				
		visibilidadBotones();
				
		//if (gl.autocom==1) runSend();
		
		//relExist.setVisibility(View.VISIBLE);
		
		txtRuta.setText("8001-1");
		txtEmp.setText("03");
		//txtWS.setText("http://192.168.1.69/wsAndr/wsandr.asmx");
	}

	
	// Events
	
	public void askRec(View view) {
		
		if (isbusy==1) {
			toastcent("Por favor, espere que se termine tarea actual.");return;
		}
			
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
	
	public void askSend(View view) {
		
		if (isbusy==1) {
			toastcent("Por favor, espere que se termine tarea actual.");return;
		}
			
		if (gl.contlic) {		
			if (!validaLicencia()) {
				mu.msgbox("Licencia invalida!");return;
			}
		}
		
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Envio");
		dialog.setMessage("Enviar datos ?");
					
		dialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	runSend();
		    }
		});
		
		dialog.setNegativeButton("Cancelar", null);
		
		dialog.show();
			
	}	
	
	public void askExist(View view) {
		
		if (isbusy==1) {
			toastcent("Por favor, espere que se termine tarea actual.");return;
		}
			
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setTitle("Existencias bodega");
		dialog.setMessage("Actualizar existencias ?");
					
		dialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	runExist();
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
		ultcor_ant=ultCorel();
		ultSerie_ant=ultSerie();

		barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
		lblInfo.setText("Conectando ...");

		wsRtask = new AsyncCallRec();
		wsRtask.execute();

	}
	
	private void runSend() {
		
		if (isbusy==1) {return;}
		
		if (!setComParams()) return;
		
		isbusy=1;
			
		barInfo.setVisibility(View.VISIBLE);barInfo.invalidate();
		lblInfo.setText("Conectando ...");
			
		showprogress=true;
		wsStask = new AsyncCallSend();
		wsStask.execute();
			
	}
	
	private void runExist() {
		super.finish();
		startActivity(new Intent(this,ComWSExist.class));		
	}
	
	public void writeData(View view){
		
		dbld.clear();

		dbld.insert("D_PEDIDO","WHERE 1=1");
		dbld.insert("D_PEDIDOD","WHERE 1=1");
		
		dbld.save();
		
	}
	
	private boolean validaPendientes() {
		int pend=0;
			
		sp="";
		
		pend=pend+getDocCount("SELECT SERIE,CORELATIVO FROM D_FACTURA WHERE STATCOM<>'S'","Fact: ");
		pend=pend+getDocCount("SELECT COREL FROM D_PEDIDO WHERE STATCOM<>'S'","Ped: ");
		pend=pend+getDocCount("SELECT COREL FROM D_COBRO WHERE STATCOM<>'S'","Rec: ");
		pend=pend+getDocCount("SELECT TOTAL FROM D_DEPOS WHERE STATCOM<>'S'","Dep: ");
		pend=pend+getDocCount("SELECT COREL FROM D_MOV WHERE STATCOM<>'S'","Inv : ");
			
		return pend>0;
		
	}
		
	private int getDocCount(String ss,String pps) {
		Cursor DT;
		int cnt;
		String st;
		
		try {
			sql=ss;
			DT=Con.OpenDT(sql);
			cnt=DT.getCount();
			
			if (cnt>0) {
				st=pps+" "+cnt;			
				sp=sp+st+", ";	
			}
				
			return cnt;
		} catch (Exception e) {
			mu.msgbox(sql+"\n"+e.getMessage());
			return 0;
		}		
	}
	
	
	// Licencia

	private boolean validaLicencia() {
		Cursor dt;
		String mac,lickey,idkey,binkey;
		int fval,ff,lkey;

		try {
			mac=lic.getMac();
			lkey=lic.getLicKey(mac);
			lickey=lic.encodeLicence(lkey);

			sql="SELECT IDKEY,BINKEY FROM LIC_CLIENTE WHERE ID='"+mac+"'";
			dt=Con.OpenDT(sql);
			if (dt.getCount()==0) return false;

			dt.moveToFirst();		
			idkey=dt.getString(0);
			binkey=dt.getString(1);

			if (!idkey.equalsIgnoreCase(lickey)) return false;

			ff=du.getActDate();
			fval=lic.decodeValue(binkey);
			fval=fval-lkey;

			//Toast.makeText(this,""+fval, Toast.LENGTH_SHORT).show();

			if (fval==999999) return true;				
			fval=fval*10000;

			if (fval>=ff) return true; else return false;

		} catch (Exception e) {
			mu.msgbox(e.getMessage());return false;
		}

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
	        if (delcmd.equalsIgnoreCase("DELETE FROM P_STOCK")) {
	        	if (rc==1) { 
	        		stockflag=0;//return 1;
	        	} else {
	        		stockflag=1;	
	        	}
	        }
	         
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
	        			ftmsg=ftmsg+"\n"+str;ftflag=true;
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
		if (showprogress) {
			fprog="Enviando ...";wsStask.onProgressUpdate();
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

	private boolean getData(){

	    Cursor DT;
		int rc,scomp,prn,jj;
		String s,val="";
	
		try {

			sql="SELECT VALOR FROM P_PARAMEXT WHERE ID=2";	
			DT=Con.OpenDT(sql);
			DT.moveToFirst();	
			
			val=DT.getString(0);

		} catch (Exception e) {
			val="N";
		}	
		
		if (val.equalsIgnoreCase("S")) gl.peStockItf=true; else gl.peStockItf=false;
				
		
		listItems.clear();scomp=0;
		idbg="";stockflag=0;
		
		ftmsg="";ftflag=false;
		
		try {
				
			// AdjustP_Cliente();
		    // AdjustP_Cobro()
			
			if (!AddTable("P_NIVELPRECIO")) return false;

			if (!AddTable("P_RUTA")) return false;
			if (!AddTable("P_CLIENTE")) return false;
			if (!AddTable("P_CLIRUTA")) return false;
			if (!AddTable("P_CLIDIR")) return false;
			if (!AddTable("P_PRODUCTO")) return false;
			if (!AddTable("P_FACTORCONV")) return false;
			if (!AddTable("P_LINEA")) return false;
			if (!AddTable("P_PRODPRECIO")) return false;
			if (!AddTable("P_DESCUENTO")) return false;
			if (!AddTable("P_EMPRESA")) return false;
			if (!AddTable("P_BANCO")) return false;
			if (!AddTable("P_STOCKINV")) return false;
			if (!AddTable("P_CODATEN")) return false;
			if (!AddTable("P_CODDEV")) return false;
            if (!AddTable("P_CODNOLEC")) return false;
			if (!AddTable("P_NIVELPRECIO")) return false;
			if (!AddTable("P_COREL")) return false;
			if (!AddTable("P_CORELNC")) return false;
			if (!AddTable("P_CORRELREC")) return false;
			if (!AddTable("P_CORREL_OTROS")) return false;	
			
			if (gl.peStockItf)
			{
				if (gl.peAceptarCarga)
				{
					if (!AddTable("P_STOCK_APR")) return false;

				} else {	

				    if (!AddTable("P_STOCK")) return false;
				}
			}
			
			if (!AddTable("P_COBRO")) return false;
			if (!AddTable("P_CLIGRUPO")) return false;
			if (!AddTable("P_MEDIAPAGO")) return false;
			if (!AddTable("P_BONIF")) return false;
			if (!AddTable("P_BONLIST")) return false;
			if (!AddTable("P_PRODGRUP")) return false;
			if (!AddTable("P_IMPUESTO")) return false;
			if (!AddTable("P_VENDEDOR")) return false;
			if (!AddTable("P_MUNI")) return false;
			//#HS_20181207 Agregue tabla P_VEHICULO.
			if (!AddTable("P_VEHICULO"))return false;
			
			if (!AddTable("P_REF1")) return false;
			if (!AddTable("P_REF2")) return false;
			if (!AddTable("P_REF3")) return false;

			if (!AddTable("P_ARCHIVOCONF")) return false;
			if (!AddTable("P_ENCABEZADO_REPORTESHH")) return false;

			// Objetivos
			
			if (!AddTable("O_RUTA")) return false;
			if (!AddTable("O_COBRO")) return false;
			if (!AddTable("O_PROD")) return false;
			if (!AddTable("O_LINEA")) return false;	
			
			
			// Mercadeo
			
			if (!AddTable("P_MEREQTIPO")) return false;
			if (!AddTable("P_MEREQUIPO")) return false;
			if (!AddTable("P_MERESTADO")) return false;
			if (!AddTable("P_MERPREGUNTA")) return false;
			if (!AddTable("P_MERRESP")) return false;
			if (!AddTable("P_MERMARCACOMP")) return false;
			if (!AddTable("P_MERPRODCOMP")) return false;
			
			
			// Configuracion
			if (gl.contlic) {
				if (!AddTable("LIC_CLIENTE")) return false;
			}

			if (!AddTable("P_PARAMEXT")) return false;

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
			//dbT.beginTransaction();
			ConT.vDatabase =dbT;
		    insT=ConT.Ins;
			
		    prn=0;jj=0;

		    for (int i = 0; i < rc; i++) {

                sql = listItems.get(i);
                dbT.execSQL(sql);

                try {
                	if (i % 10==0) {
						fprog = "Procesando: " + i + " de: " + (rc-1);
						wsRtask.onProgressUpdate();
						SystemClock.sleep(20);
					}
                } catch (Exception e) {
                    Log.e("z", e.getMessage());
                }

				Log.d("DataIn",sql);

//				jj++;
//
//		    	if (jj>=100) {
//		    		//if (prn==0) fprog="Procesando ..."; else fprog="Procesando ... "+(prn*100)+" / "+rc;
//		    		prn++;jj=0;
//		    		fprog="Procesando ... "+(prn*100);
//		    		wsRtask.onProgressUpdate();
//		    		SystemClock.sleep(50);
//		    	}
		    }

			fprog = "Confirmando documento de invnetario recibido en BOF...";
			wsRtask.onProgressUpdate();

		    Actualiza_Documentos();

			fprog = "Fin de la actualización";
			wsRtask.onProgressUpdate();

			scomp=1;
			
		} catch (Exception e) {

			Log.e("Error",e.getMessage());

			try {
				ConT.close();  
			} catch (Exception ee) {
			}
			
			sstr=e.getMessage();
			ferr=sstr+"\n"+sql;
			
			return false;
		}
		
		try {
			ConT.close();  
		} catch (Exception e) { }
		
		return true;
	}

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

	private boolean AddTable(String TN) {
		String SQL;
		
		try {
			
			fprog=TN;idbg=TN;
			wsRtask.onProgressUpdate();
			SQL=getTableSQL(TN);

			if (fillTable(SQL,"DELETE FROM "+TN)==1) {
				if (TN.equalsIgnoreCase("P_STOCK")) dbg=dbg+" ok ";
				idbg=idbg +SQL+"#"+"PASS OK";
				return true;
			} else {	
				if (TN.equalsIgnoreCase("P_STOCK")) dbg=dbg+" fail "+sstr;
				idbg=idbg +SQL+"#"+" PASS FAIL  ";
				fstr="Tab:"+TN+" "+sstr;
				return false;	
			}

		} catch (Exception e) {
			fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
			return false;
		}
	}
	
	private String getTableSQL(String TN) {
       String SQL="";
       int fi,ff;
       
       fi=du.ffecha00(du.getActDate());ff=du.ffecha24(du.getActDate());
       int ObjAno=du.getyear(du.getActDate());
       int ObjMes=du.getmonth(du.getActDate());

       if (TN.equalsIgnoreCase("P_STOCK")) { 
    	   //SQL="SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
    	   // "FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND ((dbo.AndrDate(FECHA)>="+fi+") AND (dbo.AndrDate(FECHA)<="+ff+")) ";     	   
    	 
    	   if (gl.peModal.equalsIgnoreCase("TOL")) {
    	 	   SQL="SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
                   "FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (dbo.AndrDate(FECHA)>="+fi+") " +
                   "AND (STATUS='A') AND (COREL_D_MOV='') AND (CODIGOLIQUIDACION=0) AND (ANULADO=0) ";   
    	   } else if (gl.peModal.equalsIgnoreCase("APR"))  {
    	 	   SQL="SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
            	   "FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (dbo.AndrDate(FECHA)>="+fi+") ";
     	   } else {	   
    	 	   SQL="SELECT CODIGO, CANT, CANTM, PESO, plibra, LOTE, DOCUMENTO, dbo.AndrDate(FECHA), ANULADO, CENTRO, STATUS, ENVIADO, CODIGOLIQUIDACION, COREL_D_MOV, UNIDADMEDIDA " +
                   "FROM P_STOCK WHERE RUTA='" + ActRuta + "' AND (dbo.AndrDate(FECHA)>="+fi+") ";    	   
    	   }
         	   
	       return SQL;		   
       }
       
       if (TN.equalsIgnoreCase("P_CLIRUTA")) {
    	   SQL = "SELECT RUTA,CLIENTE,SEMANA,DIA,SECUENCIA,-1 AS BANDERA FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'";
    	   return SQL;		   
       }
       
       if (TN.equalsIgnoreCase("P_CLIENTE")) {
             SQL = " SELECT CODIGO,NOMBRE,BLOQUEADO,TIPONEG,TIPO,SUBTIPO,CANAL,SUBCANAL, ";
             SQL += "NIVELPRECIO,MEDIAPAGO,LIMITECREDITO,DIACREDITO,DESCUENTO,BONIFICACION, ";
             SQL += "dbo.AndrDate(ULTVISITA),IMPSPEC,INVTIPO,INVEQUIPO,INV1,INV2,INV3, NIT, MENSAJE, ";
             SQL += "TELEFONO,DIRTIPO, DIRECCION,SUCURSAL,COORX, COORY, FIRMADIG, CODBARRA, VALIDACREDITO, " ;
             SQL += "PRECIO_ESTRATEGICO, NOMBRE_PROPIETARIO, NOMBRE_REPRESENTANTE, ";
             SQL += "BODEGA, COD_PAIS, FACT_VS_FACT, CHEQUEPOST, PERCEPCION, TIPO_CONTRIBUYENTE, ID_DESPACHO, ID_FACTURACION,MODIF_PRECIO ";
             SQL += "FROM P_CLIENTE ";
             SQL += "WHERE (CODIGO IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') )) ";
             return SQL;  
       }        
        
       if (TN.equalsIgnoreCase("P_CLIDIR")) {
            SQL = " SELECT * FROM P_CLIDIR ";
            SQL += " WHERE (P_CLIDIR.CODIGO_CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "') ))";
            return SQL;  
       }  
       /*       if (TN.equalsIgnoreCase("P_PRODUCTO")) {
           SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
           SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, DESCUENTO,BONIFICACION, ";
           SQL += "IMP1, IMP2, IMP3, VENCOMP, DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN ";
           SQL += "FROM P_PRODUCTO ";
           return SQL;  
       } 
	   */
       
       if (TN.equalsIgnoreCase("P_PRODUCTO")) {
           SQL = "SELECT CODIGO, TIPO, LINEA, SUBLINEA, EMPRESA, MARCA, CODBARRA, DESCCORTA, DESCLARGA, COSTO, ";
           SQL += "FACTORCONV, UNIDBAS, UNIDMED, UNIMEDFACT, UNIGRA, UNIGRAFACT, DESCUENTO,BONIFICACION, ";
           SQL += "IMP1, IMP2, IMP3, VENCOMP, DEVOL, OFRECER, RENTAB, DESCMAX, PESO_PROMEDIO,MODIF_PRECIO,IMAGEN ";
           SQL += "FROM P_PRODUCTO WHERE (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "')) ";
           SQL += "OR LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')) ";
           return SQL;  
       } 
         
       if (TN.equalsIgnoreCase("P_FACTORCONV")) {
           //#EJC20181112
           //SQL = "SELECT PRODUCTO,UNIDADSUPERIOR,FACTORCONVERSION,UNIDADMINIMA FROM P_FACTORCONV ";
           SQL = " SELECT * FROM P_FACTORCONV WHERE " +
           " ((PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "') " +
           " OR PRODUCTO IN (SELECT DISTINCT CODIGO FROM P_STOCKB WHERE RUTA='" + ActRuta + "')))";

           return SQL;  
        }
              
       if (TN.equalsIgnoreCase("P_LINEA")) {
          SQL = "SELECT CODIGO,MARCA,NOMBRE FROM P_LINEA ";
          SQL += "WHERE (CODIGO IN (SELECT LINEA FROM P_LINEARUTA WHERE (RUTA='" + ActRuta + "')))";
          return SQL;  
       }
     
       if (TN.equalsIgnoreCase("P_PRODPRECIO")) {

    	   SQL = "SELECT CODIGO,NIVEL,PRECIO,UNIDADMEDIDA FROM P_PRODPRECIO ";
    	   SQL += " WHERE ( (CODIGO IN ( SELECT CODIGO FROM P_PRODUCTO WHERE (LINEA IN (SELECT LINEA FROM P_LINEARUTA WHERE RUTA='" + ActRuta + "')) ) ) ";
    	   SQL += " OR  (CODIGO IN (SELECT DISTINCT CODIGO FROM P_STOCK WHERE RUTA='" + ActRuta + "')) ) ";
    	   SQL += " AND (NIVEL IN (SELECT DISTINCT NIVELPRECIO FROM P_CLIENTE WHERE CODIGO IN (SELECT DISTINCT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'))) ";
    	   return SQL;  
       }
                              
       if (TN.equalsIgnoreCase("P_DESCUENTO")) {
          SQL = "SELECT  CLIENTE,CTIPO,PRODUCTO,PTIPO,TIPORUTA,RANGOINI,RANGOFIN,DESCTIPO,VALOR,GLOBDESC,PORCANT,dbo.AndrDateIni(FECHAINI),dbo.AndrDateFin(FECHAFIN),CODDESC,NOMBRE ";
          SQL += "FROM P_DESCUENTO WHERE DATEDIFF(D, FECHAINI,GETDATE()) >=0 AND DATEDIFF(D,GETDATE(), FECHAFIN) >=0";
          return SQL;  
       }

       if (TN.equalsIgnoreCase("P_EMPRESA")) {
         SQL = "SELECT * FROM P_EMPRESA WHERE EMPRESA = '" + gEmpresa + "'";
         return SQL;  
       }
       
       if (TN.equalsIgnoreCase("P_RUTA")) {
           SQL = "SELECT * FROM P_RUTA WHERE CODIGO = '" + ActRuta + "'";
           return SQL;  
         }

       if (TN.equalsIgnoreCase("P_BANCO")) {
         SQL = "SELECT * FROM P_BANCO WHERE EMPRESA = '" + gEmpresa + "'";
         return SQL;  
       }
     
       if (TN.equalsIgnoreCase("P_STOCKINV")) {
         SQL = "SELECT * FROM P_STOCKINV";
         return SQL;  
       }
       
       if (TN.equalsIgnoreCase("P_CODATEN")) {
         SQL = "SELECT * FROM P_CODATEN";
         return SQL;  
       }

        if (TN.equalsIgnoreCase("P_CODNOLEC")) {
            SQL = "SELECT * FROM P_CODNOLEC";
            return SQL;
        }



       if (TN.equalsIgnoreCase("P_CODDEV")) {
           SQL = "SELECT * FROM P_CODDEV";
           return SQL;  
       }

       if (TN.equalsIgnoreCase("P_NIVELPRECIO")) {
         SQL = "SELECT * FROM P_NIVELPRECIO ";
         return SQL;  
       }

       if (TN.equalsIgnoreCase("P_CLIGRUPO")) {
           SQL = "SELECT CODIGO,CLIENTE FROM P_CLIGRUPO WHERE (CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "'))";
           return SQL;  
       }
       
       if (TN.equalsIgnoreCase("P_STOCK_APR")) { 
    	   SQL="SELECT CODIGO, CANT, PESO " +
    	   	   "FROM P_STOCK_APR WHERE RUTA='" + ActRuta + "' ";
    	   //SQL = "SELECT CODIGO,CANT,0 AS CANTM,PESO FROM P_STOCK WHERE RUTA='" + ActRuta + "'";
    	   //idbg=SQL;
      	   return SQL;		   
       }

       //#HS_20181212 Agregue campos ID_TRANSACCION, REFERENCIA, ASIGNACION.
       if (TN.equalsIgnoreCase("P_COBRO")) {
    	   SQL = "SELECT  DOCUMENTO, EMPRESA, RUTA, CLIENTE, TIPODOC, VALORORIG, SALDO, CANCELADO, dbo.AndrDate(FECHAEMIT),dbo.AndrDate(FECHAV),'' AS CONTRASENA, ID_TRANSACCION, REFERENCIA, ASIGNACION ";
    	   SQL += "FROM P_COBRO WHERE (RUTA='" + ActRuta + "') AND CLIENTE IN (SELECT CLIENTE FROM P_CLIRUTA WHERE (RUTA='" + ActRuta + "')) ";
    	   //idbg=SQL;
    	   return SQL;
       }
       
       if (TN.equalsIgnoreCase("P_COREL")) {
    	   SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_COREL WHERE RUTA='" + ActRuta + "'";
    	   return SQL;		   
       }       
       
       if (TN.equalsIgnoreCase("P_CORELNC")) {
    	   SQL = "SELECT RESOL,SERIE,CORELINI,CORELFIN,CORELULT,dbo.AndrDate(FECHARES),RUTA,dbo.AndrDate(FECHAVIG),RESGUARDO,VALOR1 FROM P_CORELNC WHERE RUTA='" + ActRuta + "'";
    	   return SQL;		   
       }  
       
       if (TN.equalsIgnoreCase("P_CORRELREC")) {
    	   SQL = "SELECT RUTA,SERIE,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORRELREC WHERE RUTA='" + ActRuta + "'";
    	   return SQL;		   
       } 
       
       if (TN.equalsIgnoreCase("P_CORREL_OTROS")) {
    	   SQL = "SELECT RUTA,SERIE,TIPO,INICIAL,FINAL,ACTUAL,ENVIADO FROM P_CORREL_OTROS WHERE RUTA='" + ActRuta + "'";
    	   return SQL;		   
       } 
       
       //
    
       if (TN.equalsIgnoreCase("P_MEDIAPAGO")) {
    	   SQL = "SELECT CODIGO,NOMBRE,ACTIVO,NIVEL,PORCOBRO FROM P_MEDIAPAGO WHERE ACTIVO='S'";
    	   return SQL;		   
       } 
        
       if (TN.equalsIgnoreCase("P_ARCHIVOCONF")) {
    	   SQL = "SELECT RUTA,TIPO_HH,IDIOMA,TIPO_IMPRESORA,SERIAL_HH,MODIF_PESO,PUERTO_IMPRESION,LBS_O_KGS,NOTA_CREDITO FROM P_ARCHIVOCONF WHERE (RUTA='" + ActRuta + "')";
    	   return SQL;		   
       }        
       
       if (TN.equalsIgnoreCase("P_ENCABEZADO_REPORTESHH")) {
    	   SQL = "SELECT CODIGO,TEXTO,SUCURSAL FROM P_ENCABEZADO_REPORTESHH";
    	   return SQL;		   
       }     
          
       
       if (TN.equalsIgnoreCase("P_BONIF")) {
    	   SQL = "SELECT  CLIENTE, CTIPO, PRODUCTO, PTIPO, TIPORUTA, TIPOBON, RANGOINI, RANGOFIN, TIPOLISTA, TIPOCANT, VALOR," +
    	   		 "LISTA, CANTEXACT, GLOBBON, PORCANT, dbo.AndrDate(FECHAINI), dbo.AndrDate(FECHAFIN), CODDESC, NOMBRE, EMP " +
    	   		 "FROM P_BONIF WHERE ((dbo.AndrDate(FECHAINI)<="+ff+") AND (dbo.AndrDate(FECHAFIN)>="+fi+"))";
    	   return SQL;		   
       }          
       
       if (TN.equalsIgnoreCase("P_BONLIST")) {
    	   SQL = "SELECT CODIGO,PRODUCTO,CANT,CANTMIN,NOMBRE FROM P_BONLIST";
    	   return SQL;		   
       }      
         
       if (TN.equalsIgnoreCase("P_PRODGRUP")) {
           SQL = "SELECT CODIGO,PRODUCTO,NOMBRE FROM P_PRODGRUP";
           return SQL;  
       }
        
       if (TN.equalsIgnoreCase("P_IMPUESTO")) {
           SQL = "SELECT CODIGO,VALOR FROM P_IMPUESTO";
           return SQL;  
       }      

       //#HS_20181206 Agregue Ruta.
       if (TN.equalsIgnoreCase("P_VENDEDOR")) {
     		SQL="SELECT CODIGO,NOMBRE,CLAVE,RUTA,NIVEL,NIVELPRECIO,BODEGA,SUBBODEGA,COD_VEHICULO,LIQUIDANDO,BLOQUEADO,DEVOLUCION_SAP  " +
    			"FROM P_VENDEDOR  WHERE (RUTA='"+ActRuta+"') OR (NIVEL<3) ";
           return SQL;  
       }

		//#HS_20181207 Agregue campos de P_VEHICULO.
       if(TN.equalsIgnoreCase("P_VEHICULO")){
		   SQL="SELECT CODIGO,MARCA,PLACA,PESO,KM_MILLAS,TIPO FROM P_VEHICULO";
		   return SQL;
	   }

       if (TN.equalsIgnoreCase("P_MUNI")) {
           SQL = "SELECT * FROM P_MUNI";
           return SQL;  
       }      
       
       if (TN.equalsIgnoreCase("P_REF1")) {
           SQL = "SELECT * FROM P_REF1";
           return SQL;  
       }      
       
       if (TN.equalsIgnoreCase("P_REF2")) {
           SQL = "SELECT * FROM P_REF2";
           return SQL;  
       }   
       
       if (TN.equalsIgnoreCase("P_REF3")) {
           SQL = "SELECT * FROM P_REF3";
           return SQL;  
       }   
       
       
       // Objetivos
     
       if (TN.equalsIgnoreCase("O_PROD")) {
         SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_PROD WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
         return SQL;  
       }
               
       if (TN.equalsIgnoreCase("O_LINEA")) {
          SQL = "SELECT RUTA, CODIGO, METAV, METAU, ACUMV, ACUMU FROM O_LINEA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno + ") AND (OBJMES=" + ObjMes + ")";
          return SQL;  
       }

       if (TN.equalsIgnoreCase("O_RUTA")) {
          SQL = "SELECT * FROM O_RUTA WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" + ObjAno+ ") AND (OBJMES=" +ObjMes + ")";
          return SQL;  
       }

       if (TN.equalsIgnoreCase("O_COBRO")) {
          SQL = "SELECT * FROM O_COBRO WHERE (RUTA='" + ActRuta + "') AND (OBJANO=" +ObjAno + ") AND (OBJMES=" + ObjMes + ")";
          return SQL;  
       }
       
       
       // Mercadeo
       
       if (TN.equalsIgnoreCase("P_MEREQTIPO")) {
    	   SQL = "SELECT * FROM P_MEREQTIPO";
    	   return SQL;  
       }

       if (TN.equalsIgnoreCase("P_MEREQUIPO")) {
    	   SQL = "SELECT * FROM P_MEREQUIPO ";
    	   SQL = SQL + "WHERE (CLIENTE IN  (SELECT CLIENTE FROM P_CLIRUTA WHERE RUTA='" + ActRuta + "' ) )";
    	   return SQL;  
       }
       
       if (TN.equalsIgnoreCase("P_MERESTADO")) {
    	   SQL = "SELECT * FROM P_MERESTADO" ;  
    	   return SQL; 
       }

       if (TN.equalsIgnoreCase("P_MERPREGUNTA")) {
    	   SQL = "SELECT * FROM P_MERPREGUNTA";
    	   return SQL;    
       }
       
       if (TN.equalsIgnoreCase("P_MERRESP")) {
    	   SQL = "SELECT * FROM P_MERRESP";
    	   return SQL;      	   
       }

       if (TN.equalsIgnoreCase("P_MERMARCACOMP")) {
    	   SQL = "SELECT * FROM P_MERMARCACOMP";
    	   return SQL;    
       }

       if (TN.equalsIgnoreCase("P_MERPRODCOMP")) {
    	   SQL = "SELECT * FROM P_MERPRODCOMP";
    	   return SQL;   
       }
       
       if (TN.equalsIgnoreCase("LIC_CLIENTE")) {
    	   SQL="SELECT * FROM LIC_CLIENTE WHERE ID='"+mac+"'";
    	   return SQL;   
       } 

       if (TN.equalsIgnoreCase("P_PARAMEXT")) {
    	   SQL="SELECT ID,Nombre,Valor FROM P_PARAMEXT WHERE ((idRuta='"+ActRuta+"') OR (ISNULL(idRuta,'')=''))";
    	   return SQL;   
       }       

       return SQL;
	}   
	 
	private void comparaCorrel() {
		Cursor DT;
		String ss;
		
		try {
			sql="SELECT VENTA FROM P_RUTA";
			DT=Con.OpenDT(sql);
			
			if (DT.getCount()==0) {
				msgbox("La ruta no existe. Por favor informe su supervisor !");
			}
			
			DT.moveToFirst();
			ss=DT.getString(0);
			if (ss.equalsIgnoreCase("T")) ss="V";
		} catch (Exception e) {
			ss="X";
		}	
		
		if (!ss.equalsIgnoreCase("V")) return;
		
		ultcor=ultCorel();
		if (ultcor==0) {
			//msgbox("No está definido correlativo de las facturas!\n Por favor informe a su supervisor.");
		}

		ultSerie=ultSerie(); //#HS_20181129_1005 Agregue ultSerie.
		if (ultcor_ant!=ultcor) {
			//#HS_20181129_1005 Agregue comparacion para las series.
			if(ultSerie_ant != ultSerie){
				msgbox("Nueva serie de faturación");
			}else if (ultcor_ant>0) {
				msgbox("El último correlativo actualizado ( "+ultcor+" ) no coincide con último emitido ( "+ultcor_ant+" )!\n Por favor infore a su supervisor.");return;
			}
		}
	}
		
	private boolean validaDatos(boolean completo) {
		Cursor dt;

		try {

			if (!rutatipo.equalsIgnoreCase("P")) {
				sql="SELECT RESOL FROM P_COREL";	
				dt=Con.OpenDT(sql);	
				if (dt.getCount()==0) {
					msgbox("No está definido correlativo de facturas");return false;
				}
			}

			sql="SELECT Codigo FROM P_CLIENTE";	
			dt=Con.OpenDT(sql);	
			if (dt.getCount()==0) {
				msgbox("Lista de clientes está vacia");return false;
			}

			sql="SELECT Ruta FROM P_CLIRUTA";	
			dt=Con.OpenDT(sql);	
			if (dt.getCount()==0) {
				msgbox("Lista de clientes por ruta está vacia");return false;
			}

			sql="SELECT Codigo FROM P_PRODUCTO";	
			dt=Con.OpenDT(sql);	
			if (dt.getCount()==0) {
				msgbox("Lista de productos está vacia");return false;
			}

			if (completo) {

				sql="SELECT Nivel FROM P_PRODPRECIO";	
				dt=Con.OpenDT(sql);	
				if (dt.getCount()==0) {
					msgbox("Lista de precios está vacia");return false;
				}

				sql="SELECT Producto FROM P_FACTORCONV";	
				dt=Con.OpenDT(sql);	
				if (dt.getCount()==0) {
					msgbox("Lista de conversiones está vacia");return false;
				}

				if (gl.peStockItf) {
					sql="SELECT Codigo FROM P_STOCK";	
					dt=Con.OpenDT(sql);	
					if (dt.getCount()==0) {
						msgbox("La carga de productos está vacia");return false;
					}		
				}

			}

		} catch (Exception e) {
		}		

		return true;
	}
	
	private void estandartInventario() {
		Cursor dt,df;
		String cod,ub,us,lote,doc,stat;
		double cant,fact;
	
		try {

			sql="SELECT P_STOCK.CODIGO,P_STOCK.UNIDADMEDIDA, P_PRODUCTO.UNIDBAS, P_STOCK.CANT,P_STOCK.LOTE,P_STOCK.DOCUMENTO,P_STOCK.STATUS  " +
					"FROM  P_STOCK INNER JOIN P_PRODUCTO ON P_STOCK.CODIGO=P_PRODUCTO.CODIGO";
			dt=Con.OpenDT(sql);
		
			if (dt.getCount()==0) return;

			dt.moveToFirst();
			while (!dt.isAfterLast()) {

				cod=dt.getString(0);
				us=dt.getString(1);
				ub=dt.getString(2);
				cant=dt.getDouble(3);
				lote = dt.getString(4);
				doc = dt.getString(5);
				stat = dt.getString(6);

				if (!ub.equalsIgnoreCase(us)) {
					
					sql="SELECT FACTORCONVERSION FROM P_FACTORCONV WHERE (PRODUCTO='"+cod+"') AND (UNIDADSUPERIOR='"+us+"') AND (UNIDADMINIMA='"+ub+"')";	
					df=Con.OpenDT(sql);
					
					if (df.getCount()>0) {
						
						df.moveToFirst();
						fact=df.getDouble(0);cant=cant*fact;
						
						sql="UPDATE P_STOCK SET CANT="+cant+",UNIDADMEDIDA='"+ub+"'  " +
							"WHERE (CODIGO='"+cod+"') AND (UNIDADMEDIDA='"+us+"') AND (LOTE='"+lote+"') AND (DOCUMENTO='"+doc+"') AND (STATUS='"+stat+"')";
						db.execSQL(sql);
					} else {
						msgbox("No existe factor conversion para el producto : "+cod);
						sql="DELETE FROM P_STOCK WHERE CODIGO='"+cod+"'";
						db.execSQL(sql);	
					}
				}

				dt.moveToNext();
			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

	}

	
	// Web Service handling Methods
	
	public void wsExecute(){
				
		running=1;fstr="No connect";scon=0;
					
		try {
					
			if (getTest()==1) {
				scon=1;
			} else {
			}
				
			idbg=idbg + sstr;
			
			if (scon==1) {
				fstr="Sync OK";
				if (!getData()) fstr="Recepcion incompleta : "+fstr;
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
		running=0;
			
		if (fstr.equalsIgnoreCase("Sync OK")) {
			lblInfo.setText(" ");
			s="Recepcion completa.";
			
			if (!esvacio) {
				if (stockflag==1) {
					s=s+"\nSe actualizó inventario.";
					estandartInventario();
				}
				mu.msgbox(s);	
				validaDatos(true);
					
				if (stockflag==1) sendConfirm();

			} else {
				isbusy=0;
				esvacio=false;
				SystemClock.sleep(100);
				if (validaDatos(false)) runRecep();
				return;
			}		
			
		} else {	
			lblInfo.setText(fstr);	
			mu.msgbox("Ocurrio error : \n"+fstr+" ("+reccnt+") " + ferr);
		}
				
		pendientes=validaPendientes();
		visibilidadBotones();
		
		isbusy=0;
		comparaCorrel();
		
		paramsExtra();
		//mu.msgbox("::"+dbg);
		
		if (ftflag) msgbox(ftmsg);
		
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
                synchronized (this) {
                    if (!lblInfo.getText().toString().matches(""))  lblInfo.setText(fprog);
                 }
    		} catch (Exception e) {

    		}
        }
 
    }	
	
	//#HS_20181123_1623 Agregue funcion FinDia para el commit y update de tablas.

	private boolean FinDia(){

	try {

		if(commitSQL() == 1)
		{
			db.beginTransaction();

			db.execSQL("UPDATE D_FACTURA SET STATCOM='S'");
			db.execSQL("UPDATE D_PEDIDO SET STATCOM='S'");
			db.execSQL("UPDATE D_NOTACRED SET STATCOM='S'");
			db.execSQL("UPDATE D_COBRO SET STATCOM='S'");
			db.execSQL("UPDATE D_DEPOS SET STATCOM='S'");
			db.execSQL("UPDATE D_MOV SET STATCOM='S'");
			db.execSQL("UPDATE D_CLINUEVO SET STATCOM='S'");
			db.execSQL("UPDATE D_ATENCION SET STATCOM='S'");
			db.execSQL("UPDATE D_CLICOORD SET STATCOM='S'");
			db.execSQL("UPDATE D_SOLICINV SET STATCOM='S'");
			db.execSQL("UPDATE D_MOVD SET CODIGOLIQUIDACION=0");
			db.execSQL("UPDATE FINDIA SET VAL5=0, VAL4=0,VAL3=0, VAL2=0");

			db.setTransactionSuccessful();
			db.endTransaction();
		}

	}catch (Exception e){
		msgbox("FinDia(): "+e.getMessage());
		return false;
	}
	return true;
	}


	// WEB SERVICE - ENVIO
	
	private boolean sendData(){

		senv = "Envio terminado \n \n";

		items.clear();
		
		envioFacturas();
		envioPedidos();
		envioNotasCredito();
		
		envioCobros();
		
		envioDepositos();		
		envio_D_MOV();
		envioCli();
		
		envioAtten();
		envioCoord();
		envioSolicitud();
		
		updateAcumulados();
		updateInventario();

		//updateLicence();

		envioFinDia();

		if(gl.banderafindia == true){

			claseFindia = new clsFinDia(this);

			FinDia();
			claseFindia.eliminarTablasD();

		}

		return true;
	}

	public void envioFacturas()     {

		Cursor DT;
		String cor,fruta;
		int i,pc=0,pcc=0,ccorel;
		
		fterr="";
					
		try {

			sql="SELECT COREL,RUTA,CORELATIVO FROM D_FACTURA WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {
				senv+="Facturas : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();

			dbld.clear();

			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				fruta=DT.getString(1);
				ccorel=DT.getInt(2);  
				
				dbg="::";
				
				try {

					i+=1;fprog="Factura "+i;wsStask.onProgressUpdate();
					
					//if(gl.banderafindia == false){ dbld.clear(); }
					dbld.clear();

					dbld.insert("D_FACTURA" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_FACTURAD","WHERE COREL='"+cor+"'");
					dbld.insert("D_FACTURAP","WHERE COREL='"+cor+"'");
					dbld.insert("D_FACTURAD_LOTES","WHERE COREL='"+cor+"'");
					dbld.insert("D_FACTURAF","WHERE COREL='"+cor+"'");

					dbld.insert("D_BONIF" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_BONIF_LOTES","WHERE COREL='"+cor+"'");
					dbld.insert("D_REL_PROD_BON","WHERE COREL='"+cor+"'");
					dbld.insert("D_BONIFFALT","WHERE COREL='"+cor+"'");
					
					dbld.add("UPDATE P_COREL SET CORELULT="+ccorel+"  WHERE RUTA='"+fruta+"'");	

					//if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_FACTURA SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
							dbg = sstr;
						}
					//}
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
					dbg=e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();dbg=fstr;
		}


		//if (gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Facturas : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Facturas : " + pc + "\n";
			}
		//}

	}	
		
	public void envioPedidos(){
		Cursor DT;
		String cor;
		int i,pc=0,pcc=0;
		
		fterr="";
					
		try {
			sql="SELECT COREL FROM D_PEDIDO WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {
				senv+="Pedidos : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				
				try {
					
					i+=1;fprog="Pedido "+i;wsStask.onProgressUpdate();

					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_PEDIDO" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_PEDIDOD","WHERE COREL='"+cor+"'");
					
					dbld.insert("D_BONIF" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_BONIF_LOTES","WHERE COREL='"+cor+"'");
					dbld.insert("D_REL_PROD_BON","WHERE COREL='"+cor+"'");
					dbld.insert("D_BONIFFALT","WHERE COREL='"+cor+"'");

					if(gl.banderafindia == false){
						if (commitSQL()==1) {
							sql="UPDATE D_PEDIDO SET STATCOM='S' WHERE COREL='"+cor+"'";
							db.execSQL(sql);
							Toast.makeText(this, "Envio correcto", Toast.LENGTH_SHORT).show();
							pc+=1;
						} else {
							fterr+="\n"+sstr;
						}
					}

				} catch (Exception e) {
					//fterr+="\n"+e.getMessage();
				}
			    DT.moveToNext();
			}

		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Pedidos : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Pedidos : " + pc + "\n";
			}
		}
	}
	
	public void envioCobros(){
		Cursor DT;
		String cor,fruta;
		int i,pc=0,pcc=0,corult;
		
		fterr="";
					
		try {
			sql="SELECT COREL,CORELATIVO,RUTA FROM D_COBRO WHERE STATCOM='N' ORDER BY COREL";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {
				senv+="Cobros : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);
				corult=DT.getInt(1);
				fruta=DT.getString(2);
				
				try {
					
					i+=1;fprog="Cobro "+i;wsStask.onProgressUpdate();
					
					dbld.clear();
					dbld.insert("D_COBRO" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_COBROD","WHERE COREL='"+cor+"'");
					dbld.insert("D_COBROP","WHERE COREL='"+cor+"'");
					
					dbld.add("UPDATE P_CORRELREC SET Actual="+corult+"  WHERE RUTA='"+fruta+"'");	
										
					if (commitSQL()==1) {
						sql="UPDATE D_COBRO SET STATCOM='S' WHERE COREL='"+cor+"'";
					    db.execSQL(sql); 
						pc+=1;
					} else {
						fterr+="\n"+sstr;
					}
							
				} catch (Exception e) {
					//fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Cobros : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Cobros : " + pc + "\n";
			}
		}
	}
	
	public void envioNotasCredito(){
		Cursor DT;
		String cor,fruta;
		int i,pc=0,pcc=0,ccorel;
		
		fterr="";
					
		try {
			sql="SELECT COREL,RUTA,CORELATIVO FROM D_NOTACRED WHERE STATCOM='N' ORDER BY CORELATIVO";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {
				senv+="Notas credito : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				fruta=DT.getString(1);
				ccorel=DT.getInt(2);  
				
				try {
					
					i+=1;fprog="Nota credito "+i;wsStask.onProgressUpdate();
					
					if(gl.banderafindia == false) { dbld.clear(); }

					dbld.insert("D_NOTACRED" ,"WHERE COREL='"+cor+"'");
										
					dbld.add("UPDATE P_CORELNC SET CORELULT="+ccorel+"  WHERE RUTA='"+fruta+"'");	

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_NOTACRED SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Notas credito : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Notas credito : " + pc + "\n";
			}
		}
	}	
	
	public void envioDepositos() {
		Cursor DT;
		String cor;
		int i,pc=0,pcc=0;

		fterr="";

		try {
			sql="SELECT COREL FROM D_DEPOS WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0) {
				senv+="Depositos : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				
				try {
					
					i+=1;fprog="Deposito "+i;wsStask.onProgressUpdate();
					
					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_DEPOS" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_DEPOSD","WHERE COREL='"+cor+"'");

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_DEPOS SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);
							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Depositos : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Depositos : " + pc + "\n";
			}
		}
	}

	public String Get_Corel_D_Mov(){
		Cursor DT;
		String cor = "";

		try {

			sql = "SELECT COREL FROM D_MOV WHERE STATCOM='N'";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();

			if (DT.getCount() > 0) {
				cor = DT.getString(0);
			}

		} catch (Exception e) {
			msgbox(e.getMessage());
		}
		return cor;
	}
	
	public void envio_D_MOV()
	{
		Cursor DT;
		String cor;
		int i,pc=0,pcc=0;
		
		fterr="";
					
		try {

			sql="SELECT COREL FROM D_MOV WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);

			if (DT.getCount()==0)
			{
				senv+="Inventario : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();

			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				
				try {
					
					i+=1;fprog="Inventario "+i;wsStask.onProgressUpdate();
					
					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_MOV" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_MOVD","WHERE COREL='"+cor+"'");

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_MOV SET STATCOM='S' WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							sql = "UPDATE D_MOVD SET CODIGOLIQUIDACION=0 WHERE COREL='" + cor + "'";
							db.execSQL(sql);

							pc += 1;

						} else {
							fterr += "\n" + sstr;
						}
					}
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Inventario : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Inventario : " + pc + "\n";
			}
		}
	}
	
	public void envioCli() {
		Cursor DT;
		String cor;
		int i,pc=0,pcc=0;
		
		fterr="";
					
		try {
			sql="SELECT CODIGO FROM D_CLINUEVO WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) {
				senv+="Inventario : "+pc+"\n";return;
			}
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				
				try {
					
					i+=1;fprog="Inventario "+i;wsStask.onProgressUpdate();
					
					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_CLINUEVO" ,"WHERE CODIGO='"+cor+"'");
					if (gl.peModal.equalsIgnoreCase("APR")) {
						dbld.insert("D_CLINUEVO_APR","WHERE CODIGO='"+cor+"'");
					}

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {

							sql = "UPDATE D_CLINUEVO SET STATCOM='S' WHERE CODIGO='" + cor + "'";
							db.execSQL(sql);
							if (gl.peModal.equalsIgnoreCase("APR")) {
								sql = "UPDATE D_CLINUEVO_APR SET STATCOM='S' WHERE CODIGO='" + cor + "'";
								db.execSQL(sql);
							}

							pc += 1;
						} else {
							fterr += "\n" + sstr;
						}
					}
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Cli. nuevos : " + pc + " , NO ENVIADO : " + pf + " \n";
			} else {
				senv += "Cli. nuevos : " + pc + "\n";
			}
		}
	}
	
	public void envioAtten() {
		Cursor DT;
		String cor,hora;
		int fecha;
		
		fterr="";
		fprog=" ";wsStask.onProgressUpdate();
				
		try {
			sql="SELECT RUTA,FECHA,HORALLEG FROM D_ATENCION WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
				fecha=DT.getInt(1);  
				hora=DT.getString(2);  
				
				try {
					
					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_ATENCION" ,"WHERE (RUTA='"+cor+"') AND (FECHA="+fecha+") AND (HORALLEG='"+hora+"') ");

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_ATENCION SET STATCOM='S' WHERE (RUTA='" + cor + "') AND (FECHA=" + fecha + ") AND (HORALLEG='" + hora + "') ";
							db.execSQL(sql);
						} else {
							//fterr+="\n"+sstr;
						}
					}
							
				} catch (Exception e) {
					//fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}
	
	}
	
	public void envioCoord() {
		Cursor DT;
		String cod,ss;
		int stp;
		double px,py;
		
		fterr="";
		fprog=" ";wsStask.onProgressUpdate();
				
		try {
			sql="SELECT CODIGO,COORX,COORY,STAMP FROM D_CLICOORD WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cod=DT.getString(0);  
				px=DT.getDouble(1);  
				py=DT.getDouble(2);  
				stp=DT.getInt(3);  
				
				try {
					
					if(gl.banderafindia == false){ dbld.clear(); }
					
					ss="UPDATE P_CLIENTE SET COORX="+px+",COORY="+py+" WHERE (CODIGO='"+cod+"')";					
					dbld.add(ss);
					fterr=ss;

					if(gl.banderafindia == false) {
						if (commitSQL() == 1) {
							sql = "UPDATE D_CLICOORD SET STATCOM='S' WHERE (CODIGO='" + cod + "') AND (STAMP=" + stp + ") ";
							db.execSQL(sql);
						} else {
							fterr += "\n" + sstr;
						}
					}

				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}
	
	}
	
	public void envioSolicitud(){
		Cursor DT;
		String cor;
		int i,pc=0,pcc=0;
		
		fterr="";
					
		try {
			
			sql="SELECT * FROM D_SOLICINVD";	
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			sql="SELECT COREL FROM D_SOLICINV WHERE STATCOM='N'";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return;
			
			pcc=DT.getCount();pc=0;i=0;
				
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				
				cor=DT.getString(0);  
					
				try {
					
					i+=1;fprog="Solicitud "+i;wsStask.onProgressUpdate();
					
					if(gl.banderafindia == false){ dbld.clear(); }

					dbld.insert("D_SOLICINV" ,"WHERE COREL='"+cor+"'");
					dbld.insert("D_SOLICINVD","WHERE COREL='"+cor+"'");

					if(gl.banderafindia == false) {
                        if (commitSQL() == 1) {
                            sql = "UPDATE D_SOLICINV SET STATCOM='S' WHERE COREL='" + cor + "'";
                            db.execSQL(sql);
                            pc += 1;
                        } else {
                            fterr += "\n" + sstr;
                        }
                    }
							
				} catch (Exception e) {
					fterr+="\n"+e.getMessage();
				}
				
			    DT.moveToNext();
			}	
		
		} catch (Exception e) {
			fstr=e.getMessage();
		}

		if(gl.banderafindia == false) {
			if (pc != pcc) {
				int pf = pcc - pc;
				senv += "Solicitud : " + pc + " , NO ENVIADO : " + pf + "\n";
			} else {
				senv += "Solicitud : " + pc + "\n";
			}
		}
	}	
	
	public void envioFinDia() {		
		fterr="";
		fprog=" ";wsStask.onProgressUpdate();
		
		try {
				
			if(gl.banderafindia == false){ dbld.clear(); }

			dbld.add("DELETE FROM D_REPFINDIA WHERE RUTA='"+gl.ruta+"'");
			dbld.insert("D_REPFINDIA" ,"WHERE (LINEA>=0)");

			if(gl.banderafindia == false){ commitSQL(); }


		} catch (Exception e) {
			fstr=e.getMessage();
		}
	}

	public void updateInventario() {
		DU = new DateUtils();
	    String vFecha;
		int rslt;
		int vfecha = Get_Fecha_Inventario();
		//#HS_20181203_1000 Agregue DU.univfechaext(vfecha) para convertir la fecha a formato de yymmdd hhmm
        vFecha = DU.univfechaext(vfecha);
		String corel_d_mov = Get_Corel_D_Mov();

		try {

			if(gl.banderafindia == false){ dbld.clear(); }

			ss = " UPDATE P_STOCK SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' )";
			dbld.add(ss);

			ss = " UPDATE P_STOCKB SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "')";
			dbld.add(ss);

			ss = " UPDATE P_STOCK_PALLET SET ENVIADO = 1, COREL_D_MOV = '" + corel_d_mov + "' " +
					" WHERE RUTA  = '" + gl.ruta + "' AND FECHA = '" + vFecha + "' AND ENVIADO = 0 " +
					" AND DOCUMENTO IN (SELECT DOCUMENTO FROM P_DOC_ENVIADOS_HH WHERE RUTA = '" + gl.ruta + "' AND FECHA = '" + vFecha + "')";
			dbld.add(ss);

			if(gl.banderafindia == false){
                fterr=ss+"\n";
                rslt=commitSQL();
                fterr=fterr+rslt+"\n";
            }

		} catch (Exception e) {
			fstr=e.getMessage();
			fterr=fterr+fstr;
		}

	}

	public void updateAcumulados() {
		int ff,oyear,omonth,rslt;
		
		ff=du.getActDate();
		oyear=du.getyear(ff);
		omonth=du.getmonth(ff);
		
		try {

		    if(gl.banderafindia == false){ dbld.clear(); }

			ss="exec AcumuladoObjetivos '"+gl.ruta+"',"+oyear+","+omonth;					
			dbld.add(ss);

			if(gl.banderafindia == false) {
                fterr = ss + "\n";
                rslt = commitSQL();
                fterr = fterr + rslt + "\n";
            }

		} catch (Exception e) {
			fstr=e.getMessage();
			fterr=fterr+fstr;
		}
	}
	
	public void updateLicence() {	
		String SQL;
		String TN="LIC_CLIENTE";

		try {

			fprog=TN;idbg=TN;
			listItems.clear();

		
			SQL="SELECT * FROM LIC_CLIENTE WHERE ID='"+mac+"'";
			if (fillTable(SQL,"DELETE FROM LIC_CLIENTE")==1) {
				idbg=idbg +SQL+"#"+"PASS OK";
			} else {	
				idbg=idbg +SQL+"#"+" PASS FAIL  ";
				fstr=sstr;
			}
			idbg=idbg+" :: " +listItems.size();
		} catch (Exception e) {
			fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
		}
	}
		
	public void addItem(String nombre,int env,int pend) {
		clsClasses.clsEnvio item;
		
		try {
			item=clsCls.new clsEnvio();
			
			item.Nombre=nombre;
			item.env=env;
			item.pend=pend;
			
			items.add(item);
			
		} catch (Exception e) {
		}
	}
	
	public void updateLicencePush() {
		String ss;
		
		try {
			ss=listItems.get(1);
			if (mu.emptystr(ss)) return;
			db.execSQL(ss);
		} catch (Exception e) {	
			//msgbox(e.getMessage());
		}
	}
	
	
	// Web Service handling Methods
	
	public void wsSendExecute(){

		running=1;fstr="No connect";scon=0;
					
		try {
						
			if (getTest()==1) {scon=1;}
					
			if (scon==1) {
				fstr="Sync OK";	
				if (!sendData()) {
					fstr="Envio incompleto : "+sstr;
				}else{

				}
			} else {	
				fstr="No se puede conectar al web service : "+sstr;
			}
					
		} catch (Exception e) {
			scon=0;
			fstr="No se puede conectar al web service. "+e.getMessage();
		}
	}
			
	public void wsSendFinished(){
				
		barInfo.setVisibility(View.INVISIBLE);
		lblParam.setVisibility(View.INVISIBLE);
		running=0;
		
		//senv="Envio completo\n";
		
		if (fstr.equalsIgnoreCase("Sync OK")) {
			lblInfo.setText(" ");
		} else {	
			lblInfo.setText(fstr);	
			mu.msgbox(fstr+"\n"+fterr);
		}
		
		mu.msgbox(senv);
		if (!dbg.equalsIgnoreCase("::")) mu.msgbox(dbg);
		
		//updateLicencePush();

		pendientes=validaPendientes();
		visibilidadBotones();
		
		isbusy=0;
	}
			
	private class AsyncCallSend extends AsyncTask<String, Void, Void>
	{

		@Override
	    protected Void doInBackground(String... params) {

			try {
				Looper.prepare();
				wsSendExecute();
			} catch (Exception e) {
			}

	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
	       	wsSendFinished();
            Looper.loop();
	    }
	 
        @Override
        protected void onPreExecute() {
    		try {
    		} catch (Exception e) {}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
    		try {
    			lblInfo.setText(fprog);
    		} catch (Exception e) {
    		}
        }
	 
    }
	
	
	// WEB SERVICE - CONFIRM

	private void sendConfirm() {
		Cursor dt;

		try {
			sql="SELECT DOCUMENTO FROM P_STOCK";
			dt=Con.OpenDT(sql);

			if (dt.getCount()>0) {
				dt.moveToFirst();
				docstock=dt.getString(0);
			} else {
				docstock="";
			}
		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}

		//#HS_20181126_1603 Cambie el getActDate por getFechaActual
		sql="UPDATE P_RUTA SET EMAIL='"+du.getActDate()+"'";
		db.execSQL(sql);


		Handler mtimer = new Handler();	
		Runnable mrunner=new Runnable() {
			@Override
			public void run() {
				showprogress=false;
				wsCtask = new AsyncCallConfirm();
				wsCtask.execute();
			}
		};
		mtimer.postDelayed(mrunner,500); 

	}

	
	// Web Service handling Methods

	public void wsConfirmExecute(){
		String univdate=du.univfecha(du.getActDate());
		isbusy=1;
		
		try {
			conflag=0;
					
			dbld.clear();
			dbld.add("INSERT INTO P_DOC_ENVIADOS_HH VALUES ('"+docstock+"','"+ActRuta+"','"+univdate+"',1)");	
						
			if (commitSQL()==1) conflag=1; else conflag=0;
					
		} catch (Exception e) {
			fterr+="\n"+e.getMessage();
			dbg=e.getMessage();
		}
	}

	public void wsConfirmFinished(){
		isbusy=0;
	}

	private class AsyncCallConfirm extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				wsConfirmExecute();
			} catch (Exception e) {
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			wsConfirmFinished();
		}

		@Override
		protected void onPreExecute() {
			try {
			} catch (Exception e) {}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			try {
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
			
			//if (gl.tipo==0) {
			//	wsurl=DT.getString(1);			
			//} else {	
			//	wsurl=DT.getString(0);
			//}
			
			wsurl=DT.getString(0);
			
			URL=wsurl;
			txtWS.setText(URL);
		} catch (Exception e) {
			//MU.msgbox(e.getMessage());
			//URL="*";txtWS.setText("http://192.168.1.1/wsAndr/wsandr.asmx");
			URL="*";txtWS.setText("http://192.168.1.112/WSANDR/wsandr.asmx");
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
		ActRuta=ss;
		
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
	
	private String getMac() {		
		WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}
	
	private int ultCorel() {
		Cursor DT;
		int crl;
		
		try {
			sql="SELECT CORELULT FROM P_COREL";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			crl=DT.getInt(0);
		} catch (Exception e) {
			crl=0;
		}	
		
		return crl;
	}

	//#HS_20181129_1006 Agregue funcion para obtener la serie.
	private String ultSerie(){
		Cursor DT;
		String serie="";

		try{
			sql="SELECT SERIE FROM P_COREL";
			DT=Con.OpenDT(sql);

			if(DT.getCount()>0) {
				DT.moveToFirst();
				serie = DT.getString(0);
			}

		}catch (Exception e){
			msgbox("ultSerie(): "+e.getMessage());
		}

		return serie;
	}

	//#HS_20181121_1048 Se creo la funcion Get_Fecha_Inventario().
	private int Get_Fecha_Inventario()
	{
		Cursor DT;
		int fecha = 0;

		try {

			sql="SELECT EMAIL FROM P_RUTA";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();

			if(DT.getCount()>0){
				fecha=DT.getInt(0);
				if (fecha==0)
				{
					fecha = 1001010000 ;//#HS_20181129_0945 Cambie los valores de fecha porque deben se yymmdd hhmm
				}

			}

		} catch (Exception e) {
			msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
		}
		return fecha;
	}

	private void visibilidadBotones() {
		Cursor dt;
		boolean recep=false;
		
		esvacio=false;
		
		lblEnv.setVisibility(View.VISIBLE);imgEnv.setVisibility(View.VISIBLE);
		lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);			
		relExist.setVisibility(View.VISIBLE);
		relPrecio.setVisibility(View.VISIBLE);
		relStock.setVisibility(View.VISIBLE);

		//#HS_20181121_0910 Se creo la funcion Get_Fecha_Inventario().
		int fc=Get_Fecha_Inventario();
		recep=fc==du.getActDate();
		
		try {
			sql="SELECT * FROM P_RUTA";
			dt=Con.OpenDT(sql);		
			esvacio=dt.getCount()==0;
		} catch (Exception e) {
			//msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
			esvacio=true;
		}

		if (pendientes) {
			lblInfo.setText("Pendiente : "+sp);
			lblRec.setVisibility(View.INVISIBLE);imgRec.setVisibility(View.INVISIBLE);
		} else {
			lblInfo.setText("");
			lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
			lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
		}

		//#HS 20181113_1241pm Quite la comparacion contra la letra V.
		if (rutatipo.equalsIgnoreCase("P")) {
			relExist.setVisibility(View.INVISIBLE);
		} else {
			relExist.setVisibility(View.VISIBLE);
		}

		if (gl.peModal.equalsIgnoreCase("TOL")) {
			if (pendientes) {
				relPrecio.setVisibility(View.INVISIBLE);
				relStock.setVisibility(View.VISIBLE);
				relExist.setVisibility(View.INVISIBLE);
			} else {
				if (recep) {
					relPrecio.setVisibility(View.VISIBLE);
					relExist.setVisibility(View.VISIBLE);
					relStock.setVisibility(View.INVISIBLE);	
				} else {						
					relPrecio.setVisibility(View.INVISIBLE);
					relExist.setVisibility(View.INVISIBLE);	
					relStock.setVisibility(View.VISIBLE);
				}				
			}
		} else {
			relPrecio.setVisibility(View.VISIBLE);
			relStock.setVisibility(View.VISIBLE);
		}	
	
		if (!gl.peBotInv) relExist.setVisibility(View.INVISIBLE);
		if (!gl.peBotPrec) relPrecio.setVisibility(View.INVISIBLE);
		if (!gl.peBotStock) relStock.setVisibility(View.INVISIBLE);
		
		if (!pendientes) {
			lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
		}
		
		if (gl.modoadmin) {

			txtRuta.setEnabled(true);
			txtWS.setEnabled(true);
			txtEmp.setEnabled(true);	

			if (esvacio) {
				lblEnv.setVisibility(View.INVISIBLE);imgEnv.setVisibility(View.INVISIBLE);
				lblRec.setVisibility(View.VISIBLE);imgRec.setVisibility(View.VISIBLE);
			}
			
			relExist.setVisibility(View.INVISIBLE);
			relPrecio.setVisibility(View.INVISIBLE);
			relStock.setVisibility(View.INVISIBLE);
		}
		
		
	}
	
	private void paramsExtra() {
		try {
			AppMethods app=new AppMethods(this,gl,Con,db);
			app.parametrosExtra();
		} catch (Exception e) {
			msgbox(e.getMessage());
		}
	}
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
	   if (isbusy==0) {
		   super.onBackPressed();
	   }
	}	
	
}
