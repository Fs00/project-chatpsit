package chatpsit.server;

import chatpsit.common.ServerMode;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        new Thread(new Server(ServerMode.Local)).run();
    }
}
