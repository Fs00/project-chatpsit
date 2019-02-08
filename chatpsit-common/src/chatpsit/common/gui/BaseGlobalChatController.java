package chatpsit.common.gui;

import chatpsit.common.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public abstract class BaseGlobalChatController<M extends BaseClientModel> extends BaseChatController<M>
{
    @FXML
    protected TableView<String> tableViewUsers;
    @FXML
    protected TableColumn<String, String> connectedUsersColumn;

    protected BaseGlobalChatController()
    {
        super(false);
    }

    @Override
    public void initialize()
    {
        super.initialize();
        connectedUsersColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue()));
    }

    @Override
    public void notifyMessage(Message message)
    {
        super.notifyMessage(message);
        switch (message.getType())
        {
            case UserConnected:
                tableViewUsers.getItems().add(message.getField(Message.Field.Username));
                break;
            case UserDisconnected:
                tableViewUsers.getItems().remove(message.getField(Message.Field.Username));
                break;
        }
    }
}
