package chatpsit.server;

/**
 * Rappresenta l'insieme dei dati appartenenti ad un utente
 */
public class User
{
    private String username;
    private String hashedPassword;
    private String displayName;     // nome che verrà mostrato in chat
    private boolean isAdmin;

    public User(String username, String hashedPassword, String displayName, boolean isAdmin)
    {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.displayName = displayName;
        this.isAdmin = isAdmin;
    }

    public String getUsername()
    {
        return username;
    }

    public String getHashedPassword()
    {
        return hashedPassword;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isAdmin()
    {
        return isAdmin;
    }
}
