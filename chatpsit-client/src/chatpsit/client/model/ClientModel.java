package chatpsit.client.model;

import chatpsit.common.ServerConstants;
import chatpsit.common.ServerMode;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;

import java.util.ArrayList;
import java.util.List;

public class ClientModel implements IModel
{
    private String serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
    private List<IController> attachedControllers;

    public void changeServerUrl(ServerMode connectionMode)
    {
        switch (connectionMode)
        {
            case Local:
                serverUrl = ServerConstants.LOCAL_SERVER_ADDRESS;
                break;
            case Remote:
                serverUrl = ServerConstants.REMOTE_SERVER_ADDRESS;
                break;
        }
    }

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
