package chatpsit.server;

import java.net.Socket;
import java.util.Date;

public class User
{
    private String username;
    private Socket clientSocket;
    private Date lastActivity;
}
