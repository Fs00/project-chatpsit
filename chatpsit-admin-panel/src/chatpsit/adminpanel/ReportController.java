package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;

public class ReportController implements IController<AdminPanelModel>
{
    @Override
    public void notifyMessage(Message message)
    {
        // TODO
    }

    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
