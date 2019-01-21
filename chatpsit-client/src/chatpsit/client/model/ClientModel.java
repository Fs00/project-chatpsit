package chatpsit.client.model;

import java.util.ArrayList;
import java.util.List;

public class ClientModel
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
}
