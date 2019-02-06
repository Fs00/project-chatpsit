package chatpsit.common.gui;

import chatpsit.common.Message;

public interface IController<M extends IModel>
{
    void notifyMessage(Message message);
    M getModel();
    default void bindToModel()
    {
        getModel().attachController(this);
    }
}
