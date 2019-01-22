package chatpsit.server;

import java.net.Socket;
import java.util.Date;

public class UserConnection implements Runnable
{
    private User user;
    private Socket clientSocket;
    private Date lastActivity;

    @Override
    public void run()
    {
        // TODO
    }
}
