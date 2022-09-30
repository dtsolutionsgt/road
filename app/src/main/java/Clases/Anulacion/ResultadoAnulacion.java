package Clases.Anulacion;

public class ResultadoAnulacion {

    private String estado = "";
    private String fechaAutorizacion = "";
    private String codigoRespuestaDGI = "";
    private String mensajeRespuesta = "";

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(String fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public String getCodigoRespuestaDGI() {
        return codigoRespuestaDGI;
    }

    public void setCodigoRespuestaDGI(String codigoRespuestaDGI) {
        this.codigoRespuestaDGI = codigoRespuestaDGI;
    }

    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }

    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.mensajeRespuesta = mensajeRespuesta;
    }



}
