package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ReportController implements IController<AdminPanelModel>
{
    @FXML
    private TableView<Message> reportsTable;
    @FXML
    private TableColumn<Message, String> reportedUserColumn;
    @FXML
    private TableColumn<Message, String> senderUserColumn;
    @FXML
    private TableColumn<Message, String> reasonColumn;

    public void initialize()
    {
        senderUserColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getField("sender")));
        reportedUserColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getField("reportedUser")));
        reasonColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getField("reason")));

        reasonColumn.setCellFactory(column -> new TableCell<Message, String>() {
            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);

                if (text == null || empty)
                    setText(null);
                else
                {
                    setWrapText(true);
                    setPrefWidth(column.getWidth()-20);
                    setText(text);
                }
            }
        });
    }

    @Override
    public void notifyMessage(Message message)
    {
        if (message.getType() == Message.Type.Report)
            reportsTable.getItems().add(message);
    }

    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
