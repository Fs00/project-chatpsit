package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IMainWindowController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application
{
    private static Scene loginScene, registerScene;
    private static IController loginController, registrationController;
    private static IModel model = new UserClientModel();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        loadStartupWindowLayouts();
        setLoginScene(primaryStage);
        primaryStage.show();
    }

    static void showStartupWindow()
    {
        Stage startupStage = new Stage();
        setLoginScene(startupStage);
        startupStage.show();
        startupStage.requestFocus();
    }

    /*
      A differenza della finestra di avvio, la finestra della chat globale viene caricata da capo
      ogni volta affinché venga ricreata una nuova istanza del controller senza i dati precedenti
     */
    static void showGlobalChatWindow()
    {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/globalChat.fxml"));
        try
        {
            Stage globalChatStage = new Stage();
            globalChatStage.setTitle("Chat globale");
            globalChatStage.setResizable(false);
            globalChatStage.setScene(new Scene(loader.load()));

            globalChatStage.setOnCloseRequest(event -> {
                boolean logoutSuccessful = loader.<IMainWindowController>getController().sendLogout();
                if (!logoutSuccessful)
                    event.consume();    // annulla la chiusura della finestra
            });

            globalChatStage.show();
        }
        catch (Exception exc)
        {
            System.err.println("FATAL: Error when loading global chat window: " + exc.getMessage());
            Platform.exit();
        }
    }

    /*
      Funzioni per il cambio scena nella finestra di login/registrazione.
      Il model viene scollegato all'interno dei rispettivi controller ad ogni cambio scena.
     */
    static void setLoginScene(Stage startupStage)
    {
        loginController.bindToModel();
        startupStage.setTitle("Login");
        startupStage.setScene(loginScene);
        startupStage.setResizable(false);
    }

    static void setRegisterScene(Stage startupStage)
    {
        registrationController.bindToModel();
        startupStage.setTitle("Registrazione");
        startupStage.setScene(registerScene);
        startupStage.setResizable(false);
    }

    private void loadStartupWindowLayouts() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/login.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();

        loader = new FXMLLoader(ClientApp.class.getResource("views/registration.fxml"));
        registerScene = new Scene(loader.load());
        registrationController = loader.getController();
    }

    static UserClientModel getModel() {
        return (UserClientModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }

    static void showReportWindow()
    {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/report.fxml"));
        try
        {
            Stage reportStage = new Stage();
            reportStage.setTitle("Report");
            reportStage.setResizable(false);
            reportStage.setScene(new Scene(loader.load()));

            reportStage.setOnCloseRequest(event -> {
                reportStage.close();
            });

            reportStage.show();
        }
        catch (Exception exc)
        {
            System.err.println("FATAL: Error when loading report window: " + exc.getMessage());
            Platform.exit();
        }
    }
}
