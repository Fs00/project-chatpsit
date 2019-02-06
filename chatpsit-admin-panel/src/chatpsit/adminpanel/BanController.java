package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.Message;
import chatpsit.common.User;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BanController implements IController<AdminPanelModel>
{
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn, isAdminColumn, isBannedColumn;
    @FXML
    private Button banButton, unbanButton;

    public void initialize()
    {
        // TODO cell factories

        usersTable.getSelectionModel().selectedItemProperty().addListener((__, ___, selectedUser) -> {
            if (selectedUser != null && !selectedUser.isAdmin())
            {
                if (selectedUser.isBanned())
                {
                    banButton.setDisable(true);
                    unbanButton.setDisable(false);
                }
                else
                {
                    banButton.setDisable(false);
                    unbanButton.setDisable(true);
                }
            }
            else
            {
                banButton.setDisable(true);
                unbanButton.setDisable(true);
            }
        });
    }

    @FXML
    private void banUser()
    {
        // TODO reason dialog
    }

    @FXML
    private void unbanUser()
    {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        Message unbanMessage = Message.createNew(Message.Type.Unban)
                                .field("bannedUser", selectedUser.getUsername())
                                .build();

        try
        {
            getModel().sendMessageToServer(unbanMessage);
        }
        catch (Exception exc)
        {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Errore di connessione");
            errAlert.setHeaderText("Impossibile rimuovere il ban");
            errAlert.setContentText(exc.getMessage());
            errAlert.show();
        }
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case UserData:
                // TODO
                break;
            case UserBanned:
                // TODO
                break;
            case UserUnbanned:
                // TODO
                break;
            case UserRegistered:
                // TODO
                break;
        }
    }

    @Override
    public AdminPanelModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
