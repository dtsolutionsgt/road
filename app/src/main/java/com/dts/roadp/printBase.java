package com.dts.roadp;

import datamaxoneil.connection.ConnectionBase;
import datamaxoneil.printer.DocumentLP;
import android.content.Context;
import android.os.Handler;

public class printBase {

	public String printerAddress = "Unknown";
	public Runnable printclose;
	public String errmsg;
	public int prwidth;
	
	protected Context cont;
	 
	protected ConnectionBase prconn = null;
	protected String fname;
	protected DocumentLP docLP = new DocumentLP("!");
	
	protected Thread prthread;
	protected Handler handler = new Handler(); 
	protected Runnable callback;
	
	protected byte[] printData = {0};	
	
	protected boolean hasCallback;
	
	public printBase(Context context,String printerMAC) {
		cont=context;
		printerAddress=printerMAC;	
	}
	
	// Abstract Methods
	
	public void printask(Runnable callBackHook) {
		hasCallback=true;
		callback=callBackHook;
	}

	public void printask(Runnable callBackHook, String fName) {
		hasCallback=true;
		callback=callBackHook;
		fname=fName;
	}

	public void printask() {
		hasCallback=false;
	}
		
	public boolean print() {
		return true;
	}
		
	public void printask(String fileName) {	
		hasCallback=false;
	}
			
	public boolean print(String fileName) {
		return true;
	}

}
