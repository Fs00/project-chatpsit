package chatpsit.client;

import chatpsit.client.model.UserClientModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application
{
    private static Stage startupStage, globalChatStage;
    private static Scene loginScene, registerScene;
    private static Parent globalChatRootNode;
    private static IController loginController, registrationController, globalChatController;
    private static IModel model = new UserClientModel();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        startupStage = primaryStage;
        initializeStartupScenes();
        initializeGlobalChatStage();
        showStartupStage();
    }

    static void showGlobalChatWindow()
    {
        startupStage.hide();
        model.detachControllers();
        model.attachController(globalChatController);
        globalChatStage.setScene(new Scene(globalChatRootNode));
        globalChatStage.show();
        globalChatStage.requestFocus();
    }

    static void showStartupStage()
    {
        globalChatStage.hide();
        setLoginScene();
        startupStage.show();
    }

    static void setLoginScene()
    {
        model.detachControllers();
        model.attachController(loginController);
        startupStage.setTitle("Login");
        startupStage.setScene(loginScene);
        startupStage.setResizable(false);
    }

    static void setRegisterScene()
    {
        model.detachControllers();
        model.attachController(registrationController);
        startupStage.setTitle("Registrazione");
        startupStage.setScene(registerScene);
        startupStage.setResizable(false);
    }

    private void initializeGlobalChatStage() throws IOException
    {
        globalChatStage = new Stage();
        globalChatStage.setTitle("Chat globale");
        globalChatStage.setResizable(false);

        FXMLLoader loader = new FXMLLoader(ClientApp.class.getResource("views/globalChat.fxml"));
        globalChatRootNode = loader.load();
        globalChatController = loader.getController();
    }

    private void initializeStartupScenes() throws IOException
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
}
