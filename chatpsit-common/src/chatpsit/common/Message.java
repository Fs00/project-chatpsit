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
        Login,
        PrivateMessage,
        GlobalMessage,
        Report,
        Register,
        Logout,
        NewMessage,
        NotifySuccess,
        NotifyError
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

    public Message(Type type, Map<String, String> fields)
    {

        //Assegno i parametri a campi della classe dopo aver controllato che fields abbia tutti i campi specificati
        //in messageTypeFields per il tipo passato
        switch(type)
        {
            case Login:
                if(fields.containsKey("username") && fields.containsKey("password"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case PrivateMessage:
                if(fields.containsKey("sender") && fields.containsKey("recipient") && fields.containsKey("message"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case GlobalMessage:
                if(fields.containsKey("sender") && fields.containsKey("message"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case Report:
                if(fields.containsKey("sender") && fields.containsKey("reportedUser"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case Register:
                if(fields.containsKey("username") && fields.containsKey("password") && fields.containsKey("displayName"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case Logout:
                if(fields.containsKey("username"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case NewMessage:
                if(fields.containsKey("sender") && fields.containsKey("message"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case NotifySuccess:
                if(fields.containsKey("description"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
            case NotifyError:
                if(fields.containsKey("description"))
                {
                    this.type = type;
                    this.fields = fields;
                }
                else
                {
                    throw new IllegalArgumentException("Campi non rispettati.");
                }
                break;
        }
    }

    /**
     * Crea una stringa contenente i dati dell'istanza corrente per poterli trasmettere
     */
    public String serialize()
    {
        /*
          deve formare la stringa concatenando i fields nell'ordine specificato dall'array corrispondente
          al tipo del messaggio in messageTypeFields
         */
        String message = messageTypeStrings.get(type.toString());
        for(Map.Entry<String, String> entry: fields.entrySet())
        {
            message += ";" + entry.getValue();
        }
        return message;
    }

    /**
     * Crea una nuova istanza della classe partendo da una stringa trasmessa
     * @param rawMessage stringa prodotta dal metodo serialize()
     */
    public static Message deserialize(String rawMessage)
    {
        String[] rawMessageFields = rawMessage.split(";");
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

    // Contiene le stringhe del protocollo corrispondenti al tipo di messaggio
    private static Map<Type, String> messageTypeStrings;
    // Contiene i nomi dei campi per ogni tipo di messaggio e il loro ordine
    private static Map<Type, String[]> messageTypeFields;
    static {
        messageTypeFields = new HashMap<>();
        messageTypeFields.put(Type.Login, new String[] {"username", "password"});
        messageTypeFields.put(Type.PrivateMessage, new String[] {"sender", "recipient", "message"});
        messageTypeFields.put(Type.GlobalMessage, new String[] {"sender", "message"});
        //Volendo si potrebbe prevedere di inserire pure una morivazione al report
        messageTypeFields.put(Type.Report, new String[] {"sender", "reportedUser"});
        messageTypeFields.put(Type.Register, new String[] {"username", "password", "displayName"});
        messageTypeFields.put(Type.Logout, new String[] {"username"});
        messageTypeFields.put(Type.NewMessage, new String[]{"sender", "message"});
        messageTypeFields.put(Type.NotifySuccess, new String[] {"description"});
        messageTypeFields.put(Type.NotifyError, new String[]{ "description" });
        // "blocca" la map in modo che non sia più modificabile
        messageTypeFields = Collections.unmodifiableMap(messageTypeFields);

        messageTypeStrings = new HashMap<>();
        messageTypeStrings.put(Type.Login, "LOGIN");
        messageTypeStrings.put(Type.PrivateMessage, "PRMSG");
        messageTypeStrings.put(Type.GlobalMessage, "GLMSG");
        messageTypeStrings.put(Type.Report, "REPRT");
        messageTypeStrings.put(Type.Register, "RGSTR");
        messageTypeStrings.put(Type.Logout, "LGOUT");
        messageTypeStrings.put(Type.NewMessage, "NWMSG");
        messageTypeStrings.put(Type.NotifySuccess, "SUCSS");
        messageTypeStrings.put(Type.NotifyError, "ERROR");
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