package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

public class PrintDialog extends PBase {

	private printer prn;
	private Runnable printcallback,printclose;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_dialog);
		
		super.InitBase();
		
		printcallback= new Runnable() {
		    public void run() {
		    	askPrint();
		    }
		};
		
		printclose= new Runnable() {
		    public void run() {
		    	PrintDialog.super.finish();
		    }
		};
		
		prn=new printer(this,printclose);
		
		final Handler shandler = new Handler();
		shandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				singlePrint();
			}
		}, 500);		

	}
	
	// Events
	
	// Main
	
 	public void singlePrint() {
 		prn.printask(printcallback);
 	}	
 	
	private void askPrint() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		    	
		dialog.setTitle("Road");
		dialog.setMessage("Impresion correcta ?");
					
		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {	
		    	
				((appGlobals) vApp).closeCliDet=true;
				((appGlobals) vApp).closeVenta=true;
				PrintDialog.super.finish();		
		    }
		});
		
		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {			      	
		    	singlePrint();
		    }
		});
		
		
		dialog.show();
			
	}	

	// Aux
	
	// Activity Events
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
