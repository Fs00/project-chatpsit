package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ServerLogController implements IController<AdminPanelModel>
{
    @FXML
    private ListView<String> logList;

    public void initialize()
    {
        logList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null)
                    setText(null);
                else
                {
                    setWrapText(true);
                    setPrefWidth(listView.getWidth()-20);
                    setText(item);
                }
            }
        });
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case LogEvent:
                logList.getItems().add(message.getField("text"));
                logList.scrollTo(logList.getItems().size()-1);
                break;
        }
    }

    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
