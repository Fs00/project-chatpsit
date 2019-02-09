package chatpsit.adminpanel;

import chatpsit.common.gui.ClientModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IMainWindowController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminPanelApp extends Application
{
    private static Scene loginScene;
    private static IController loginController;
    private static IModel model = new ClientModel();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        loadLoginWindowLayout();
        showLoginWindow();
    }

    static void showLoginWindow()
    {
        loginController.bindToModel();

        Stage loginStage = new Stage();
        loginStage.setTitle("Pannello di amministrazione");
        loginStage.setResizable(false);
        loginStage.setScene(loginScene);
        loginStage.show();
        loginStage.requestFocus();
    }

    /*
      A differenza della finestra di login, la finestra principale viene caricata da capo
      ogni volta affinché venga ricreata una nuova istanza del controller senza i dati precedenti
     */
    static void showMainWindow()
    {
        FXMLLoader loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindow.fxml"));
        try
        {
            Stage mainStage = new Stage();
            mainStage.setTitle("Pannello di amministrazione - " + getModel().getLoggedInUsername());
            mainStage.setResizable(false);
            mainStage.setScene(new Scene(loader.load()));

            mainStage.setOnCloseRequest(event -> {
                boolean logoutSuccessful = loader.<IMainWindowController>getController().sendLogout();
                if (!logoutSuccessful)
                    event.consume();    // annulla la chiusura della finestra
            });

            mainStage.show();
        }
        catch (Exception exc)
        {
            System.err.println("FATAL: Missing layout assets for global chat window");
            Platform.exit();
        }
    }

    private void loadLoginWindowLayout() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(AdminPanelApp.class.getResource("views/login.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();
    }

    static ClientModel getModel() {
        return (ClientModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
