package chatpsit.common.gui;

import chatpsit.common.Message;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public abstract class BaseGlobalChatController<M extends ClientModel> implements IController<M>
{
    @FXML
    protected ListView<Message> globalChatList;
    @FXML
    protected TableView<String> tableViewUsers;
    @FXML
    protected TableColumn<String, String> connectedUsersColumn;

    public void initialize()
    {
        // Custom factory per la lista dei messaggi in modo che, a seconda del tipo di messaggio,
        // la entry nella lista venga formattata in maniera diversa
        globalChatList.setCellFactory(listView -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null)
                    setText(null);
                else
                {
                    setWrapText(true);
                    setPrefWidth(listView.getWidth()-20);

                    switch (msg.getType())
                    {
                        case GlobalMessage:
                            if (msg.getField(Message.Field.Sender).equals(getModel().getLoggedInUsername()))
                            {
                                setAlignment(Pos.CENTER_RIGHT);
                                setStyle("-fx-font-weight: bold");
                                setText(msg.getField(Message.Field.Data));
                            }
                            else
                            {
                                setAlignment(Pos.CENTER_LEFT);
                                setStyle("-fx-font-weight: normal");
                                setText(msg.getField(Message.Field.Sender) + ": " + msg.getField(Message.Field.Data));
                            }
                            break;

                        case UserDisconnected:
                        case UserConnected:
                        case UserBanned:
                        case UserUnbanned:
                            setAlignment(Pos.CENTER);
                            setStyle("-fx-font-style: italic");

                            String messageText = "L'utente " + msg.getField(Message.Field.Username);
                            if (msg.getType() == Message.Type.UserBanned)
                                messageText += " è stato bannato";
                            else if (msg.getType() == Message.Type.UserUnbanned)
                                messageText += " non è più bannato";
                            else
                                messageText += " si è " + (msg.getType() == Message.Type.UserConnected ? "connesso" : "disconnesso");

                            setText(messageText);
                            break;
                    }
                }
            }
        });

        connectedUsersColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue()));
    }

    @Override
    public void notifyMessage(Message message)
    {
        switch (message.getType())
        {
            case GlobalMessage:
            case UserBanned:
            case UserUnbanned:
                globalChatList.getItems().add(message);
                break;
            case UserConnected:
                tableViewUsers.getItems().add(message.getField(Message.Field.Username));
                globalChatList.getItems().add(message);
                break;
            case UserDisconnected:
                tableViewUsers.getItems().remove(message.getField(Message.Field.Username));
                globalChatList.getItems().add(message);
                break;
        }
        // Posiziona lo scrolling della lista sull'ultimo messaggio
        globalChatList.scrollTo(globalChatList.getItems().size()-1);
    }

    @Override
    public abstract M getModel();
}
