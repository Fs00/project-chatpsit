package chatpsit.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Message
{
    public enum Type {
        NotifyError
    }

    private String type;
    private Map<String, String> fields;

    public String getType()
    {
        return type;
    }

    public Map<String, String> getFields()
    {
        return fields;
    }

    public Message(Type type, Map<String, String> fields)
    {
        /*
          deve:
          - controllare che fields abbia tutti i campi specificati in messageFormatDefinitions per il tipo passsato
          - se il controllo è ok, assegnare i parametri ai campi della classe
         */
    }

    /**
     * Crea una stringa contenente i dati del'istanza corrente per poterli trasmettere
     */
    public String serialize()
    {
        /*
          deve formare la stringa concatenando i fields nell'ordine specificato dall'array corrispondente
          al tipo del messaggio in messageFormatDefinitions
         */
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
    }

    // Contiene le stringhe del protocollo corrispondenti al tipo di messaggio
    private static Map<Type, String> messageTypeStrings;
    // Contiene i nomi dei campi per ogni tipo di messaggio e il loro ordine
    private static Map<Type, String[]> messageFormatDefinitions;
    static {
        messageFormatDefinitions = new HashMap<>();
        messageFormatDefinitions.put(Type.NotifyError, new String[]{ "description" });
        // TODO mettere qui le altre definizioni come sopra
        // "blocca" la map in modo che non sia più modificabile
        messageFormatDefinitions = Collections.unmodifiableMap(messageFormatDefinitions);

        messageTypeStrings = new HashMap<>();
        messageTypeStrings.put(Type.NotifyError, "ERROR");
        // TODO mettere qui le altre definizioni come sopra
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
