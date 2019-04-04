package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class clsDocument {

	public String nombre,numero,serie,ruta,vendedor,cliente,nit,tipo,ref;
	public String resol,resfecha,resvence,resrango,fsfecha,modofact;
	public String tf1="",tf2="",tf3="",tf4="",tf5="",add1="",add2="",deviceid;
	public clsRepBuilder rep;
	public boolean docfactura,docrecibo,docanul,docpedido,docdevolucion,doccanastabod;
	public int ffecha,pendiente,residx;
	
	protected android.database.sqlite.SQLiteDatabase db;
	protected BaseDatos Con;
	protected String sql;
	
	protected ArrayList<String> lines= new ArrayList<String>();
	
	protected Context cont;
	protected DateUtils DU;
	protected DecimalFormat decfrm;
	
	protected String clicod,clidir,pemodo,vendcod;

	protected int prw;
	
	public clsDocument(Context context,int printwidth,String cursym,int decimpres, String archivo) {
		cont=context;
		prw=printwidth;
		
		rep=new clsRepBuilder(cont,prw,true,cursym,decimpres, archivo);
		DU=new DateUtils();
		decfrm = new DecimalFormat("#,##0.00");

	}

	public boolean buildPrint(String corel,int reimpres) {
		setAddlog("Build print",""+DU.getActDateTime(),"");

		modofact="*";
		rep.clear();
				
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}

	public boolean buildPrint(String corel,int reimpres,String modo) {
		int flag;

        modofact=modo;
		rep.clear();

		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;

		flag=0;

		if (modofact.equalsIgnoreCase("TOL")) {
			if (docfactura && (reimpres==10)) flag=1;
			if (doccanastabod) flag=2;
			if (docrecibo && (reimpres==0)) flag=3;
        } else if(modofact.equalsIgnoreCase("*")) {
            if (doccanastabod) flag = 2;
        }

		if (flag==0) {
			if (!rep.save()) return false;
		} else if (flag==1){
			if (!rep.save(2)) return false;
		} else if (flag==2){
			if (!rep.save(3)) return false;
		} else if (flag==3){
			if (!rep.save(3)) return false;
		}

		return true;
	}
	
	public boolean buildPrint(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb ) {
		
		rep.clear();
				
		if (!buildHeader(corel,reimpres,pCon,pdb)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintExt(String corel,int reimpres,String modo) {
		
		pemodo=modo;
		
		rep.clear();
		
		if (!buildHeader(corel,0)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!buildHeader(corel,reimpres)) return false;
		if (!buildDetail()) return false;
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}
	
	public boolean buildPrintSimple(String corel,int reimpres) {
		
		rep.clear();
		
		if (!buildFooter()) return false;
		
		if (!rep.save()) return false;
		
		return true;
	}


	// Methods Prototypes
	
	protected boolean buildDetail() {
		return true;
	}
	
	protected boolean buildFooter() {
		
		return true;
	}	
	
	protected boolean loadDocData(String corel) {
		return true;
	}
	
	protected boolean loadHeadData(String corel) {
		nombre="";numero="";serie="";ruta="";vendedor="";cliente="";
		
		return true;
	}

    protected void saveHeadLines(int reimpres) {
        String s;

        rep.empty();rep.empty();

        for (int i = 0; i <lines.size(); i++) 		{

            s=lines.get(i);
            s=encabezado(s);

            if (docpedido) {
                s=s.replace("Factura serie","Pedido");
                s=s.replace("numero : 0","");
            }

            if (docrecibo) {
				s=s.replace("Factura","Recibo");
            }

            if(docdevolucion){
				s=s.replace("Factura","Recibo");
			}

			if(doccanastabod){
				s=s.replace("Factura","Recibo");
			}

            if (residx==1) {
                if (docfactura) {
                    rep.add(resol);
                    rep.add(resfecha);
                    rep.add(resvence);
                    rep.add(resrango);
					rep.add("Fecha de Emision : "+fsfecha);
                }
                residx=0;
            }

            if (!s.equalsIgnoreCase("@@")) rep.add(s);
        }

        if (!emptystr(nit)) rep.add("NIT : "+nit);
        if (!emptystr(clidir)) rep.add("Dir : "+clidir);
        if(docdevolucion || doccanastabod) rep.add("Fecha de Emision : "+fsfecha);
        //if (!emptystr(clicod)) rep.add("Codigo: "+clicod);


        if (!emptystr(add1)) {

            rep.add("");
            rep.add(add1);
            if (!emptystr(add2)) rep.add(add2);
            rep.add("");

        }

        if (docfactura){

			rep.add("");
			if (docfactura && (reimpres==1)) rep.add("-------  R E I M P R E S I O N  -------");
			if (docfactura && (reimpres==10)) rep.add("-------  R E I M P R E S I O N  -------");
			if (docfactura && (reimpres==2)) rep.add("------  C O P I A  ------");
			if (docfactura && (reimpres==3)) rep.add("------       A N U L A D O      ------");
			//#HS_20181212 condición para factura pendiente de pago
			if(docfactura && (reimpres==4)) {
				rep.add("- P E N D I E N T E  D E  P A G O -");
				pendiente = reimpres;
			}
			if (docfactura && (reimpres==5)) rep.add("------  C O N T A B I L I T A D  ------");
			rep.add("");

		}else if(docdevolucion){

			rep.add("");
			if (docdevolucion && (reimpres==1)) rep.add("-------  R E I M P R E S I O N  -------");
			if (docdevolucion && (reimpres==2)) rep.add("------  C O P I A  ------");
			if (docdevolucion && (reimpres==3)) rep.add("------       A N U L A D O      ------");
			rep.add("");

		}



    }

    protected String encabezado(String l) {
        String s,lu,a;
        int idx;

        residx=0;

        //lu=l.toUpperCase().trim();
        lu=l.trim();

        if (lu.length()==1 && lu.equalsIgnoreCase("N")) {
            s=nombre;s=rep.ctrim(s);return s;
        }

        if (l.indexOf("dd-MM-yyyy")>=0) {
            s=DU.sfecha(DU.getActDateTime());
            l=l.replace("dd-MM-yyyy",s);return l;
        }

        if (l.indexOf("HH:mm:ss")>=0) {
            s=DU.shora(DU.getActDateTime());
            l=l.replace("HH:mm:ss",s);return l;
        }

		if (l.indexOf("##") >=0) {

			int index = l.indexOf("##");

			String temp = l.substring(index + 2, index + 4);
			String temp1 = l.substring(index + 4, index + 5);

			int ctemp= Integer.parseInt(temp);

			String str=StringUtils.leftPad("", ctemp, temp1);

			if (!serie.isEmpty()) {
				numero = StringUtils.right(str + numero, Integer.parseInt(temp));

				if (!numero.isEmpty()){
                    int ctemp1= Integer.parseInt(numero);
                    if (ctemp1==0) numero = StringUtils.leftPad("", ctemp);
                }

            }

			if (l.length() > index + numero.length() ){
				l = l.substring(0, index) + numero + l.substring(index + numero.length());
			}else{
				l = l.substring(0, index) + numero;
			}

			if (l.indexOf("##")>=0) {
				l = StringUtils.replace(l,"##","");
			}
		}

		if (StringUtils.upperCase(l).indexOf("S#") != -1) {

			int index = StringUtils.upperCase(l).indexOf("S#");

			String temp = l.substring(index + 2, index + 4);
			String temp1 = l.substring(index + 4, index + 5);

			int ctemp= Integer.parseInt(temp);

			String str=StringUtils.leftPad("", ctemp, temp1);

			numero = StringUtils.right(str + numero, Integer.parseInt(temp));

            if (!numero.isEmpty()){
                int ctemp1= Integer.parseInt(numero);
                if (ctemp1==0) numero = StringUtils.leftPad("", ctemp);
            }

			if ((l.length()) > index + serie.length() + numero.length()) {
				l = l.substring(0, index) + serie + numero + l.substring(index + serie.length() + numero.length());
			}else{
				l = l.substring(0, index) + serie + numero;
			}

			if (l.indexOf("S#")>=0) {
				l = StringUtils.replace(l,"S#","");
			}
		}

		idx=l.indexOf("SS");
        if (idx>=0) {

			if (l.length() > idx + serie.length()) {
				l = l.substring(0, idx) + serie + l.substring(idx + 2, idx + l.length() - idx - 2);
			}else{
				l = l.substring(0, idx) + serie;
			}

			if (l.indexOf("SS")>=0) {
				l = StringUtils.replace(l,"SS","");
			}

        }

        idx=lu.indexOf("VV");
        if (idx>=0) {
        	rep.addc("");
            if (emptystr(vendedor)) return "@@";
            l=l.replace("VV",vendcod+" - "+vendedor);return l;
        }

        idx=lu.indexOf("RR");
        if (idx>=0) {
            if (emptystr(ruta)) return "@@";
            l=l.replace("RR",ruta);return l;
        }

        idx=lu.indexOf("CC");
        if (idx>=0) {
            if (emptystr(cliente)) return "@@";
            if(l.length()>14){
				l=l.replace("CC",clicod+" - "+cliente);
				return l;
			}
            l=l.replace("CC",clicod+" - "+rep.ltrim(cliente, 14));return l;
        }

        return l;
    }


    // Private

	private boolean buildHeader(String corel,int reimpres) {
		
		lines.clear();
		
		try {
			Con = new BaseDatos(cont);
			opendb();
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);
			}

			loadHeadLines();

			try {
				Con.close();   
			} catch (Exception e1) {

			}
			
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			Toast.makeText(cont,"buildheader: "+e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}

		saveHeadLines(reimpres);
		
		return true;
	}

	private boolean buildHeader(String corel,int reimpres,BaseDatos pCon,android.database.sqlite.SQLiteDatabase pdb) {
		
		lines.clear();
		
		try {
				
			Con=pCon;db=pdb;
			
			if (!corel.equalsIgnoreCase("0")) {
				loadDocData(corel);
				loadHeadData(corel);
			}
			loadHeadLines();

			saveHeadLines(reimpres);
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();return false;
		}		

		return true;
	}

	
	// Aux
	
	private boolean loadHeadLines() {
		Cursor DT;	
		String s,sucur;
		
		try {

			sql = "SELECT SUCURSAL FROM P_RUTA";
			DT = Con.OpenDT(sql);
			DT.moveToFirst();
			sucur = DT.getString(0);

			sql="SELECT TEXTO FROM P_ENCABEZADO_REPORTESHH WHERE SUCURSAL='"+sucur+"' ORDER BY CODIGO";
			DT=Con.OpenDT(sql);
			if (DT.getCount()==0) return false;

			DT.moveToFirst();
			while (!DT.isAfterLast()) {

				s=DT.getString(0);	
				lines.add(s);	

				DT.moveToNext();
			}

			return true;
		} catch (Exception e) {
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
			//Toast.makeText(cont,e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	public boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}
	
	public String sfecha(int f) {
		int vy,vm,vd;
		String s;
		
		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		
		s="";
		if (vd>9) { s=s+String.valueOf(vd)+"-";} else {s=s+"0"+String.valueOf(vd)+"-";}  
		if (vm>9) { s=s+String.valueOf(vm)+"-20";} else {s=s+"0"+String.valueOf(vm)+"-20";}  
		if (vy>9) { s=s+String.valueOf(vy);} else {s=s+"0"+String.valueOf(vy);} 
		
		return s;
	}

	public String shora(int vValue) {
		int h,m;
		String sh,sm;

		h=vValue % 10000;
		m=h % 100;if (m>9) {sm=String.valueOf(m);} else {sm="0"+String.valueOf(m);}
		h=(int) h/100;if (h>9) {sh=String.valueOf(h);} else {sh="0"+String.valueOf(h);}

		return sh+":"+sm;
	}
	
	public String frmdecimal(double val,int ndec) {
		String ss="",ff="#,##0.";
		DecimalFormat ffrmint = new DecimalFormat("#,##0"); 
		
		if (ndec<=0) {		
			ss=ffrmint.format((int) val);return ss;
		}
		
		for (int i = 1; i <ndec+1; i++) {
			ff=ff+"0";
		}
		
		DecimalFormat decim = new DecimalFormat(ff);
		ss=decim.format(val);
		
		return ss;
	}

	public void toast(String msg) {
		Toast.makeText(cont,msg, Toast.LENGTH_SHORT).show();	
	}

	public void setAddlog(String methodname,String msg,String info) {

		BufferedWriter writer = null;
		FileWriter wfile;

		try {

			String fname = Environment.getExternalStorageDirectory()+"/roadlog.txt";
			wfile=new FileWriter(fname,true);
			writer = new BufferedWriter(wfile);

			writer.write("Método: " + methodname + " Mensaje: " +msg + " Info: "+ info );
			writer.write("\r\n");

			writer.close();

		} catch (Exception e) {
			//msgbox("Error " + e.getMessage());
		}
	}

	private void opendb() {
		
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
	    } catch (Exception e) {
	    	//Toast.makeText(cont,"Opendb "+e.getMessage(), Toast.LENGTH_LONG).show();
			setAddlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");

		}
	}	
		
}
