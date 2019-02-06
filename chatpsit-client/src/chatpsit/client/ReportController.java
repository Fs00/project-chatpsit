package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.Message;
import chatpsit.common.gui.IController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ReportController implements IController<UserClientModel> {

    @FXML
    private Button undoReportButton, sendReportButton;

    @FXML
    private TextArea reasonTextArea;

    public void initialize()
    {
        // Listener per abilitare bottone Riporta utente se il testo non è vuoto e non contiene spazi
        reasonTextArea.textProperty().addListener(text -> {
            if (!reasonTextArea.getText().trim().isEmpty())
                sendReportButton.setDisable(false);
            else
                sendReportButton.setDisable(true);
        });
    }

    @Override
    public void notifyMessage(Message message) {

    }

    @Override
    public UserClientModel getModel() {
        return null;
    }
}
