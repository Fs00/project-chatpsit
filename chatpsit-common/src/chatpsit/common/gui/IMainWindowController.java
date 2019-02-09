package chatpsit.common.gui;

import chatpsit.common.Message;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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

    default void showInfoDialog()
    {
        Dialog infoDialog = new Dialog<>();
        infoDialog.setTitle("Informazioni su");
        infoDialog.setHeaderText(null);

        VBox vbox = new VBox(8);
        vbox.setAlignment(Pos.CENTER);
        Label headerLabel = new Label("ChaTPSIT");
        Label developedBy = new Label("Sviluppato da:");
        headerLabel.setFont(new Font(26));
        developedBy.setFont(new Font(16));
        vbox.getChildren().addAll(
            headerLabel,
            developedBy,
            new Label("Farina Samuel"),
            new Label("Lorenzini Loris"),
            new Label("Omodei Davide"),
            new Label("Saltori Francesco"),
            new Label("Valerio Matteo")
        );

        infoDialog.getDialogPane().setContent(vbox);
        infoDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        infoDialog.show();
        infoDialog.setWidth(250);
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

    @Override
    default void notifyMessage(Message message)
    {
        if (message.getType() == Message.Type.ServerShutdown)
        {
            Alert banAlert = new Alert(Alert.AlertType.INFORMATION);
            banAlert.setTitle("");
            banAlert.setHeaderText("Il server è stato arrestato");
            banAlert.setContentText("Grazie per aver usufruito del servizio.");
            banAlert.getButtonTypes().setAll(new ButtonType("Esci dall'applicazione"));
            banAlert.showAndWait();
            Platform.exit();
        }
    }
}
