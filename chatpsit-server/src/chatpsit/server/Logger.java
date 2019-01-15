package chatpsit.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class Logger
{
    private static Server.Mode mode;
    private static Consumer<String> loggingFunction;
    private static SimpleDateFormat dateFormatter;

    public static void initialize(Server.Mode mode)
    {
        Logger.mode = mode;
        dateFormatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");

        if (mode == Server.Mode.Local)
            loggingFunction = System.out::println;
        else
            loggingFunction = Logger::appendMessageToFile;
    }

    public static void logEvent(LogEventType type, String message)
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
        loggingFunction.accept(logString);
    }

    private static void appendMessageToFile(String message)
    {
        // TODO
    }

    // prevents the creation of new instances from outside
    private Logger() {}
}
