package com.dts.roadp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Repesaje extends PBase {

    private ListView listView;
    private TextView lblPrec,lblCant,lblPeso,lblOPeso,lblOCant;
    private EditText txtPeso,txtBol,txtCan;

    private AppMethods app;
    private Precio prc;

    private ArrayList<clsClasses.clsRepes> ritems= new ArrayList<clsClasses.clsRepes>();
    private ListAdaptRepes adapter;

    private String prodid;
    private int ival,tcant;
    private double dpeso,dcan,tpeso,ocant,opeso,ttotal, tprecio, tcantidad;
    private boolean esbarra;

    // Calculator
    private ArrayList<HashMap<String,Object>> items;
    private PackageManager pm ;
    private List<PackageInfo> packs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repesaje);

        super.InitBase();
        addlog("Repesaje",""+du.getActDateTime(),gl.vend);

        listView = (ListView) findViewById(R.id.listView1);

        txtPeso= (EditText) findViewById(R.id.editText);
        txtBol= (EditText) findViewById(R.id.editText4);
        txtCan= (EditText) findViewById(R.id.editText5);

        lblPrec= (TextView) findViewById(R.id.textView50);
        lblCant= (TextView) findViewById(R.id.textView53);
        lblPeso= (TextView) findViewById(R.id.textView55);
        lblOPeso= (TextView) findViewById(R.id.textView59);
        lblOCant= (TextView) findViewById(R.id.textView60);

        prepareCalculator();

        prodid=gl.gstr;

        app = new AppMethods(this, gl, Con, db);
        esbarra=app.prodBarra(prodid);

        prc=new Precio(this,mu,gl.peDec);

        setHandlers();

        clearItem();
        ritems.clear();
        loadItem();

        // 1.92 .. 3.0
    }


    //region Events

    public void doSave(View view) {
        try{
            if (checkItem()) saveItem();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void doDelete(View view) {
        try{
            deleteItem();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    public void doApply(View view) {
        totales();

        if (esbarra) {
            if (ocant!=tcant) {
                msgbox("La cantidad de bolsas incorrecta");return;
            }
        }

        try{
            if (!checkLimits()) return;

            if (opeso==tpeso) {
                msgbox("No se puede aplicar repesaje.\nPeso total iqual al peso original.");return;
            }

            if (tcant>0) msgAskApply("Seguro de cambiar peso de "+mu.frmdecimal(opeso, gl.peDecImp)+" a "+mu.frmdecimal(tpeso, gl.peDecImp));
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
        totales();

    }

    public void doCalc(View view) {
        try{
            openCalculator();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void setHandlers() {

        try{listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object lvObj = listView.getItemAtPosition(position);
                    clsClasses.clsRepes item = (clsClasses.clsRepes) lvObj;

                    selid = item.id;
                    selidx = 0;
                    adapter.setSelectedIndex(position);
                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    mu.msgbox(e.getMessage());
                }
            }
        });

            txtPeso.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                txtBol.requestFocus();
                                return true;
                        }
                    }
                    return false;
                }
            });

            txtBol.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                txtBol.requestFocus();
                                return true;
                        }
                    }
                    return false;
                }
            });

            txtCan.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                if (checkItem()) saveItem();
                                return true;
                        }
                    }
                    return false;
                }
            });

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Main

    private void listItems() {
        try{
            adapter=new ListAdaptRepes(this, ritems);
            listView.setAdapter(adapter);

            totales();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }
    }

    private void loadItem() {
        try{
            if (esbarra) {
                getPrecio();
                loadBarras();
            } else {
                loadItemSingle();
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void loadItemSingle() {
        Cursor DT;

        try {
            sql = "SELECT PESO,TOTAL,CANT, PRECIO FROM T_VENTA WHERE PRODUCTO='"+prodid+"' ";

            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            lblOCant.setText("1");
            lblOPeso.setText(mu.frmdecimal(DT.getDouble(0),gl.peDecImp));
            lblPrec.setText(mu.frmdecimal(DT.getDouble(1),2));

            opeso=DT.getDouble(0);
            ocant=DT.getDouble(2);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }
    }

    private void getPrecio() {
        Cursor DT;

        try {

            sql = "SELECT PRECIO FROM T_VENTA WHERE PRODUCTO='"+prodid+"' ";

            DT = Con.OpenDT(sql);

            if(DT.getCount()>0){
                DT.moveToFirst();
                tprecio=DT.getDouble(0);
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }
    }

    private void loadBarras() {
        Cursor DT;

        try {

            sql = "SELECT SUM(PESO),SUM(PRECIO),COUNT(BARRA), SUM(PESOORIG) FROM T_BARRA WHERE CODIGO='"+prodid+"' ";

            DT = Con.OpenDT(sql);
            DT.moveToFirst();

            lblOCant.setText(""+DT.getInt(2));
            lblOPeso.setText(mu.frmdecimal(DT.getDouble(3),gl.peDecImp));
            lblPrec.setText(mu.frmdecimal(DT.getDouble(1),2));

            opeso=DT.getDouble(3);
            ocant=DT.getDouble(2);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }
    }

    private void saveItem() {
        try{
            clsClasses.clsRepes item = clsCls.new clsRepes();

            item.id=ritems.size()+1;
            item.peso=dpeso;
            item.bol=ival;
            item.can=dcan;

            item.sid="#"+item.id;
            item.speso=mu.frmdecimal(dpeso,gl.peDecImp);
            item.sbol=" "+ival;
            item.scan=mu.frmdecimal(dcan,gl.peDecImp);
            item.stot=mu.frmdecimal(dpeso-dcan,gl.peDecImp);

            ritems.add(item);

            clearItem();
            listItems();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void clearItem() {
        try{
            txtPeso.setText("");txtPeso.requestFocus();
            if (esbarra) txtBol.setText("");else txtBol.setText("1");
            txtCan.setText("");
            selidx=-1;
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void deleteItem() {
        if (selidx<0) return;

        try {
            ritems.remove(selidx);
            clearItem();

            for (int i = 0; i <ritems.size(); i++) {
                ritems.get(i).id=i+1;
                ritems.get(i).sid="#"+ritems.get(i).id;
            }

            listItems();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    private void apply() {
        if (esbarra) applyBarra();else applySimple();
        browse=1;
    }

    private void applySimple() {
        double prec,precdoc,tot,peso;

        try {
            prec=prc.precio(prodid,ocant,gl.nivel,gl.um,gl.umpeso,tpeso,gl.um);
            precdoc=prc.precdoc;
            tot=prec*ocant;
            peso=tpeso;

            sql="UPDATE T_VENTA SET PRECIO="+prec+",PESO="+peso+",TOTAL="+tot+",PRECIODOC="+precdoc+" WHERE PRODUCTO='"+prodid+"' ";
            db.execSQL(sql);

            finish();
            toastcent("Repesaje aplicado");
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
   }

    private void applyBarra() {
        Cursor dt;
        double rf,pp0,pp,ppr,tp,dtp;
        String bar;

        try {

            rf=tpeso/opeso;rf=mu.roundr(rf,gl.peDec);tp=0;

            sql = "SELECT BARRA,PESO,CANTIDAD,PESOORIG FROM T_BARRA WHERE CODIGO='"+prodid+"' ";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            while (!dt.isAfterLast()) {
                bar=dt.getString(0);
                pp0=dt.getDouble(3);pp=pp0*rf;ppr=mu.roundr(pp,gl.peDec);
                tcantidad=dt.getDouble(2);
                tp+=ppr;

                if (prodPorPeso(prodid)) ttotal=mu.round2(tprecio*ppr);
                else ttotal=tprecio*tcantidad;

                sql="UPDATE T_BARRA SET PESO="+ppr+", PRECIO="+ttotal+" WHERE CODIGO='"+prodid+"' AND BARRA='"+bar+"'";
                db.execSQL(sql);

                dt.moveToNext();
            }

            tp=mu.roundr(tp,gl.peDec+2);
            dtp=tpeso-tp;
            dtp=mu.roundr(dtp,gl.peDec+2);// diferencia por redondeo

            sql = "SELECT BARRA,PESO,CANTIDAD FROM T_BARRA WHERE CODIGO='"+prodid+"' ";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();
            bar=dt.getString(0);
            pp=dt.getDouble(1);
            tcantidad=dt.getDouble(2);

            // agregar la diferencia a la primera barra
            sql="UPDATE T_BARRA SET PESO=PESO+"+dtp+", PRECIO="+ttotal+" WHERE CODIGO='"+prodid+"' AND BARRA='"+bar+"'";
            db.execSQL(sql);

           //Actualizar totales
            sql = "SELECT CANT FROM T_VENTA WHERE PRODUCTO='"+prodid+"' ";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();
            tcantidad=dt.getDouble(0);

           if (prodPorPeso(prodid)) ttotal=mu.round2(tprecio*tpeso);
           else ttotal=tprecio*tcantidad;

           lblPrec.setText(mu.frmdecimal(ttotal,2));

            // actualizar peso en T_VENTA
            sql="UPDATE T_VENTA SET TOTAL="+ttotal+",PESO="+tpeso+" WHERE PRODUCTO='"+prodid+"'";
            db.execSQL(sql);

            finish();
            toastcent("Repesaje aplicado");
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            mu.msgbox(e.getMessage());
        }
    }


    private boolean prodPorPeso(String prodid) {
        try {
            return app.ventaPeso(prodid);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            return false;
        }
    }

    //endregion

    //region Calculator

    private void prepareCalculator() {
        try{
            items =new  ArrayList<HashMap<String,Object>>();
            Handler mtimer = new Handler();
            Runnable mrunner=new Runnable() {
                @Override
                public void run() {
                    listPackages();
                }
            };
            mtimer.postDelayed(mrunner,500);
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void listPackages() {
        try{
            pm = getPackageManager();
            packs = pm.getInstalledPackages(0);
            for (PackageInfo pi : packs) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("appName", pi.applicationInfo.loadLabel(pm));
                map.put("packageName", pi.packageName);
                items.add(map);
            }
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    private void openCalculator() {
        int d = 0;

        try {
            if (items.size() >= 1) {
                int j = 0;
                for (j = 0; j < items.size(); j++) {
                    String AppName = (String) items.get(j).get("appName");
                    if (AppName.matches("Calculator")) {
                        d = j;
                        break;
                    }
                }
                String packageName = (String) items.get(d).get("packageName");

                Intent itt = pm.getLaunchIntentForPackage(packageName);
                if (itt != null) {
                    startActivity(itt);
                } else {
                    toast("No se puede abrir calculadora");
                }
            } else {
                toast("No se puede abrir calculadora");
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    //endregion

    //region Aux

    private boolean checkItem() {
        String ss;

        totales();

        try {
            ss = txtPeso.getText().toString();
            dpeso=Double.parseDouble(ss);
            if (dpeso<=0) throw new Exception();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Peso incorrecto.");txtPeso.requestFocus();return false;
        }

        try {
            ss = txtBol.getText().toString();
            ival=Integer.parseInt(ss);
            if (ival<=0) throw new Exception();

            if (ival+tcant>ocant) {
                msgbox("Cantidad de bolsas mayor que cantidad de bolsas escaneadas.");txtBol.requestFocus();return false;
            }
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Cantidad incorrecta.");txtBol.requestFocus();return false;
        }

        try {
            ss = txtCan.getText().toString();
            if (mu.emptystr(ss)) ss="0";
            dcan=Double.parseDouble(ss);
            if (dcan<0) throw new Exception();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Descuento canastas incorrecto.");txtCan.requestFocus();return false;
        }

        return true;
    }

    private void msgAskExit(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Repesaje");
            dialog.setMessage(msg  + " ?");
            dialog.setIcon(R.drawable.ic_quest);

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private void totales() {
        try{
            tcant=0;tpeso=0;

            for (int i = 0; i <ritems.size(); i++) {
                tpeso+=ritems.get(i).peso;
                tcant+=ritems.get(i).bol;
            }

            lblCant.setText("" + tcant);
            lblPeso.setText(mu.frmdecimal(tpeso, gl.peDecImp));
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    private boolean checkLimits() {
        Cursor dt;
        double pmin,pmax;
        String ss;

        try {
            sql="SELECT PORCMINIMO,PORCMAXIMO FROM P_PORCMERMA WHERE PRODUCTO='"+prodid+"'";
            dt=Con.OpenDT(sql);

            if (dt.getCount() == 0) {
                msgbox("El repesaje no se puede aplicar,\n no esta definido rango de repesaje para el producto.");return false;
            }
            dt.moveToFirst();

            pmin = opeso - dt.getDouble(0) * opeso / 100;
            pmax = opeso + dt.getDouble(1) * opeso / 100;

            if (tpeso<pmin) {
                ss="El repesaje ("+mu.frmdecimal(tpeso, gl.peDecImp)+") está por debajo de los percentajes permitidos," +
                        " minimo : "+mu.frmdecimal(pmin, gl.peDecImp)+", no se puede aplicar.";
                msgbox(ss);return false;
            }

            if (tpeso>pmax) {
                ss="El repesaje ("+mu.frmdecimal(tpeso, gl.peDecImp)+") está por debajo de los percentajes permitidos," +
                        " máximo : "+mu.frmdecimal(pmax, gl.peDecImp)+", no se puede aplicar.";
                msgbox(ss);return false;
            }

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),sql);
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

        return true;
    }

    //endregion

    //region Messages

    private void msgAskApply(String msg) {
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setTitle("Title");
            dialog.setMessage("¿" + msg + "?");

            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    apply();
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }


    }

    //endregion

    //region Activity events

    @Override
    public void onBackPressed() {
        try{
            msgAskExit("Salir sin aplicar repesaje");
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
        }

    }

    //endregion

}
