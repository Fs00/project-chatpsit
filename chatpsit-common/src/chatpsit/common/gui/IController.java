package chatpsit.common.gui;

import chatpsit.common.Message;

public interface IController
{
    void notifyMessage(Message message);
    default void bindToModel(IModel model)
    {
        model.attachController(this);
    }
}
