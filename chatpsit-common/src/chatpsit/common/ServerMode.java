package chatpsit.common;

public enum ServerMode
{
    Remote,
    Local;

    public String getCorrespondingUrl()
    {
        switch (this)
        {
            case Local:
                return ServerConstants.LOCAL_SERVER_ADDRESS;
            case Remote:
                return ServerConstants.REMOTE_SERVER_ADDRESS;
            default:
                return null;
        }
    }

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
