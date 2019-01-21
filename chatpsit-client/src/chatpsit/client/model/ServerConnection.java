package chatpsit.client.model;

public enum ServerConnection
{
    Local,
    Remote;

    @Override
    public String toString()
    {
        switch (this)
        {
            case Local:
                return "Locale";
            case Remote:
                return "Remoto";
            default:
                return null;
        }
    }
}
