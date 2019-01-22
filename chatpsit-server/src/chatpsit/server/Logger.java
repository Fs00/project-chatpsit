package chatpsit.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Classe contenente funzioni di logging
 * Può operare in modalità locale, stampando i messaggi di log a video (comportamento di default),
 * oppure in modalità remota per l'esecuzione sul cloud, salvando quindi i messaggi su un file di testo e inviandoli
 * al client del pannello di amministrazione.
 */
public final class Logger
{
    public enum EventType {
        Info,
        Warning,
        Error
    }

    // Funzioni utilizzate per il logging
    private final static Consumer<String> localLoggingFunction = System.out::println;
    private final static Consumer<String> remoteLoggingFunction = Logger::performRemoteLogging;
    // Utilizzato per creare una stringa partendo dalla data attuale
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");

    private static Server.Mode mode = Server.Mode.Local;
    private static Server server;

    /**
     * Modifica la modalità di logging (locale o remota)
     * @param mode la modalità scelta
     */
    public static void setMode(Server.Mode mode, Server server)
    {
        Logger.mode = mode;
        Logger.server = server;
    }

    /**
     * Stampa a video o registra a seconda della modalità del server il messaggio passatogli
     * @param type Tipo dell'evento (info, warning, errore)
     * @param message Messaggio di log
     */
    public static void logEvent(EventType type, String message)
    {
        String logString = dateFormatter.format(new Date());
        switch (type)
        {
            case Info:
                logString += " INFO: ";
                break;
            case Error:
                logString += " ERR: ";
                break;
            case Warning:
                logString += " WARN: ";
                break;
        }
        logString += message;

        if (mode == Server.Mode.Local)
            localLoggingFunction.accept(logString);
        else
            remoteLoggingFunction.accept(logString);
    }

    /**
     * Accoda la stringa di log in un buffer di memoria e, se il pannello di amministrazione è connesso,
     * la invia al relativo socket.
     * @param message Stringa di logging preparata da logEvent
     */
    private static void performRemoteLogging(String message)
    {
        // TODO scrittura su file di log
        // TODO invio agli admin panel
    }

    // Impedisce che il costruttore possa essere usato all'esterno
    private Logger() {}
}
