package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application
{
    private static Scene loginScene, registerScene, globalChatScene;
    private static IController loginController, registrationController, globalChatController;
    private static IModel model = new UserClientModel();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        loadWindowLayouts();
        setLoginScene(primaryStage);
        primaryStage.show();
    }

    static void showGlobalChatWindow()
    {
        model.detachControllers();
        model.attachController(globalChatController);

        Stage globalChatStage = new Stage();
        globalChatStage.setTitle("Chat globale");
        globalChatStage.setResizable(false);
        globalChatStage.setScene(globalChatScene);
        globalChatStage.setOnCloseRequest(event -> {
            boolean logoutSuccessful = ((GlobalChatController) globalChatController).sendLogout();
            if (!logoutSuccessful)
                event.consume();    // stops window closing
        });
        globalChatStage.show();
    }

    static void showStartupWindow()
    {
        Stage startupStage = new Stage();
        setLoginScene(startupStage);
        startupStage.show();
    }

    static void setLoginScene(Stage startupStage)
    {
        model.detachControllers();
        model.attachController(loginController);
        startupStage.setTitle("Login");
        startupStage.setScene(loginScene);
        startupStage.setResizable(false);
    }

    static void setRegisterScene(Stage startupStage)
    {
        model.detachControllers();
        model.attachController(registrationController);
        startupStage.setTitle("Registrazione");
        startupStage.setScene(registerScene);
        startupStage.setResizable(false);
    }

    private void loadWindowLayouts() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/login.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();

        loader = new FXMLLoader(ClientApp.class.getResource("views/registration.fxml"));
        registerScene = new Scene(loader.load());
        registrationController = loader.getController();

        loader = new FXMLLoader(ClientApp.class.getResource("views/globalChat.fxml"));
        globalChatScene = new Scene(loader.load());
        globalChatController = loader.getController();
    }

    static UserClientModel getModel() {
        return (UserClientModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
