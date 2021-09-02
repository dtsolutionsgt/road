package com.dts.roadp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class clsDataBuilder {

	public ArrayList<String> items=new ArrayList<String>();

	public String err="";
	
	private Context cCont;
	
	protected SQLiteDatabase db;
	protected BaseDatos Con;

	private ArrayList<Integer> tcol=new ArrayList<Integer>();
	private ArrayList<String> sendlog=new ArrayList<String>();


	private DateUtils DU;
	private MiscUtils MU;
	private AppMethods AM;
	
	private BufferedWriter writer = null,lwriter = null,writerbk = null,lwriterbk = null;
	private FileWriter wfile,lfile,wfilebk,lfilebk;
	private String fname,logname,logname2,namefile, codCliNuevo,lognamebk,namefilebk;

	public clsDataBuilder(Context context) {
		
		cCont=context; 
		DU=new DateUtils();
		MU=new MiscUtils(cCont);

		Con = new BaseDatos(cCont);
		try {
			db = Con.getWritableDatabase();
		 	Con.vDatabase =db;
	    } catch (Exception e) {
	    	MU.msgbox(e.getMessage());
	    }

		parametrosGlobales();

		System.setProperty("line.separator","\r\n");
		
		fname = Environment.getExternalStorageDirectory()+"/SyncFold/rd_data.txt";
		logname = Environment.getExternalStorageDirectory()+"/roadenvio.txt";
		logname2 = Environment.getExternalStorageDirectory()+"/roadenvio_bck.txt";
		namefile = Environment.getExternalStorageDirectory()+"/data.acr";

		lognamebk = Environment.getExternalStorageDirectory()+"/roadenvio"+DU.dayofweek(fechaFactTol(DU.getActDate()))+".txt";
		namefilebk = Environment.getExternalStorageDirectory()+"/data"+DU.dayofweek(fechaFactTol(DU.getActDate()))+".acr";

	}
	
	public void close(){
		try {
			Con.close();   } 
		catch (Exception e) { }
	}
	
	public void add(String si) {
		items.add(si);
		sendlog.add(si);
	}
	
	public boolean insert(String tn,String ws){

		Cursor PRG,DT;
		String s,n,t,si;
		int j,cc,ct;
		
		tcol.clear();
		String SQL_="INSERT INTO "+tn+" VALUES(";
		String SS="SELECT ";
		
		s="";

		try {

			if (!db.isOpen()){
				db = Con.getWritableDatabase();
				Con.vDatabase =db;
			}

			String vSQL = "PRAGMA table_info('"+tn+"')"; 
			PRG=db.rawQuery(vSQL, null);
			cc=PRG.getCount();
			if (tn.equalsIgnoreCase("D_CANASTA")) cc--;
			PRG.moveToFirst();j=0;
		
			while (!PRG.isAfterLast()) {
				  
				n=PRG.getString(PRG.getColumnIndex("name"));
				t=PRG.getString(PRG.getColumnIndex("type"));
				if (tn.equals("D_CANASTA") && n.equalsIgnoreCase("IDCANASTA")) {
					PRG.moveToNext();
					continue;
				}

				ct=getCType(n,t);
				tcol.add(ct);
				s=s+n+"  "+ct+"\n";

				SS=SS+n;
				if (j<cc-1) SS=SS+",";

				PRG.moveToNext();j+=1;
			}

			if (tn.equals("D_FACTURA")) SS="SELECT COREL, ANULADO, FECHA, EMPRESA, RUTA, VENDEDOR, " +
					                       "CASE ADD1 WHEN 'NUEVO' THEN '" + codCliNuevo + "' ELSE CLIENTE END  CLIENTE, " +
					                       "KILOMETRAJE, FECHAENTR, FACTLINK, TOTAL, DESMONTO, IMPMONTO, PESO, BANDERA, "+
					                       "STATCOM, CALCOBJ, SERIE, CORELATIVO, IMPRES, ADD1, ADD2, ADD3, DEPOS, PEDCOREL," +
					                       "REFERENCIA, ASIGNACION, SUPERVISOR, AYUDANTE, VEHICULO, CODIGOLIQUIDACION, " +
					                       "RAZON_ANULACION, CODIGO_RUTA_PEDIDO, DESPCOREL";
			if (tn.equals("D_CANASTA")) SS="SELECT RUTA,FECHA,CLIENTE,PRODUCTO,CANTREC,CANTENTR,STATCOM,CORELTRANS,PESOREC,PESOENTR," +
											"ANULADO,UNIDBAS,CODIGOLIQUIDACION";
		} catch (Exception e) {
			err=e.getMessage();//return false;
			throw new RuntimeException(err);
		}
		
		SS=SS+" FROM "+tn+" "+ws;
		
		try {

			DT=Con.OpenDT(SS);
			if (DT.getCount()==0) return true;
			
			DT.moveToFirst();
			while (!DT.isAfterLast()) {
				  
				si=SQL_;
				
				for (int i = 0; i < cc; i++) {

					ct=tcol.get(i);

					if (ct==0) s=""+DT.getDouble(i);
					if (ct==1) s="'"+DT.getString(i)+"'";

					if (ct==2) s="'"+DU.univfechaext(DT.getInt(i))+"'";
					if (ct==3) s="'"+DU.univfechaext(DT.getInt(i))+"'";
					
					if (i<cc-1) s=s+",";
					si=si+s;
			    }
				
				si=si+")";

				items.add(si);
				sendlog.add(si);
						  
			    DT.moveToNext();
			}

			if(DT!=null) DT.close();

		} catch (Exception e) {
			err=e.getMessage();//return false;
			throw new RuntimeException(err);
		}
		
		return (!err.isEmpty()?false:true);
	}

	public void parametrosGlobales() {
		Cursor dt;
		String sql;

		try {
			sql="SELECT FTPSERVER FROM P_GLOBPARAM";
			dt=Con.OpenDT(sql);

			if(dt.getCount()>0){
				dt.moveToFirst();

				codCliNuevo =dt.getString(0);
			}else{
				codCliNuevo ="";
			}

		} catch (Exception e) {
			MU.toast("Ocurrió un error obteniendo los valores de clientes nuevos" + e.getMessage());
		}

	}

	public long fechaFactTol(long f0) {
		Cursor DT;
		String sql;
		long ff;

		try {
			sql = "SELECT FECHA FROM P_FECHA";
			DT = Con.OpenDT(sql);

			DT.moveToFirst();
			ff=DT.getLong(0);

			return ff;
		} catch (Exception e) {
			return f0;
		}
	}

	public void clear(){
		items.clear();
	}
	
	public int size(){
		return items.size();
	}

	public int save(){
		String s;
		
		if (items.size()==0) {return 1;}
		
		try {
		 				
			wfile=new FileWriter(fname,false);
			writer = new BufferedWriter(wfile);
				
		    for (int i = 0; i < items.size(); i++) {
			   	s=items.get(i);
			   	writer.write(s);writer.write("\r\n");
			}
			
		    writer.close();
		    
		} catch(Exception e){
			return 0;
		}
				
		return 1;		
	}

	public int saveArchivo(String fecha){
		String s;
		if (items.size()==0) {return 1;}

		try {

			wfile=new FileWriter(namefile,false);
			writer = new BufferedWriter(wfile);
			writer.write("#"+fecha);writer.write("\r\n");

			for (int i = 0; i < items.size(); i++) {
				s=items.get(i);
				writer.write(s);writer.write("\r\n");
			}

			writer.close();

		} catch(Exception e){
			return 0;
		}

		return 1;
	}

	public int saveArchivo_bck(String fecha){
		String s;
		if (items.size()==0) {return 1;}

		try {

			wfile=new FileWriter(logname2,false);
			writer = new BufferedWriter(wfile);
			writer.write("#"+fecha);writer.write("\r\n");

			for (int i = 0; i < items.size(); i++) {
				s=items.get(i);
				writer.write(s);writer.write("\r\n");
			}

			writer.close();

		} catch(Exception e){
			return 0;
		}

		return 1;
	}

	public void clearlog() {
		sendlog.clear();
	}

	public void savelog() {
		String s;

		if (sendlog.size()==0) return ;

		try {

			lfile=new FileWriter(logname,false);
			lwriter = new BufferedWriter(lfile);

			lfilebk=new FileWriter(lognamebk,false);
			lwriterbk = new BufferedWriter(lfilebk);

			for (int i = 0; i < sendlog.size(); i++) {
				s=sendlog.get(i);
				lwriter.write(s);lwriter.write("\r\n");
				lwriterbk.write(s);lwriterbk.write("\r\n");
			}

			lwriter.close();
			lwriterbk.close();

		} catch(Exception e){
			return;
		}
	}

	public void savelog(String flogname) {
		String s;
		FileWriter ffile;
		BufferedWriter fwriter;
		flogname =  Environment.getExternalStorageDirectory()+"/"+flogname;

		if (sendlog.size()==0) return ;

		try {

			ffile=new FileWriter(flogname,false);
			fwriter = new BufferedWriter(ffile);

			for (int i = 0; i < sendlog.size(); i++) {
				s=sendlog.get(i);
				fwriter.write(s);
				fwriter.write("\r\n");
			}

			fwriter.close();

		} catch(Exception e){
			return;
		}
	}

	// Private
	
	private int getCType(String cn,String ct) {
		int c=0;
		
		if (cn.equalsIgnoreCase("FECHA") || cn.equalsIgnoreCase("FECHAENTR")
            || cn.equalsIgnoreCase("FECHANAC") || cn.equalsIgnoreCase("FECHA_SISTEMA") ) {
			c=2;
			if (cn.equalsIgnoreCase("FECHANAC")) c=3;
		} else {	
			if (ct.equalsIgnoreCase("TEXT")) c=1;
		}
		
		return c;
	}
	
}
