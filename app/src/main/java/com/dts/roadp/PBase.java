package com.dts.roadp;

import android.app.Activity;
import android.os.Bundle;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PBase extends Activity {

	protected int active;
	protected SQLiteDatabase db;
	protected BaseDatos Con;
	protected BaseDatos.Insert ins;
	protected BaseDatos.Update upd;
	protected String sql;
	
	protected Application vApp;
	protected appGlobals gl;
	protected MiscUtils mu;
	protected DateUtils du;
	protected clsClasses clsCls = new clsClasses();
	protected InputMethodManager keyboard;	
	
	protected int itemid,browse,mode;
	protected int selid,selidx,fecha,deposito;
	protected String s,ss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plist_base);
	}

	public void InitBase(){
		
		Con = new BaseDatos(this);

	    opendb();
	    ins=Con.Ins;upd=Con.Upd;
		
		vApp=this.getApplication();
		gl=((appGlobals) this.getApplication());
		
		mu=new MiscUtils(this,gl.peMon);
		du=new DateUtils();fecha=du.getActDateTime();
		
		keyboard = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);

	    browse=0;
	}
	
	// Web service call back
	
	public void wsCallBack(){
		
	}
	
	// Aux
	
	protected void closekeyb(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
		
	protected void showkeyb(){
		if (keyboard != null) {
			keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
	}
		
	protected void hidekeyb() {
		keyboard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}	
	
	protected void msgbox(String msg){
		mu.msgbox(msg);
	}

	protected void toast(String msg) {
		Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();
	}
	
	protected void toast(double val) {
		this.toast(""+val);
	}
	
	protected void toastlong(String msg) {
		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected void toastcent(String msg) {

		if (mu.emptystr(msg)) return;

		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}	
	
	protected void addlog(String methodname,String msg,String info) {

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
			msgbox("Error " + e.getMessage());
		}

	}


	// Activity Events
	
	@Override
 	protected void onResume() {
		opendb();
	    super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			Con.close();   } 
		catch (Exception e) { }
		active= 0;
	    super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	public void opendb() {
		try {
			db = Con.getWritableDatabase();
			if (db!= null)
			{
				Con.vDatabase =db;
				active=1;
			}else{
				active = 0;
			}
	    } catch (Exception e) {
	    	mu.msgbox(e.getMessage());
	    	active= 0;
	    }
	}			
	
	
}
