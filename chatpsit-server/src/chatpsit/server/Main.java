package chatpsit.server;

public class Main
{
    public static void main(String[] args)
    {
        new Thread(new Server(Server.Mode.Local)).run();
    }
}
