package chatpsit.server;

/**
 * Rappresenta l'insieme dei dati appartenenti ad un utente
 */
public class User
{
    private String username;
    private String hashedPassword;
    private boolean isAdmin;

    public User(String username, String hashedPassword, boolean isAdmin)
    {
        this.username = username;
        this.hashedPassword = hashedPassword;
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

    public boolean isAdmin()
    {
        return isAdmin;
    }
}
