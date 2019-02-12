package chatpsit.server;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Server serverInstance = new Server(false);
        new Thread(serverInstance).start();

        // Effettua le operazioni di chiusura del server quando viene richiesta la terminazione del processo
        Runtime.getRuntime().addShutdownHook(new Thread(serverInstance::shutdownServer));
    }
}
