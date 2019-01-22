package chatpsit.server;

import chatpsit.common.Message;

import java.net.Socket;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class UserConnection implements Runnable
{
    private User user;
    private Socket clientSocket;
    private Date lastActivity;
    private LinkedBlockingQueue<Message> messageQueue;

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
