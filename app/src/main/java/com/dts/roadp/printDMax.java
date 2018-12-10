package com.dts.roadp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import datamaxoneil.connection.Connection_Bluetooth;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

// 00:17:AC:01:75:E9

public class printDMax extends printBase
{
	
	private String ss;
	private PrintDialog printDialog;
	
	public printDMax(Context context,String printerMAC) {
		super(context,printerMAC);
	}
	
	
	// Main
	
	public void printask(Runnable callBackHook) {
			
		hasCallback=true;
		callback=callBackHook;
		
		fname="print.txt";errmsg="";
		msgAskPrint();
	}
	
	public void printask()
	{
		hasCallback=false;
		
		fname="print.txt";errmsg="";
		msgAskPrint();				
	}
	
	public boolean print()
	{

		hasCallback=false;
		
		fname="print.txt";errmsg="";

		try
		{
			if (loadFile()){
				doStartPrint();
			}	else
				return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	
	public void printask(String fileName)
	{
		hasCallback=false;
		
		fname=fileName;	errmsg="";
		msgAskPrint();				
	}
		
	public boolean print(String fileName)
	{
		hasCallback=false;
		
		fname=fileName;errmsg="";
		
		try
		{
			if (loadFile())
			{
				doStartPrint();
			}else
				return false;
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());return false;
		}			
		
		return true;
	}
	

	// Private
	
	private boolean loadFile()
	{

		File ffile;
		BufferedReader dfile;
		String ss;
		
		try {
			
			File file1 = new File(Environment.getExternalStorageDirectory(), "/"+fname);
			ffile = new File(file1.getPath());
					
			FileInputStream fIn = new FileInputStream(ffile);
			dfile = new BufferedReader(new InputStreamReader(fIn));
			
		} catch (Exception e) {
			showmsg("Error: " + e.getMessage());
			return false;
		}			
		
		try {
				
			
			docLP.clear();
			
			while ((ss = dfile.readLine()) != null) {
					docLP.writeText(ss);
			}
			
			docLP.writeText("");
			docLP.writeText("");
			
			dfile.close();	
				       
	        printData = docLP.getDocumentData();
	
			return true;

		} catch (Exception e) {
			try {
				dfile.close();
			} catch (Exception e1) {}	
			
			showmsg("Error: " + e.getMessage());
			
			return false;
		}			
	}
	
	private void doStartPrint()
	{
		showmsg("Imprimiendo ..." );
		//showmsg("MAC : "+printerAddress );
		AsyncPrintCall wsRtask = new AsyncPrintCall();
		wsRtask.execute();
	}
	
	private class AsyncPrintCall extends AsyncTask<String, Void, Void> {

		@Override
	    protected Void doInBackground(String... params)
		{
			try
			{
				processPrint();
			} catch (Exception e) {
				Log.d("Err_Impr",e.getMessage());
			}
	            
	        return null;
	    }
	 
	    @Override
	    protected void onPostExecute(Void result) {
	    	try {
	    		doCallBack();
			} catch (Exception e) {}
	    }
	 
        @Override
        protected void onPreExecute() {}
	 
        @Override
        protected void onProgressUpdate(Void... values) {}
	 
    }	
	
	private void doCallBack() {
		//showmsg("Impresión completa.");
		
		//showmsg("Imp : "+ss);
		
		if (!hasCallback) return;
		
		final Handler cbhandler = new Handler();
		cbhandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.run();
			}
		}, 500);

		//#EJC201800: Llamar el close de la printer
		final Handler cbhandler1 = new Handler();
		cbhandler1.postDelayed(new Runnable() {
			@Override
			public void run() {
				printclose.run();
			}
		}, 200);
	}
	
	public void processPrint() {
		
		ss="p1..";
		
		try {

			prconn = null;
			
			ss=ss+"p2..";
			
			//Looper.prepare();

			prconn = Connection_Bluetooth.createClient(printerAddress);
		
			ss=ss+"p3..";
			
			if (!prconn.getIsOpen()) prconn.open();
				
			ss=ss+"p4..";
			
			prconn.write(printData);

			prthread.sleep(1500);

			prconn.clearWriteBuffer();
			printclose.run();
			prconn.close();

			ss=ss+"p6..";
		
		} catch (Exception e) {
			ss=ss+"Error : " + e.getMessage();
			Log.d("processPrint_ERR: ", ss);
			if (prconn != null) prconn.close();
		}	
		
	}
	
	
	// Aux
	
	private void msgAskPrint()
	{

		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage("¿La impresora está lista?");

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	try
				{
					if (loadFile())
						doStartPrint();
				} catch (Exception e) {
					showmsg("Error: " + e.getMessage());
				}
		    }

		});

//#EJC20181130:Se comentarió por solicitud de auditor de SAT.
//		dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
//		{
//		    public void onClick(DialogInterface dialog, int which)
//			{
//		    	final Handler cbhandler = new Handler();
//				cbhandler.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						printclose.run();
//					}
//				}, 200);
//		    }
//		});

		dialog.show();
			
	}
	
	public void showmsg(String MsgStr) {
		errmsg=MsgStr;
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(cont, errmsg, Toast.LENGTH_SHORT).show();
			}
		});
			
	}	
	

}
