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
    private static Stage startupStage, mainStage;
    private static Scene loginScene, registerScene;
    private static IController loginController, registrationController;
    private static IModel model = new UserClientModel();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        startupStage = primaryStage;
        initializeStartupScenes();
        showLoginScene();
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

    static void showLoginScene()
    {
        model.detachControllers();
        model.attachController(loginController);
        startupStage.setTitle("Login");
        startupStage.setScene(loginScene);
        startupStage.setResizable(false);
        startupStage.show();
    }

    static void showRegisterScene()
    {
        model.detachControllers();
        model.attachController(registrationController);
        startupStage.setTitle("Registrazione");
        startupStage.setScene(registerScene);
        startupStage.setResizable(false);
        startupStage.show();
    }

    static UserClientModel getModel() {
        return (UserClientModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
