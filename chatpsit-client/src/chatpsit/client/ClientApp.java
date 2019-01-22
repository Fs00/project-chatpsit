package chatpsit.client;

import chatpsit.client.model.ClientModel;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application
{
    private static Stage mainStage;
    private static Scene loginScene, registerScene;
    private static final IModel model = new ClientModel();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        mainStage = primaryStage;
        initializeStartupScenes();
        showLoginScene();
    }

    private void initializeStartupScenes() throws IOException
    {
        Parent loginRoot = FXMLLoader.load(ClientApp.class.getResource("views/login.fxml"));
        loginScene = new Scene(loginRoot);

        Parent registerRoot = FXMLLoader.load(ClientApp.class.getResource("views/registration.fxml"));
        registerScene = new Scene(registerRoot);
    }

    static void showLoginScene()
    {
        model.detachControllers();
        mainStage.setTitle("Login");
        mainStage.setScene(loginScene);
        mainStage.setResizable(false);
        mainStage.show();
    }

    static void showRegisterScene()
    {
        model.detachControllers();
        mainStage.setTitle("Registrazione");
        mainStage.setScene(registerScene);
        mainStage.setResizable(false);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    static IModel getModel() {
        return model;
    }
}
