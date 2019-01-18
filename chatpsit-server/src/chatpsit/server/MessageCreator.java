package chatpsit.server;

/**
 * Classe adibita alla creazione dei messaggi in linea con il protocollo METTIU
 */
public class MessageCreator {

    public static String notifySuccess()
    {
        return "OK";
    }

    public static String notifyError()
    {
        return "ERROR";
    }

    /**
     * Crea la stringa che notificherà al client l'arrivo di un nuovo messaggio
     * @param message il messaggio da inviare
     * @param sender utente che ha inviato il messaggio
     * @param type tipo di messaggio (global -> messaggio rivolto alla chat globale; private -> messaggio rivolto
     * ad un singolo utente)
     * @return
     */
    public String notifyNewMsg(String message, String sender, String type)
    {
        return "NWMSG;"+sender+";"+message+";"+type;
    }
}
