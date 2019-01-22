package chatpsit.client.model;

import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;

import java.util.ArrayList;
import java.util.List;

public class ClientModel implements IModel
{
    private List<IController> attachedControllers;

    public ClientModel()
    {
        attachedControllers = new ArrayList<>(3);
    }

    public void attachController(IController controller)
    {
        attachedControllers.add(controller);
    }

    public void detachController(IController controller)
    {
        attachedControllers.remove(controller);
    }

    public void detachControllers()
    {
        attachedControllers.clear();
    }
}
