package chatpsit.server;

import chatpsit.common.ServerMode;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Server serverInstance = new Server(ServerMode.Local);
        new Thread(serverInstance).start();

        // Effettua le operazioni di chiusura del server quando viene richiesta la terminazione del processo
        Runtime.getRuntime().addShutdownHook(new Thread(serverInstance::shutdownServer));
    }
}
