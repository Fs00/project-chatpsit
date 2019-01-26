package chatpsit.common;

public enum ServerMode
{
    Remote,
    Local;

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
