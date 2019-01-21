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
        //in messageFormatDefiniotns per il tipo passato
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
          al tipo del messaggio in messageFormatDefinitions
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
        /*
         deve controllare che:
         - la stringa del tipo di messaggio esista tra i valori di typeProtocolStrings
         - il numero dei campi corrisponda a quello dell'array relativo al tipo in messageFormatDefinitions
         e infine creare una Map che abbia come chiavi i nomi dei campi forniti in messageFormatDefinitions
         e passarla al costruttore di message
         */
        Map<String, String> messageFields;
        Type typeMessage;
        String[] parts = rawMessage.split(";");
        switch(parts[0])
        {
            case "LOGIN":
                if((parts.length - 1) == messageFormatDefinitions.get("LOGIN").length)
                {
                    typeMessage = Type.Login;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.Login)[0], parts[1],
                            messageFormatDefinitions.get(Type.Login)[1], parts[2]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "PRMSG":
                if((parts.length - 1) == messageFormatDefinitions.get("PRMSG").length)
                {
                    typeMessage = Type.PrivateMessage;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.PrivateMessage)[0], parts[1],
                            messageFormatDefinitions.get(Type.PrivateMessage)[1], parts[2],
                            messageFormatDefinitions.get(Type.PrivateMessage)[2], parts[3]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "GLMSG":
                if((parts.length - 1) == messageFormatDefinitions.get("GLMSG").length)
                {
                    typeMessage = Type.GlobalMessage;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.GlobalMessage)[0], parts[1],
                            messageFormatDefinitions.get(Type.GlobalMessage)[1], parts[2]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "REPRT":
                if((parts.length - 1) == messageFormatDefinitions.get("REPRT").length)
                {
                    typeMessage = Type.Report;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.Report)[0], parts[1],
                            messageFormatDefinitions.get(Type.Report)[1], parts[2]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "RGSTR":
                if((parts.length - 1) == messageFormatDefinitions.get("RGSTR").length)
                {
                    typeMessage = Type.Register;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.Register)[0], parts[1],
                            messageFormatDefinitions.get(Type.Register)[1], parts[2],
                            messageFormatDefinitions.get(Type.Register)[2], parts[3]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "LGOUT":
                if((parts.length - 1) == messageFormatDefinitions.get("LGOUT").length)
                {
                    typeMessage = Type.Logout;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.Logout)[0], parts[1]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "NWMSG":
                if((parts.length - 1) == messageFormatDefinitions.get("NWMSG").length)
                {
                    typeMessage = Type.NewMessage;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.NewMessage)[0], parts[1],
                            messageFormatDefinitions.get(Type.NewMessage)[1], parts[2]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "SUCES":
                if((parts.length - 1) == messageFormatDefinitions.get("SUCES").length)
                {
                    typeMessage = Type.NotifySuccess;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.NotifySuccess)[0], parts[1]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            case "ERROR":
                if((parts.length - 1) == messageFormatDefinitions.get("ERROR").length)
                {
                    typeMessage = Type.NotifyError;
                    messageFields = createFieldsMap(messageFormatDefinitions.get(Type.NotifyError)[0], parts[1]);
                }
                else
                {
                    throw new IllegalArgumentException("Formato del pacchetto errato.");
                }
                break;
            default:
                throw new IllegalArgumentException("Stringa del tipo di messaggio errata.");
        }
        return new Message(typeMessage, messageFields);
    }

    // Contiene le stringhe del protocollo corrispondenti al tipo di messaggio
    private static Map<Type, String> messageTypeStrings;
    // Contiene i nomi dei campi per ogni tipo di messaggio e il loro ordine
    private static Map<Type, String[]> messageFormatDefinitions;
    static {
        messageFormatDefinitions = new HashMap<>();
        messageFormatDefinitions.put(Type.Login, new String[] {"username", "password"});
        messageFormatDefinitions.put(Type.PrivateMessage, new String[] {"sender", "recipient", "message"});
        messageFormatDefinitions.put(Type.GlobalMessage, new  String[] {"sender", "message"});
        //Volendo si potrebbe prevedere di inserire pure una morivazione al report
        messageFormatDefinitions.put(Type.Report, new String[] {"sender", "reportedUser"});
        messageFormatDefinitions.put(Type.Register, new String[] {"username", "password", "displayName"});
        messageFormatDefinitions.put(Type.Logout, new String[] {"username"});
        messageFormatDefinitions.put(Type.NewMessage, new String[]{"sender", "message"});
        messageFormatDefinitions.put(Type.NotifySuccess, new String[] {"description"});
        messageFormatDefinitions.put(Type.NotifyError, new String[]{ "description" });
        // "blocca" la map in modo che non sia più modificabile
        messageFormatDefinitions = Collections.unmodifiableMap(messageFormatDefinitions);

        messageTypeStrings = new HashMap<>();
        messageTypeStrings.put(Type.Login, "LOGIN");
        messageTypeStrings.put(Type.PrivateMessage, "PRMSG");
        messageTypeStrings.put(Type.GlobalMessage, "GLMSG");
        messageTypeStrings.put(Type.Report, "REPRT");
        messageTypeStrings.put(Type.Register, "RGSTR");
        messageTypeStrings.put(Type.Logout, "LGOUT");
        messageTypeStrings.put(Type.NewMessage, "NWMSG");
        messageTypeStrings.put(Type.NotifySuccess, "SUCES");
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
}