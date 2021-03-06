package chatpsit.adminpanel;

import chatpsit.common.gui.ClientModel;
import chatpsit.common.Message;
import chatpsit.common.User;
import chatpsit.common.gui.IController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class BanController implements IController<ClientModel>
{
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> usernameColumn, isAdminColumn, isBannedColumn;
    @FXML
    private Button banButton, unbanButton;

    public void initialize()
    {
        usernameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getUsername()));
        isAdminColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().isAdmin() ? "sì" : "no"));
        isBannedColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().isBanned() ? "sì" : "no"));

        usersTable.getSelectionModel().selectedItemProperty().addListener((__, ___, selectedUser) -> {
            updateButtonsAfterUserSelection(selectedUser);
        });
    }

    private void updateButtonsAfterUserSelection(User selectedUser)
    {
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
    }

    /**
     * Effettua il ban dell'utente selezionato richiedendo una motivazione
     * Se la motivazione non viene inserita, la procedura si interrompe
     */
    @FXML
    private void banUser()
    {
        User userToBeBanned = usersTable.getSelectionModel().getSelectedItem();

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Ban dell'utente " + userToBeBanned.getUsername());
        inputDialog.setGraphic(null);
        inputDialog.setHeaderText("Inserisci una motivazione da mostrare all'utente:");

        Optional<String> banReason = inputDialog.showAndWait();
        if (banReason.isPresent())
        {
            if (banReason.get().isEmpty())
            {
                Alert errAlert = new Alert(Alert.AlertType.ERROR);
                errAlert.setHeaderText(null);
                errAlert.setContentText("Non è stata inserita una motivazione.");
                errAlert.show();
            }
            else
            {
                Message banMessage = Message.createNew(Message.Type.Ban)
                                    .field(Message.Field.BannedUser, userToBeBanned.getUsername())
                                    .field(Message.Field.Reason, banReason.get())
                                    .build();

                try
                {
                    getModel().sendMessageToServer(banMessage);
                }
                catch (Exception exc)
                {
                    Alert errAlert = new Alert(Alert.AlertType.ERROR);
                    errAlert.setTitle("Errore di connessione");
                    errAlert.setHeaderText("Impossibile bannare l'utente");
                    errAlert.setContentText(exc.getMessage());
                    errAlert.show();
                }
            }
        }
    }

    /**
     * Rimuove il ban per l'utente selezionato
     */
    @FXML
    private void unbanUser()
    {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        Message unbanMessage = Message.createNew(Message.Type.Unban)
                                .field(Message.Field.BannedUser, selectedUser.getUsername())
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
                usersTable.getItems().add(User.deserialize(message.getField(Message.Field.Data)));
                break;

            case UserUnbanned:
            case UserBanned:
                User userTableEntry = usersTable.getItems().stream()
                                      .filter(user -> user.getUsername().equals(message.getField(Message.Field.Username)))
                                      .findFirst().orElse(null);

                if (userTableEntry != null)
                {
                    if (message.getType() == Message.Type.UserBanned)
                        userTableEntry.ban();
                    else
                        userTableEntry.unban();

                    // Abilita/disabilita i bottoni in base all'azione effettuata e aggiorna la tabella
                    updateButtonsAfterUserSelection(userTableEntry);
                    usersTable.refresh();
                }
                else
                    System.err.println("Ricevuta notifica di ban/unban di un utente sconosciuto");
                break;

            case UserRegistered:
                usersTable.getItems().add(new User(message.getField(Message.Field.Username), "", false, false));
                break;
        }
    }

    @Override
    public ClientModel getModel()
    {
        return AdminPanelApp.getModel();
    }
}
