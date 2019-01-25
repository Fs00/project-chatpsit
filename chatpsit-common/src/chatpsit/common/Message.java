package chatpsit.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Message
{
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

    /**
     * Tipo del messaggio
     */
    private Type type;
    /**
     * Campi del messaggio
     */
    private Map<String, String> fields;

    public Type getType()
    {
        return type;
    }

    public Map<String, String> getFields()
    {
        return fields;
    }

    /**
     * Il costruttore controlla che fields abbia tutti i campi specificati
     * in messageTypeFields per il tipo passato
     * @param type il tipo di messaggio
     * @param fields una Map contenente i campi del messaggio
     */
    public Message(Type type, Map<String, String> fields)
    {
        String[] typeFields = messageTypeFields.get(type);
        if (typeFields.length != fields.size())
            throw new IllegalArgumentException("Il numero di campi del messaggio non è conforme al suo tipo.");

        for (int i = 0; i < typeFields.length; i++)
        {
            if (!fields.containsKey(typeFields[i]))
                throw new IllegalArgumentException("È presente un campo del messaggio non conforme al suo tipo.");
        }

        this.fields = fields;
        this.type = type;
    }

    /**
     * Crea una stringa contenente i dati dell'istanza corrente per poterli trasmettere
     */
    public String serialize()
    {
        String message = messageTypeStrings.get(type);
        String[] typeFields = messageTypeFields.get(type);

        for (int i = 0; i < typeFields.length; i++)
            message += DELIMITER_CHAR + fields.get(typeFields[i]);

        return message;
    }

    /**
     * Crea una nuova istanza della classe partendo da una stringa trasmessa
     * @param rawMessage stringa prodotta dal metodo serialize()
     */
    public static Message deserialize(String rawMessage)
    {
        String[] rawMessageFields = rawMessage.split(Character.toString(DELIMITER_CHAR));
        Type rawMessageType = getMessageTypeFromString(rawMessageFields[0]);
        if (rawMessageType == null)
            throw new IllegalArgumentException("Il tipo di messaggio " + rawMessageFields[0] + " non esiste.");

        // Ottengo i nomi dei campi corrispondenti al tipo di messaggio dato
        String[] rawMessageTypeFieldNames = messageTypeFields.get(rawMessageType);
        // Si ricorda che rawMessageFields ha un campo in più rispetto all'array in messageTypeFields, che è il tipo di messaggio
        if (rawMessageTypeFieldNames.length == (rawMessageFields.length - 1))
        {
            Map<String, String> messageFieldsMap = new HashMap<>();
            for (int i = 0; i < rawMessageTypeFieldNames.length; i++)
                messageFieldsMap.put(rawMessageTypeFieldNames[i], rawMessageFields[i+1]);

            return new Message(rawMessageType, messageFieldsMap);
        }
        else
            throw new IllegalArgumentException("Il numero di campi del messaggio non è conforme al suo tipo.");
    }

    private static char DELIMITER_CHAR = 31;    // ASCII unit separator
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
        messageTypeFields.put(Type.Register, new String[] {"username", "password", "displayName"});
        messageTypeFields.put(Type.Logout, new String[] {"username"});
        messageTypeFields.put(Type.NotifySuccess, new String[] {"description"});    // valutare se rimuovere descrizione (superflua)
        messageTypeFields.put(Type.NotifyError, new String[]{ "description" });
        messageTypeFields.put(Type.LogEvent, new String[] { "text" });
        messageTypeFields.put(Type.UserConnected, new String[] {"username"});
        messageTypeFields.put(Type.UserDisconnected, new String[] {"username"});
        // "blocca" la map in modo che non sia più modificabile
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
     * Crea una nuova Map partendo da una lista di coppie chiave-valore sotto forma di stringa
     * @param pairs numero variabile di stringhe (deve essere pari)
     */
    public static Map<String, String> createFieldsMap(String... pairs)
    {
        if (pairs.length % 2 != 0)
            throw new IllegalArgumentException("Non c'è un valore corrispondente ad ogni campo, il numero di " +
                    "parametri è dispari.");

        Map<String, String> fieldsMap = new HashMap<>();
        for (int i = 0; i < pairs.length; i=i+2)
            fieldsMap.put(pairs[i], pairs[i+1]);

        return fieldsMap;
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