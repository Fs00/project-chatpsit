package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;


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

        reasonColumn.setCellFactory(column -> {
            TableCell<Message, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(reasonColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
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
