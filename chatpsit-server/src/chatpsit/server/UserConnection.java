package chatpsit.server;

import chatpsit.common.Message;

import java.net.Socket;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class UserConnection implements Runnable
{
    private User user;
    private Socket clientSocket;
    private Server server;
    private Date lastActivity;
    private boolean isAdminPanelConnection;
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    public UserConnection(User user, Socket clientSocket, Server server, boolean isAdminPanelConnection) {
        this.user = user;
        this.clientSocket = clientSocket;
        this.server = server;
        this.isAdminPanelConnection = isAdminPanelConnection;
        this.lastActivity = new Date();
    }

    @Override
    public void run()
    {
        // TODO
    }

    public void sendMessage(Message message)
    {
        try {
            messageQueue.put(message);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public User getUser()
    {
        return user;
    }

    public Date getLastActivity()
    {
        return lastActivity;
    }
}
