package chatpsit.common;

import com.sun.istack.internal.NotNull;

import java.util.Collections;
import java.util.EnumMap;
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
        private Map<Field, String> messageFields = new EnumMap<>(Field.class);
        private boolean isLastMessage = false;

        private Builder(Type type)
        {
            messageType = type;
        }

        public Builder field(Field name, String value)
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
    private Map<Field, String> fields;
    private boolean isLastMessage;

    /**
     * Il costruttore verifica la correttezza dei dati passatigli e crea una nuova istanza del messaggio.
     * NON CHIAMARE DIRETTAMENTE: utilizzare il builder con Message.createNew()
     * @param type il tipo di messaggio
     * @param fields una Map contenente i campi del messaggio
     * @param isLastMessage indica se dopo l'invio del messaggio la connessione verrà chiusa
     */
    private Message(Type type, @NotNull Map<Field, String> fields, boolean isLastMessage)
    {
        if (type == null)
            throw new IllegalArgumentException("Il tipo del messaggio non può essere null.");

        Field[] typeFields = type.fields();
        if (typeFields.length != fields.size())
            throw new IllegalArgumentException("Il numero di campi del messaggio non è conforme al suo tipo.");

        for (Field typeField : typeFields)
        {
            if (!fields.containsKey(typeField))
                throw new IllegalArgumentException("È presente un campo del messaggio non conforme al suo tipo.");

            if (fields.get(typeField) == null)
                throw new IllegalArgumentException("Il campo " + typeField + " del messaggio non è valorizzato.");
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

    public String getField(Field name)
    {
        return fields.get(name);
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
        String message = type.serializedString();
        Field[] typeFields = type.fields();

        for (Field typeField : typeFields)
            message += DELIMITER_CHAR + this.fields.get(typeField);

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
        Type rawMessageType = Type.fromSerializedString(rawMessageFields[0]);
        if (rawMessageType == null)
            throw new IllegalArgumentException("Il tipo di messaggio " + rawMessageFields[0] + " non esiste.");

        // Ottengo i nomi dei campi corrispondenti al tipo di messaggio dato
        Field[] rawMessageTypeFields = rawMessageType.fields();
        // rawMessageFields ha un campo in più rispetto all'array in messageTypeFields, che è il tipo di messaggio
        if (rawMessageTypeFields.length == (rawMessageFields.length - 1))
        {
            Builder messageBuilder = Message.createNew(rawMessageType);
            for (int i = 0; i < rawMessageTypeFields.length; i++)
                messageBuilder.field(rawMessageTypeFields[i], rawMessageFields[i+1]);

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
    public enum Type
    {
        UserLogin("ULGIN"),
        AdminPanelLogin("ALGIN"),
        Ready("READY"),
        PrivateMessage("PRMSG"),
        GlobalMessage("GLMSG"),
        Report("REPRT"),
        Register("RGSTR"),
        Logout("LGOUT"),
        NotifySuccess("SUCSS"),
        NotifyError("ERROR"),
        Ban("BANHR"),
        Unban("UNBAN"),
        LogEvent("LGEVT"),
        UserData("UDATA"),
        UserConnected("USRCT"),
        UserDisconnected("USRDC"),
        UserBanned("USRBN"),
        UserUnbanned("USRUB"),
        UserRegistered("USRRG"),
        ServerShutdown("SHUTD");

        private final String serializedString;
        Type(String serializedString)
        {
            this.serializedString = serializedString;
        }

        String serializedString()
        {
            return serializedString;
        }
        Field[] fields()
        {
            return messageTypeFields.get(this);
        }

        /**
         * Data la rappresentazione in forma di stringa di un tipo di messaggio, restituisce il corrispondente valore
         * dell'enumerazione
         */
        static Type fromSerializedString(String serializedString)
        {
            for (Type messageType : Type.values())
            {
                if (messageType.serializedString.equals(serializedString))
                    return messageType;
            }
            return null;
        }
    }

    /**
     * Elenca i possibili campi presenti in un messaggio
     */
    public enum Field
    {
        Username,
        Password,
        Sender,
        Recipient,
        Reason,
        BannedUser,
        ReportedUser,
        Data,
        Action
    }

    // Caratteri usati nella serializzazione del messaggio
    private static char DELIMITER_CHAR = 31;
    private static char END_OF_TRANSMISSION_CHAR = 4;

    // Contiene i nomi dei campi per ogni tipo di messaggio e il loro ordine
    private static Map<Type, Field[]> messageTypeFields;
    static {
        messageTypeFields = new EnumMap<>(Type.class);
        messageTypeFields.put(Type.UserLogin, new Field[] { Field.Username, Field.Password });
        messageTypeFields.put(Type.AdminPanelLogin, new Field[] { Field.Username, Field.Password});
        messageTypeFields.put(Type.Ready, new Field[] {});
        messageTypeFields.put(Type.PrivateMessage, new Field[] { Field.Sender, Field.Recipient, Field.Data });
        messageTypeFields.put(Type.GlobalMessage, new Field[] { Field.Sender, Field.Data });
        messageTypeFields.put(Type.Report, new Field[] { Field.Sender, Field.ReportedUser, Field.Reason });
        messageTypeFields.put(Type.Ban, new Field[] { Field.BannedUser, Field.Reason });
        messageTypeFields.put(Type.Unban, new Field[] { Field.BannedUser });
        messageTypeFields.put(Type.Register, new Field[] { Field.Username, Field.Password});
        messageTypeFields.put(Type.Logout, new Field[] {});
        messageTypeFields.put(Type.NotifySuccess, new Field[] { Field.Action, Field.Data });
        messageTypeFields.put(Type.NotifyError, new Field[] { Field.Action, Field.Data });
        messageTypeFields.put(Type.LogEvent, new Field[] { Field.Data });
        messageTypeFields.put(Type.UserData, new Field[] { Field.Data });
        messageTypeFields.put(Type.UserConnected, new Field[] { Field.Username });
        messageTypeFields.put(Type.UserDisconnected, new Field[] { Field.Username });
        messageTypeFields.put(Type.UserBanned, new Field[] { Field.Username });
        messageTypeFields.put(Type.UserUnbanned, new Field[] { Field.Username });
        messageTypeFields.put(Type.UserRegistered, new Field[] { Field.Username });
        messageTypeFields.put(Type.ServerShutdown, new Field[] {});
        // blocca la map in modo che non sia più modificabile
        messageTypeFields = Collections.unmodifiableMap(messageTypeFields);
    }
}