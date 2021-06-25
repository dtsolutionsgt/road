package com.dts.roadp;

import android.content.Intent;

public class srvEnvPedido extends srvBase {

    private wsEnvPedido wscom;

    private String command,corel;

    @Override
    public void execute() {
        wscom =new wsEnvPedido(URL);
        wscom.execute(command,corel);
    }

    @Override
    public void loadParams(Intent intent) {
        command = intent.getStringExtra("command");
        corel = intent.getStringExtra("correlativo");
    }

}
