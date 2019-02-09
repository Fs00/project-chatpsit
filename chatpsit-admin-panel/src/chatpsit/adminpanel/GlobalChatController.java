package chatpsit.adminpanel;

import chatpsit.common.gui.ClientModel;
import chatpsit.common.gui.BaseGlobalChatController;

public class GlobalChatController extends BaseGlobalChatController<ClientModel>
{
    @Override
    public ClientModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
