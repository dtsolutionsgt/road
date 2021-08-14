package com.dts.roadp;

public class clsClasses {
	
	public class clsCD {
		public String Cod,Desc,Text,um;
		public boolean bandera,disp;
	}
	
	public class clsCDB {
		public String Cod,Desc,Adds;
		public int Bandera,Cobro,ppend, prefacturas;
		public double valor,coorx,coory;
	}

    //JP20210614
	public class clsCFDV {
		public int id,flag,bandera;
		public String Cod,Desc,Fecha,Valor,Sid;
		public double val;
	}
	
	public class clsExist {
		public String Cod,Desc,Fecha,Valor,ValorM,ValorT,Peso,PesoM,PesoT,Lote,Doc,Centro,Stat;
		public double cant,cantm;
		public int id,flag,items;
	}

	public class clsDevCan {
		public String Cod,Desc,Fecha,Valor,ValorM,ValorT,Peso,PesoM,PesoT,Lote,Doc,Centro,Stat;
		public double cant,cantm;
		public int id,flag,items;
	}

	public class clsMenu {
		public int ID,Icon;
		public String Name;
	}	
	
	public class clsVenta {
		public String Cod,Nombre,um,val,valp,sdesc,ums;
		public double Cant,Peso,Prec,Desc,Total,imp,percep, precio,factor;

	}
	
	public class clsPromoItem {
		public String Prod,Nombre,Bon,Tipo;
		public double RIni,RFin,Valor;
		public boolean Porrango,Porprod,Porcant;
	}
	
	public class clsObj {
		public String Nombre,Cod,Meta,Acum,Falta,Perc;
	}
	
	public class clsDepos {
		public String Nombre,Cod,Tipo,Banco;
		public double Valor,Total,Efect,Chec;
		public int Bandera,NChec;
	}
	
	public class clsCobro {
		public String Factura,Tipo,fini,ffin;
		public double Valor,Saldo,Pago;
		public int id,flag;
	}
	
	public class clsPago {
		public String Tipo,Num,Valor;
		public int id;
	}
	
	public class clsEnvio {
		public String Nombre;
		public int id,env,pend;
	}
	
	public class clsBonifItem {
		public String prodid,lista,cantexact,globbon,tipocant,porcant;
		public int tipolista;
		public double valor,mul,monto;
	}	
	
	public class clsBonifProd {
		public String id,nombre,prstr;
		public int flag;
		public double cant,cantmin,disp,precio,costo;
	}	
	
	public class clsDemoDlg {
		public int tipo,flag;
		public String Cod,Desc;
		public double val;
	}

	//#HS_20181121_1546 Se agregó clsFinDia y los campos de la tabla FinDia.
	public class clsFinDiaItems {
		public int id, corel, val1, val2, val3, val4, val5, val6, val7, val8;
	}

	public class clsRepes {
		public int id,bol;
		public double peso,can;
		public String sid,speso,sbol,scan,stot;
	}

	public class  clsAyudante{
		public String idayudante;
		public String nombreayudante;
	}

	public class clsVehiculo{
		public String idVehiculo,marca,placa;
	}

	public class clsBarras{
		public String barra,peso;
	}

	public class clsDs_pedido {
		public String corel;
		public String anulado;
		public long fecha;
		public String empresa;
		public String ruta;
		public String vendedor;
		public String cliente;
		public double kilometraje;
		public long fechaentr;
		public String direntrega;
		public double total;
		public double desmonto;
		public double impmonto;
		public double peso;
		public String bandera;
		public String statcom;
		public String calcobj;
		public int  impres;
		public String add1;
		public String add2;
		public String add3;
	}

	public class clsDs_pedidod {
		public String corel;
		public String producto;
		public String empresa;
		public String anulado;
		public double cant;
		public double precio;
		public double imp;
		public double des;
		public double desmon;
		public double total;
		public double preciodoc;
		public double peso;
		public double val1;
		public String val2;
		public String ruta;
		public String umventa;
		public String umstock;
		public String umpeso;
	}


	public class clsTipoCanastas {
		public String codigo, desccorta, desclarga;
		public int tenregado, trecibido;
		public Boolean totales=true;
	}

	public class clsCanasta {
		public String ruta, cliente, producto;
		public int idCanasta, fecha, cantrec, cantentr;
		public String codigo, desccorta, desclarga, fechaFormato;
		public boolean editar;
	}
}
