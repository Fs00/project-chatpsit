package chatpsit.common.gui;

import chatpsit.common.Message;

public interface IModel
{
    void attachController(IController controller);
    void detachController(IController controller);
    void detachControllers();

    void sendMessageToServer(Message message) throws Exception;
}
