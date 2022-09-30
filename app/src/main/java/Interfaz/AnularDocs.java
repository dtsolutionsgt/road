package Interfaz;


import Clases.Anulacion.AnularFactura;
import Clases.Anulacion.ResultadoAnulacion;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AnularDocs {

    @POST("Emision/Eventos/API/Anulacion")
    Call<ResultadoAnulacion> AnularFactura(@Body AnularFactura data, @Header("Authorization") String Token);
}
