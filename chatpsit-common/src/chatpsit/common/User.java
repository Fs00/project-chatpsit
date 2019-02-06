package chatpsit.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Rappresenta l'insieme dei dati appartenenti ad un utente
 */
public class User
{
    private String username;
    private String hashedPassword;
    private boolean isAdmin;
    private boolean isBanned;

    public User(String username, String hashedPassword, boolean isAdmin, boolean isBanned)
    {
        if (!isValidUsername(username))
            throw new IllegalArgumentException("Formato username non valido");
        else if (isAdmin && isBanned)
            throw new IllegalArgumentException("Un amministratore non può essere bannato");
        else
        {
            this.username = username;
            this.hashedPassword = hashedPassword;
            this.isAdmin = isAdmin;
            this.isBanned = isBanned;
        }
    }

    public String getUsername()
    {
        return username;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }

    public boolean isBanned()
    {
        return isBanned;
    }

    public void ban()
    {
        if (!isAdmin)
            isBanned = true;
        else
            throw new IllegalArgumentException("Un amministratore non può essere bannato");
    }

    public void unban()
    {
        isBanned = false;
    }

    /**
     * Verifica che la password inserita dall'utente sia l'equivalente della password hashata registrata
     * @param plainPassword password in chiaro inserita dall'utente
     * @return true o false in base all'equivalnza o meno
     */
    public boolean passwordMatches(String plainPassword)
    {
        return hashedPassword.equals(User.hashPassword(plainPassword));
    }

    /**
     * Crea una stringa contenente i dati dell'istanza corrente per poterli salvare sul file
     */
    public String serialize()
    {
        String fileRecord = "";
        if (isAdmin)
            fileRecord = "@";
        else if (isBanned)
            fileRecord = "!";
        return fileRecord + username + ";" + hashedPassword;
}

    /**
     * Crea una nuova istanza della classe partendo da una stringa trasmessa
     * @param userAsString stringa prodotta dal metodo serialize()
     * @return l'utente estrapolato dalla stringa
     */
    public static User deserialize(String userAsString)
    {
        boolean isAdmin = false;
        boolean isBanned = false;
        String [] userAsStringFields = userAsString.split(";");
        if (userAsStringFields[0].startsWith("@"))
        {
            isAdmin = true;
            userAsStringFields[0] = userAsStringFields[0].substring(1);
        }
        else if (userAsStringFields[0].startsWith("!"))
        {
            isBanned = true;
            userAsStringFields[0] = userAsStringFields[0].substring(1);
        }
        return new User(userAsStringFields[0], userAsStringFields[1], isAdmin, isBanned);
    }

    /**
     * Controlla se il nome preso in esame è valido o meno
     * @param username username da controllare
     * @return true o false in base correttezza o meno
     */
    public static boolean isValidUsername(String username)
    {
        return !(username == null || username.isEmpty() || username.contains(";") || username.contains("@") ||
                 username.contains("!") || username.contains(" "));
    }

    /**
     * Hasha la password in chiaro
     * @param plainPassword password in chiaro
     * @return la password in chiaro
     */
    public static String hashPassword(String plainPassword)
    {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
        return toHexadecimalString(encodedhash);
    }

    /**
     * Hasha il biyeArray della password inserita dall'utente
     * @param byteArray biteArray contenente la password
     * @return Password, sottoforma di stringa, hashata
     */
    private static String toHexadecimalString(byte[] byteArray)
    {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            String hex = Integer.toHexString(0xff & byteArray[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
