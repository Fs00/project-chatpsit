package chatpsit.common.gui;

import chatpsit.common.Message;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public interface IMainWindowController<M extends IModel> extends IController<M>
{
    Stage getCurrentWindow();
    void showStartupWindow();

    default void initialize()
    {
        bindToModel();
        sendReadyMessageToServer();
    }

    default boolean sendLogout()
    {
        boolean logoutSuccessful = false;
        try
        {
            Message logoutMessage = Message.createNew(Message.Type.Logout)
                                    .lastMessage()
                                    .build();
            getModel().sendMessageToServer(logoutMessage);
            getModel().detachControllers();
            logoutSuccessful = true;
        }
        catch (Exception exc)
        {
            Alert errorDialog = new Alert(Alert.AlertType.ERROR);
            errorDialog.setTitle("Errore di connessione");
            errorDialog.setHeaderText("Logout fallito");
            errorDialog.setContentText(exc.getMessage());

            ButtonType quitAnyway = new ButtonType("Esci comunque", ButtonBar.ButtonData.LEFT);
            ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            errorDialog.getButtonTypes().setAll(quitAnyway, ok);

            Optional<ButtonType> choice = errorDialog.showAndWait();
            if (choice.isPresent() && choice.get() == quitAnyway)
                Platform.exit();
        }
        return logoutSuccessful;
    }

    default void logoutAndCloseWindow()
    {
        boolean logoutSuccessful = sendLogout();
        if (logoutSuccessful)
            getCurrentWindow().close();
    }

    default void logoutAndQuitToLogin()
    {
        logoutAndCloseWindow();
        if (!getCurrentWindow().isShowing())
            showStartupWindow();
    }

    /**
     * Segnala al server che è pronto a ricevere messaggi
     * Se l'invio del messaggio fallisce, l'utente è invitato a riprovare o a chiudere l'applicazione
     */
    default void sendReadyMessageToServer()
    {
        boolean messageSent = false;
        while (!messageSent)
        {
            try
            {
                Message readyMessage = Message.createNew(Message.Type.Ready).build();
                getModel().sendMessageToServer(readyMessage);
                messageSent = true;
            }
            catch (Exception exc)
            {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Errore di connessione");
                errorAlert.setHeaderText("Impossibile mandare il messaggio 'READY' al server");
                errorAlert.setContentText("Riprova o esci dall'applicazione.");

                ButtonType retry = new ButtonType("Riprova", ButtonBar.ButtonData.BACK_PREVIOUS);
                ButtonType quit = new ButtonType("Esci", ButtonBar.ButtonData.FINISH);
                errorAlert.getButtonTypes().setAll(retry, quit);

                Optional<ButtonType> choice = errorAlert.showAndWait();
                if (!choice.isPresent() || choice.get() == quit)
                    Platform.exit();
            }
        }
    }
}
