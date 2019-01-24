package chatpsit.server;

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
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.isAdmin = isAdmin;
        this.isBanned = isBanned;
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
        isBanned = true;
    }

    public void unban()
    {
        isBanned = false;
    }

    public boolean passwordMatches(String plainPassword)
    {
        // TODO
    }

    public String serialize()
    {
        // TODO
    }

    public static User deserialize(String userAsString)
    {
        // TODO
    }

    public static boolean isValidUsername(String username)
    {
        // TODO
    }

    public static String hashPassword(String plainPassword)
    {
        // TODO
    }
}
