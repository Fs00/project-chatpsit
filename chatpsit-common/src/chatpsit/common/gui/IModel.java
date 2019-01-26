package chatpsit.common.gui;

import chatpsit.common.Message;
import chatpsit.common.ServerMode;

import java.io.IOException;

public interface IModel
{
    void attachController(IController controller);
    void detachController(IController controller);
    void detachControllers();

    void sendMessageToServer(Message message) throws IOException;
    void startMessageReceivingListener();
    void stopMessageReceivingListener();
    void changeServerMode(ServerMode mode);
}
