package chatpsit.server;

import chatpsit.common.Message;
import chatpsit.common.ServerMode;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    // Utilizzato per creare una stringa partendo dalla data attuale
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");

    private static ServerMode mode = ServerMode.Local;
    private static Server server;
    private static boolean logOnFile = false;
    private static QueueFileWriter asyncLogFileWriter;

    /**
     * Modifica la modalità di logging (locale o remota)
     * @param mode la modalità scelta
     */
    public static void setMode(ServerMode mode, Server server)
    {
        Logger.mode = mode;
        Logger.server = server;

        if (mode == ServerMode.Remote)
            startLoggingOnFile();
    }

    public static void startLoggingOnFile()
    {
        try
        {
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            asyncLogFileWriter = new QueueFileWriter(Paths.get(System.getProperty("user.dir"), "server_" + dateTime + ".log"));
            logOnFile = true;
        }
        catch (Exception exc)
        {
            logEvent(EventType.Error, "Impossibile aprire il file di log per la scrittura: " + exc.getMessage());
        }
    }

    public static void closeLogFile()
    {
        if (asyncLogFileWriter != null)
            asyncLogFileWriter.stopProcessingAndClose();

        logOnFile = false;
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

        if (mode == ServerMode.Local)
            System.out.println(logString);
        else if (server != null)
            server.sendToAdminPanelsOnly(new Message(Message.Type.LogEvent, Message.createFieldsMap("text", logString)));

        if (logOnFile)
            asyncLogFileWriter.appendText(logString);
    }

    // Impedisce che il costruttore possa essere usato all'esterno
    private Logger() {}
}
