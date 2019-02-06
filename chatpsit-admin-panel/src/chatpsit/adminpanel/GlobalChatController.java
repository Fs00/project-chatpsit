package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.gui.BaseGlobalChatController;

public class GlobalChatController extends BaseGlobalChatController<AdminPanelModel>
{
    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
