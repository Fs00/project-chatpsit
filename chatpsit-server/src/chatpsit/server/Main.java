package chatpsit.server;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        new Thread(new Server(Server.Mode.Local)).run();
    }
}
