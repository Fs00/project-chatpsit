package chatpsit.common;

import com.sun.istack.internal.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Rappresenta un messaggio formattato secondo il protocollo dell'applicazione.
 * Per la creazione di nuovi messaggi viene usato il builder pattern.
 * Ogni messaggio è composto da un tipo, un insieme di campi e un flag che indica se dopo
 * l'invio del messaggio la connessione verrà chiusa.
 * La classe contiene inoltre le definizioni dei vari tipi di messaggio e dei loro campi.
 */
public class Message
{
    /**
     * Fornisce metodi per creare in maniera incrementale un nuovo oggetto Message.
     * La validazione degli attributi avviene nel costruttore di Message
     */
    public static class Builder
    {
        private Type messageType;
        private Map<String, String> messageFields = new HashMap<>();
        private boolean isLastMessage = false;

        private Builder(Type type)
        {
            messageType = type;
        }

        public Builder field(String name, String value)
        {
            messageFields.put(name, value);
            return this;
        }

        public Builder lastMessage()
        {
            isLastMessage = true;
            return this;
        }

        public Message build()
        {
            return new Message(messageType, messageFields, isLastMessage);
        }
    }

    /**
     * Metodo per creare una nuova istanza del Builder dall'esterno
     * @param type il tipo del messaggio da creare
     */
    public static Builder createNew(Type type)
    {
        return new Builder(type);
    }

    /*
     * Attributi di Message
     */
    private Type type;
    private Map<String, String> fields;
    private boolean isLastMessage;

    /**
     * Il costruttore verifica la correttezza dei dati passatigli e crea una nuova istanza del messaggio.
     * NON CHIAMARE DIRETTAMENTE: utilizzare il builder con Message.createNew()
     * @param type il tipo di messaggio
     * @param fields una Map contenente i campi del messaggio
     * @param isLastMessage indica se dopo l'invio del messaggio la connessione verrà chiusa
     */
    private Message(Type type, @NotNull Map<String, String> fields, boolean isLastMessage)
    {
        if (type == null)
            throw new IllegalArgumentException("Il tipo del messaggio non può essere null.");

        String[] typeFieldNames = messageTypeFields.get(type);
        if (typeFieldNames.length != fields.size())
            throw new IllegalArgumentException("Il numero di campi del messaggio non è conforme al suo tipo.");

        for (int i = 0; i < typeFieldNames.length; i++)
        {
            if (!fields.containsKey(typeFieldNames[i]))
                throw new IllegalArgumentException("È presente un campo del messaggio non conforme al suo tipo.");

            if (fields.get(typeFieldNames[i]) == null)
                throw new IllegalArgumentException("Il campo " + typeFieldNames[i] + " del messaggio non è valorizzato.");
        }

        this.fields = fields;
        this.type = type;
        this.isLastMessage = isLastMessage;
    }

    public Type getType()
    {
        return type;
    }

    public boolean isLastMessage()
    {
        return isLastMessage;
    }

    public String getField(String fieldName)
    {
        return fields.get(fieldName);
    }

    /**
     * Crea una stringa contenente i dati dell'istanza corrente per poterli trasmettere.
     * I dati vengono serializzati nel seguente formato:
     *    TIPOMSG[31]campo1[31]campo2(...)([4])
     * [31] è il carattere ASCII utilizzato come delimitatore fra i campi; il carattere [4] (EOT) è presente
     * soltanto se il messaggio è l'ultimo prima della chiusura della trasmissione.
     */
    public String serialize()
    {
        String message = messageTypeStrings.get(type);
        String[] typeFieldNames = messageTypeFields.get(type);

        for (int i = 0; i < typeFieldNames.length; i++)
            message += DELIMITER_CHAR + this.fields.get(typeFieldNames[i]);

        if (isLastMessage)
            message += END_OF_TRANSMISSION_CHAR;

        return message;
    }

    /**
     * Crea una nuova istanza della classe partendo da un messaggio serializzato
     * @param rawMessage stringa prodotta dal metodo serialize()
     */
    public static Message deserialize(String rawMessage)
    {
        // Verifica se il messaggio termina la trasmissione
        boolean isLastMessage = false;
        if (rawMessage.endsWith(Character.toString(END_OF_TRANSMISSION_CHAR)))
        {
            isLastMessage = true;
            rawMessage = rawMessage.substring(0, rawMessage.length() - 1);
        }

        // Il parametro -1 indica di mantenere i campi vuoti nell'array se ci fossero due delimitatori vicini
        String[] rawMessageFields = rawMessage.split(Character.toString(DELIMITER_CHAR), -1);
        Type rawMessageType = getMessageTypeFromString(rawMessageFields[0]);
        if (rawMessageType == null)
            throw new IllegalArgumentException("Il tipo di messaggio " + rawMessageFields[0] + " non esiste.");

        // Ottengo i nomi dei campi corrispondenti al tipo di messaggio dato
        String[] rawMessageTypeFieldNames = messageTypeFields.get(rawMessageType);
        // rawMessageFields ha un campo in più rispetto all'array in messageTypeFields, che è il tipo di messaggio
        if (rawMessageTypeFieldNames.length == (rawMessageFields.length - 1))
        {
            Builder messageBuilder = Message.createNew(rawMessageType);
            for (int i = 0; i < rawMessageTypeFieldNames.length; i++)
                messageBuilder.field(rawMessageTypeFieldNames[i], rawMessageFields[i+1]);

            if (isLastMessage)
                messageBuilder.lastMessage();

            return messageBuilder.build();
        }
        else
            throw new IllegalArgumentException("Il numero di campi del messaggio non è conforme al suo tipo.");
    }

    /**
     * Definisce i tipi possibili di messaggio
     */
    public enum Type {
        UserLogin,
        AdminPanelLogin,
        PrivateMessage,
        GlobalMessage,
        Report,
        Register,
        Logout,
        NotifySuccess,
        NotifyError,
        Ban,
        LogEvent,
        UserConnected,
        UserDisconnected
    }

    // Caratteri usati nella serializzazione del messaggio
    private static char DELIMITER_CHAR = 31;
    private static char END_OF_TRANSMISSION_CHAR = 4;

    // Contiene le stringhe del protocollo corrispondenti al tipo di messaggio
    private static Map<Type, String> messageTypeStrings;
    // Contiene i nomi dei campi per ogni tipo di messaggio e il loro ordine
    private static Map<Type, String[]> messageTypeFields;
    static {
        messageTypeFields = new HashMap<>();
        messageTypeFields.put(Type.UserLogin, new String[] {"username", "password"});
        messageTypeFields.put(Type.AdminPanelLogin, new String[] {"username", "password"});
        messageTypeFields.put(Type.PrivateMessage, new String[] {"sender", "recipient", "message"});
        messageTypeFields.put(Type.GlobalMessage, new String[] {"sender", "message"});
        messageTypeFields.put(Type.Report, new String[] {"sender", "reportedUser", "reason"});
        messageTypeFields.put(Type.Ban, new String[] {"bannedUser"});
        messageTypeFields.put(Type.Register, new String[] {"username", "password"});
        messageTypeFields.put(Type.Logout, new String[] {"username"});
        messageTypeFields.put(Type.NotifySuccess, new String[] {});
        messageTypeFields.put(Type.NotifyError, new String[]{ "description" });
        messageTypeFields.put(Type.LogEvent, new String[] { "text" });
        messageTypeFields.put(Type.UserConnected, new String[] {"username"});
        messageTypeFields.put(Type.UserDisconnected, new String[] {"username"});
        // blocca la map in modo che non sia più modificabile
        messageTypeFields = Collections.unmodifiableMap(messageTypeFields);

        messageTypeStrings = new HashMap<>();
        messageTypeStrings.put(Type.UserLogin, "ULGIN");
        messageTypeStrings.put(Type.AdminPanelLogin, "ALGIN");
        messageTypeStrings.put(Type.PrivateMessage, "PRMSG");
        messageTypeStrings.put(Type.GlobalMessage, "GLMSG");
        messageTypeStrings.put(Type.Report, "REPRT");
        messageTypeStrings.put(Type.Ban, "BANHR");
        messageTypeStrings.put(Type.Register, "RGSTR");
        messageTypeStrings.put(Type.Logout, "LGOUT");
        messageTypeStrings.put(Type.NotifySuccess, "SUCSS");
        messageTypeStrings.put(Type.NotifyError, "ERROR");
        messageTypeStrings.put(Type.LogEvent, "LGEVT");
        messageTypeStrings.put(Type.UserConnected, "USRCT");
        messageTypeStrings.put(Type.UserDisconnected, "USRDC");
        messageTypeStrings = Collections.unmodifiableMap(messageTypeStrings);
    }

    /**
     * Data la rappresentazione in forma di stringa di un tipo di messaggio, restituisce il corrispondente valore
     * dell'enumerazione Message.Type
     * @param typeAsString stringa di 5 caratteri
     */
    private static Type getMessageTypeFromString(String typeAsString)
    {
        for (Map.Entry<Type, String> entry : messageTypeStrings.entrySet())
        {
            if (entry.getValue().equals(typeAsString))
                return entry.getKey();
        }
        return null;
    }
}