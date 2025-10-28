package hospital.logic;

public class Protocol {
    public static final String SERVER = "localhost";
    public static final int PORT = 1234;

    // ----------------- PACIENTE -------------------------
    public static final int PACIENTE_CREATE = 101;
    public static final int PACIENTE_READ   = 102;
    public static final int PACIENTE_UPDATE = 103;
    public static final int PACIENTE_DELETE = 104;
    public static final int PACIENTE_SEARCH = 105;

    // ----------------- MÉDICO ---------------------------
    public static final int MEDICO_CREATE = 201;
    public static final int MEDICO_READ   = 202;
    public static final int MEDICO_UPDATE = 203;
    public static final int MEDICO_DELETE = 204;
    public static final int MEDICO_SEARCH = 205;

    // ----------------- FARMACEUTA -----------------------
    public static final int FARMACEUTA_CREATE = 301;
    public static final int FARMACEUTA_READ   = 302;
    public static final int FARMACEUTA_UPDATE = 303;
    public static final int FARMACEUTA_DELETE = 304;
    public static final int FARMACEUTA_SEARCH = 305;

    // ----------------- MEDICAMENTO ----------------------
    public static final int MEDICAMENTO_CREATE = 401;
    public static final int MEDICAMENTO_READ   = 402;
    public static final int MEDICAMENTO_UPDATE = 403;
    public static final int MEDICAMENTO_DELETE = 404;
    public static final int MEDICAMENTO_SEARCH = 405;

    // ----------------- RECETA ---------------------------
    public static final int RECETA_CREATE = 501;
    public static final int RECETA_READ   = 502;
    public static final int RECETA_UPDATE = 503;
    public static final int RECETA_DELETE = 504;
    public static final int RECETA_SEARCH = 505;

    // ----------------- ADMINISTRADOR --------------------
    public static final int ADMINISTRADOR_CREATE = 601;
    public static final int ADMINISTRADOR_READ   = 602;
    public static final int ADMINISTRADOR_UPDATE = 603;
    public static final int ADMINISTRADOR_DELETE = 604;
    public static final int ADMINISTRADOR_GETALL = 605;

    //------------------ DETALLE RECETA -------------------
    public static final int DETALLE_RECETA_CREATE = 701;
    public static final int DETALLE_RECETA_UPDATE   = 702;
    public static final int DETALLE_RECETA_DELETE = 703;
    public static final int DETALLE_RECETA_GETXRECETA = 704;
    public static final int DETALLE_RECETA_GETALL = 705;

    //------------------ MÉTODOS DE AUTENTICACIÓN ---------
    public static final int AUTHENTICATE = 801;
    public static final int CHANGE_PASSWORD = 802;

    //-----------NOTIFICACIONES ASINCRONICAS---------------

    public static final int SYNC           = 900;  // solicita Session Id
    public static final int ASYNC          = 901;  // registra socket asíncrono
    public static final int DELIVER_MESSAGE = 902; // notificación

    //------------------ ERRORES --------------------------

    public static final int ERROR_NO_ERROR=0;
    public static final int ERROR_ERROR=1;

    //-----------------------------------------------------
    public static final int DISCONNECT=99;


    public static final int MEDICO_GETALL = 206;
    public static final int FARMACEUTA_GETALL = 306;
    public static final int MEDICAMENTO_GETALL = 406;
    public static final int PACIENTE_GETALL = 106;
    public static final int RECETA_GETALL = 506;
}