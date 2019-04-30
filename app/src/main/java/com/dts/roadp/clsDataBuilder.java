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
	
	private BufferedWriter writer = null,lwriter = null;
	private FileWriter wfile,lfile;
	private String fname,logname;

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
		
		System.setProperty("line.separator","\r\n");
		
		fname = Environment.getExternalStorageDirectory()+"/SyncFold/rd_data.txt";
		logname = Environment.getExternalStorageDirectory()+"/roadenvio.txt";
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
			
			PRG.moveToFirst();j=0;
		
			while (!PRG.isAfterLast()) {
				  
				n=PRG.getString(PRG.getColumnIndex("name"));
				t=PRG.getString(PRG.getColumnIndex("type"));

				ct=getCType(n,t);
				tcol.add(ct);
				s=s+n+"  "+ct+"\n";
				
				SS=SS+n;
				if (j<cc-1) SS=SS+",";
				  
			    PRG.moveToNext();j+=1;
			}
			
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
		} catch (Exception e) {
			err=e.getMessage();//return false;
			throw new RuntimeException(err);
		}
		
		return (!err.isEmpty()?false:true);
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

	public void clearlog() {
		sendlog.clear();
	}

	public void savelog() {
		String s;

		if (sendlog.size()==0) return ;

		try {

			lfile=new FileWriter(logname,false);
			lwriter = new BufferedWriter(lfile);

			for (int i = 0; i < sendlog.size(); i++) {
				s=sendlog.get(i);
				lwriter.write(s);lwriter.write("\r\n");
			}

			lwriter.close();

		} catch(Exception e){
			return;
		}
	}


	// Private
	
	private int getCType(String cn,String ct) {
		int c=0;
		
		if (cn.equalsIgnoreCase("FECHA") || cn.equalsIgnoreCase("FECHAENTR") || cn.equalsIgnoreCase("FECHANAC")) {
			c=2;
			if (cn.equalsIgnoreCase("FECHANAC")) c=3;
		} else {	
			if (ct.equalsIgnoreCase("TEXT")) c=1;
		}
		
		return c;
	}
	
}
