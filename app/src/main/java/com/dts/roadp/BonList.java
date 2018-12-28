package com.dts.roadp;

import java.util.ArrayList;

import com.dts.roadp.clsClasses.clsBonifItem;
import com.dts.roadp.clsClasses.clsBonifProd;
import com.dts.roadp.clsClasses.clsCFDV;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BonList extends PBase {

	private ListView listView;
	private RelativeLayout relMonto;
	private TextView lblMonto,lblFalta,lblTipo,lblMarg;
	private ImageView imgComp,imgAdd;
	
	private ArrayList<clsClasses.clsBonifProd> items = new ArrayList<clsClasses.clsBonifProd>();
	private ListAdaptBonif adapter;
	private clsBonifItem item;
	private clsBonifProd selitem;
	private Precio prc;
	
	private InputMethodManager keyboard;	
	
	private String lista,rutatipo,bonprodid,um; 
	private int pos,mpos,tipolista,selpos,poruni,nivel,dectipo;
	private double valor,bonprodcant,icant,falt,mul,marg;
	private boolean completo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bon_list);
		
		super.InitBase();
		
		listView =  (ListView) findViewById(R.id.listView1);
		relMonto =  (RelativeLayout) findViewById(R.id.relMonto);
		lblMonto =  (TextView) findViewById(R.id.textView5);
		lblFalta =  (TextView) findViewById(R.id.textView7);
		lblTipo =  (TextView) findViewById(R.id.textView2);
		lblMarg =  (TextView) findViewById(R.id.textView8);
		imgComp =  (ImageView) findViewById(R.id.imageView2);
		imgAdd =  (ImageView) findViewById(R.id.imageView3);
		
		rutatipo=gl.rutatipo;
		bonprodid=gl.bonprodid;
		bonprodcant=gl.bonprodcant;
		nivel=gl.nivel;
		
		keyboard = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);	
		
		sethandlers();
		
		prc=new Precio(this,mu,gl.peDec);
		um=gl.um;
		
		clearByProd();	
		marg=getMargin();
		
		mpos=gl.bonus.size();
		pos=0;
		setItem();
		
		//showkeyb();
	}
	
	
	// Events
	
	public void nextScreen(View view) {
		processNextScreen();
	}
	
	public void selectProd(View view) {
		gl.gstr="";
		browse=1;
		
		if (rutatipo.equalsIgnoreCase("P")) gl.prodtipo=0;else gl.prodtipo=1;
		
		Intent intent = new Intent(this,Producto.class);
		startActivity(intent);	
	}
		
	private void sethandlers() {
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	  	  		    	  
		    	try {
		    		Object lvObj = listView.getItemAtPosition(position);
		    		selitem = (clsBonifProd)lvObj;
			         
		       		adapter.setSelectedIndex(position);
					selpos=position;	
					
					//if (tipolista!=2) msgAskDel("Eliminar producto de la lista ?");
					if (tipolista==2) setVarCant();
					if (tipolista==3) setVarAllCant();
					
			    } catch (Exception e) {
			   	   mu.msgbox( e.getMessage());
			    }
			  	//return true;
		   }
		});				
		
	}
	
	
	// Main
	
	private void showBonList() {
		double mmarg;
		
		lista=item.lista;
		tipolista=item.tipolista;
		valor=item.valor;mmarg=marg*valor;
		mul=item.mul;
		
		lblMarg.setText(" ");
		
		imgComp.setVisibility(View.INVISIBLE);
		imgAdd.setVisibility(View.INVISIBLE);
		
		switch (tipolista) {
		case 0:  
			s="Producto";break;
		case 1:  
			s="Lista fija";break;
		case 2:  
			s="Lista variable";
			lblMarg.setText("+/- "+mu.frmdec(mmarg));
			break;
		case 3:  
			s="Todos los productos";
			imgAdd.setVisibility(View.VISIBLE);
			lblMarg.setText("+/- "+mu.frmdec(mmarg));
			break;	
		}
		
		lblTipo.setText(s);		
		listItems();
		
	}
	
	private void listItems() {
		Cursor DT;
		clsClasses.clsBonifProd item;	
		double ddisp,bcant;
		
		items.clear();
		
		//mu.msgbox("Lista : " +lista+"  ,   TipoLista : "+tipolista);
		
		try {
			
			switch (tipolista) {
			case 0:  
				sql="SELECT CODIGO,DESCLARGA,1,0 FROM P_PRODUCTO WHERE (CODIGO='"+lista+"')";break;
			case 3:  
				sql="SELECT CODIGO,DESCLARGA,1,0 FROM P_PRODUCTO WHERE CODIGO=''";break;	
			default:  
				sql="SELECT P_BONLIST.PRODUCTO, P_PRODUCTO.DESCLARGA, P_BONLIST.CANT, P_BONLIST.CANTMIN " +
					"FROM P_PRODUCTO INNER JOIN P_BONLIST ON P_PRODUCTO.CODIGO = P_BONLIST.PRODUCTO " +
					"WHERE (P_BONLIST.CODIGO='"+lista+"') ORDER BY P_PRODUCTO.DESCLARGA";break;
			}
			
			DT=Con.OpenDT(sql);
			//mu.msgbox("Regs. "+DT.getCount());
			
			DT.moveToFirst();
			
			while (!DT.isAfterLast()) {
				  
			  item = clsCls.new clsBonifProd();
			  
			  item.id=DT.getString(0);
			  item.nombre=DT.getString(1);
			  
			  bcant=DT.getDouble(2)*valor;
			  if (tipolista==2) bcant=DT.getDouble(3)*mul;
			  item.cant=bcant;
			  item.cantmin=DT.getDouble(3)*mul;
			  
			  item.precio=prc.precio( item.id,bcant,nivel,um);
			  item.prstr=mu.frmdec(item.precio);
			  item.costo=prc.costo;
			  
			  if (rutatipo.equalsIgnoreCase("V")) {
				  ddisp=getDisp(item.id);
				  item.disp=ddisp;
				  if (ddisp>=bcant) item.flag=1;else item.flag=0;
			  } else {
				  item.flag=1;					
			  }
			  
			  items.add(item);	
			 
			  DT.moveToNext();
			}
			
		} catch (Exception e) {
			mu.msgbox(e.getMessage()+"\n"+sql);
		}	
		
		adapter=new ListAdaptBonif(this,items);
		listView.setAdapter(adapter);
		
		showStat();
	}
	
	private void applyBon() {
		clsBonifProd item;
		double cant,disp,diff;

		for (int i = 0; i <items.size(); i++) {

			item=items.get(i);		
			cant=item.cant;

			if (cant>0) {
				if (item.flag==0) {
					disp=item.disp;	
					diff=cant-disp;

					addFalt(item.id,diff);

					s="Se registro faltante :\n"+mu.frmdecno(diff)+" - "+item.nombre;
					toast(s);

				} else {	
					addItem(item);	
				}				
			}
		}		
	}
	
	private void addItem(clsBonifProd item) {
		Cursor DT;
		int iidx;
		
		try {
			sql="SELECT MAX(ITEM) FROM T_BONITEM";
			DT=Con.OpenDT(sql);
			DT.moveToFirst();
			iidx=DT.getInt(0);
		} catch (Exception e) {
			iidx=0;
		}	
		iidx++;
		
		
		try {
			ins.init("T_BONITEM");
			
			ins.add("ITEM",iidx);
			ins.add("PRODID",bonprodid);
			ins.add("BONIID",item.id);
			ins.add("CANT",item.cant);
			ins.add("PRECIO",item.precio);
			ins.add("COSTO",item.costo);
				
	    	db.execSQL(ins.sql());
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}		
	}
	
	private void addFalt(String prid,double prcant) {
			
		try {
			ins.init("T_BONIFFALT");
			
			ins.add("PRODID",bonprodid);
			ins.add("PRODUCTO",prid);
			ins.add("CANT",prcant);
				
	    	db.execSQL(ins.sql());
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}	
		
	}
	
	private void showStat() {
		double btot=0,bmon=0,afalt,margval;
			
		if (tipolista<2) return;
		
		for (int i = 0; i <items.size(); i++) {
			btot+=items.get(i).cant;
			bmon+=items.get(i).cant*items.get(i).precio;
		}	
		
		imgComp.setVisibility(View.INVISIBLE);completo=false;
		
		if (poruni==1) {
			falt=valor-btot;
			lblMonto.setText(mu.frmdecno(btot));
			lblFalta.setText(mu.frmdecno(falt));	
			
			if (falt==0) {
				imgComp.setVisibility(View.VISIBLE);completo=true;
			}
			
		} else {
			
			falt=valor-bmon;
			lblMonto.setText(mu.frmdec(bmon));
			lblFalta.setText(mu.frmdec(falt));	
			
			afalt=Math.abs(falt);
			margval=valor*marg;
			
			if (afalt<=margval) {
				imgComp.setVisibility(View.VISIBLE);completo=true;	
			}
			
		}
	
	}
	
	private void processNextScreen() {
		if (tipolista<2) {
			nextItem();	
		} else {
			showStat();
			if (completo) {
				nextItem();	
			} else {
				mu.msgbox("Bonificación incompleta.");
			}
		}			
	}
	
	private void processItem() {
		clsClasses.clsBonifProd item;
		String pid,pname;
		double bcant,ddisp;
		
		pid=gl.gstr;
		pname=gl.pprodname;
		
		if (mu.emptystr(pid)) return;

		item = clsCls.new clsBonifProd();

		item.id=pid;
		item.nombre=pname;

		bcant=0;
		item.cant=bcant;
		item.cantmin=0;

		item.precio=prc.precio( item.id,1,nivel,um);
		item.prstr=mu.frmdec(item.precio);
		item.costo=prc.costo;

		if (rutatipo.equalsIgnoreCase("V")) {
			ddisp=getDisp(item.id);
			item.disp=ddisp;
			if (ddisp>0) item.flag=1;else item.flag=0;
		} else {
			item.flag=1;					
		}

		items.add(item);	

		adapter.notifyDataSetChanged();
	}
	
	
	// Aux
	
	private void setVarCant() {
		double val,ffalt,ppr,valmx,valmi;
		final double cmin,cmax;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		valmi=selitem.cantmin;
		val=selitem.disp;
		ffalt=falt+selitem.cant;
		if (val>ffalt) valmx=ffalt;else valmx=val;
		
		if (poruni==0) {
			
			ppr=selitem.precio;if (ppr<=0) ppr=1;
			
			val=valmi/ppr;
			valmi=Math.floor(val);
			
			val=valmx/ppr;
			valmx=Math.floor(val);
		}
		
		
		cmin=valmi;
		cmax=valmx;
		
		
		alert.setTitle("Cantidad");
		alert.setMessage("Entre : "+mu.frmdecno(cmin)+" y "+mu.frmdecno(cmax));
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText(mu.frmdecno(selitem.cant));
		input.requestFocus();
		
		
		alert.setNegativeButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String s;
				
				try {
			    	s=input.getText().toString();
			    	icant=Double.parseDouble(s);
			    	
			    	if (icant<cmin) throw new Exception();
			    	if (icant>cmax) throw new Exception();
			    	
			    	inputCant();
				} catch (Exception e) {
					mu.msgbox("Cantidad incorrecta");return;
			    }
			
		  	}
		});

		alert.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		if (cmax>=cmin) {
			alert.show();	
		} else {
			mu.msgbox("No hay existencia para aplicar");
		}
	}
	
	private void setVarAllCant() {
		double val,ffalt,ppr,valmx,valmi;
		final double cmin,cmax;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		valmi=selitem.cantmin;
		val=selitem.disp;
		ffalt=falt+selitem.cant;
		if (val>ffalt) valmx=ffalt;else valmx=val;
		
		if (poruni==0) {
			
			ppr=selitem.precio;if (ppr<=0) ppr=1;
			
			val=valmi/ppr;
			valmi=Math.floor(val);
			
			val=valmx/ppr;
			valmx=Math.floor(val);
		}
		
		cmin=valmi;
		cmax=valmx;
		
		alert.setTitle("Cantidad");
		alert.setMessage("Entre : "+mu.frmdecno(cmin)+" y "+mu.frmdecno(cmax));
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
		input.setText(mu.frmdecno(selitem.cant));
		input.requestFocus();
		
		alert.setNegativeButton("Aplicar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String s;
				
				try {
			    	s=input.getText().toString();
			    	icant=Double.parseDouble(s);
			    	
			    	if (icant<cmin) throw new Exception();
			    	if (icant>cmax) throw new Exception();
			    	
			    	inputCant();
				} catch (Exception e) {
					mu.msgbox("Cantidad incorrecta");return;
			    }
			
		  	}
		});

		alert.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		
		alert.setNeutralButton("Borrar", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				try {
					items.remove(selpos);
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
				}
			}
		});
		

		if (cmax>=cmin) {
			alert.show();	
		} else {
			mu.msgbox("No hay existencia para aplicar");
		}
	}
	
	private void inputCant() {
		selitem.cant=icant;
		
		adapter.notifyDataSetChanged();
		showStat();
	}
	
	private void setItem() {
		
		try {
			item=gl.bonus.get(pos);
			
			if (gl.bonus.get(pos).porcant.equalsIgnoreCase("S")) poruni=1;else poruni=0;			
			
			showBonList();
		} catch (Exception e) {
		}
		
		relMonto.setVisibility(View.GONE);dectipo=-1;
	
		if (tipolista>=2) {
			relMonto.setVisibility(View.VISIBLE);dectipo=0; 
		}
		
		if (poruni==0) {
			relMonto.setVisibility(View.VISIBLE);dectipo=1; 
		} else {
			lblMarg.setText(" ");
		}
	}
	
	private void nextItem() {
		
		applyBon();
		
		if (pos==mpos-1) {
			super.finish();
			return;
		}
		
		pos++;
		setItem();
	}
	
	private void msgAskDel(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);

		dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			      	
				try {
					items.remove(selpos);
					adapter.notifyDataSetChanged();
				} catch (Exception e) {
					mu.msgbox(e.getMessage());
				}
			}
		});

		dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {			      	
				;
			}
		});

		dialog.show();

	}	
	
	private double getDisp(String prodid) {
		Cursor DT;
		double sdisp,bcant,bpcant;
		
		if (rutatipo.equalsIgnoreCase("V")) {
			sql="SELECT SUM(CANT) FROM P_STOCK WHERE CODIGO='"+prodid+"'";	
		} else {
			sql="SELECT CANT FROM P_STOCKINV WHERE CODIGO='"+prodid+"'";	
		}
		
		try {
	       	DT=Con.OpenDT(sql);
       		DT.moveToFirst();
  			sdisp=DT.getDouble(0);
		} catch (Exception e) {
			sdisp=0;
	    }	
		
		
		if (prodid.equalsIgnoreCase(bonprodid)) bpcant=bonprodcant; else bpcant=0;
			
		// Total en lista de bonoficaciones
		
		bcant=0;
		
		return sdisp-bcant-bpcant;
	}
	
	private double getMargin() {
		Cursor DT;
		double sdisp;
			
		try {
			sql="SELECT BONVOLTOL FROM P_EMPRESA";
	       	DT=Con.OpenDT(sql);
       		DT.moveToFirst();
  			sdisp=DT.getDouble(0);
		} catch (Exception e) {
			sdisp=25;
	    }	
		
		sdisp=sdisp/100;
		
		return sdisp;
	}
	
	private void toastcnt(String msg) {
		
		if (mu.emptystr(msg)) return;
		
		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);  
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}	
	
	private void clearByProd() {
		
		try {
			sql="DELETE FROM T_BONITEM WHERE PRODID='"+bonprodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}		
		
		try {
			sql="DELETE FROM T_BONIFFALT WHERE PRODID='"+bonprodid+"'";
			db.execSQL(sql);
		} catch (SQLException e) {
			mu.msgbox("Error : " + e.getMessage());
		}
			
	}
	
	protected void showkeyb(){
		if (keyboard != null) keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	
	
	// Activity Events

	@Override
	protected void onResume() {
	    super.onResume();
	    
	    if (browse==1) {
	    	browse=0;
	    	processItem();return;
	    }

	}
	
	@Override
	public void onBackPressed() {
		processNextScreen();
	}	
	
	
}
