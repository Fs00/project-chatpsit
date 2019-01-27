package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminPanelApp extends Application
{
    private static Map<String, Parent> parentNodes = new HashMap<>();
    private static Stage loginStage, mainStage;
    private static IController loginController;
    private static IModel model = new AdminPanelModel();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        loginStage = primaryStage;
        loadParentNodes();
        showLoginScene();
    }

    private static void showLoginScene()
    {
        model.detachControllers();
        model.attachController(loginController);
        loginStage.setTitle("Pannello di amministrazione");
        loginStage.setScene(new Scene(parentNodes.get("login")));
        loginStage.setResizable(false);
        loginStage.show();
    }

    private void loadParentNodes() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(AdminPanelApp.class.getResource("views/login.fxml"));
        parentNodes.put("login", loader.load());
        loginController = loader.getController();
    }

    static AdminPanelModel getModel() {
        return (AdminPanelModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
