package chatpsit.common;

public class ServerConstants
{
    public final static int SERVER_PORT = 7777;
    public final static String REMOTE_SERVER_ADDRESS = ""; // TODO
    public final static String LOCAL_SERVER_ADDRESS = "localhost";

    public final static String ALREADY_LOGGED_IN_MSG_ERR = "È ancora attiva una sessione effettuata da questo utente. " +
                                                           "Effettua il logout e riprova.";
    public final static String WRONG_CREDENTIALS_ERR = "Le credenziali sono errate.";
    public final static String ALREADY_REGISTERED_ERR = "È già registrato un utente con questo nome.";
    public final static String WRONG_USERNAME_FORMAT_ERR = "Il nome utente contiene caratteri non permessi. " +
                               "Assicurati che non siano presenti spazi, punti esclamativi o chiocciole.";
    public final static String ONLY_ADMIN_CAN_ERR = "Solo gli admin possono accedere al pannello di amministrazione.";

    private ServerConstants() {}
}
