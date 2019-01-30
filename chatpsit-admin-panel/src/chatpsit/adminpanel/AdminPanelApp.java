package chatpsit.adminpanel;

import chatpsit.adminpanel.model.AdminPanelModel;
import chatpsit.common.gui.IController;
import chatpsit.common.gui.IModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminPanelApp extends Application
{
    private static Scene loginScene, mainWindowScene;
    private static IController loginController, mainWindowController;
    private static IModel model = new AdminPanelModel();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        loadWindowLayouts();
        showLoginWindow();
    }

    static void showLoginWindow()
    {
        model.detachControllers();
        model.attachController(loginController);

        Stage loginStage = new Stage();
        loginStage.setTitle("Pannello di amministrazione");
        loginStage.setResizable(false);
        loginStage.setScene(loginScene);
        loginStage.show();
    }

    static void showMainWindow()
    {
        model.detachControllers();
        model.attachController(mainWindowController);

        Stage mainStage = new Stage();
        mainStage.setScene(mainWindowScene);
        mainStage.setTitle("Pannello di amministrazione");
        mainStage.setResizable(false);
        mainStage.show();
    }

    private void loadWindowLayouts() throws IOException
    {
        FXMLLoader loader = new FXMLLoader(AdminPanelApp.class.getResource("views/login.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();

        loader = new FXMLLoader(AdminPanelApp.class.getResource("views/mainWindow.fxml"));
        mainWindowScene = new Scene(loader.load());
        mainWindowController = loader.getController();
    }

    static AdminPanelModel getModel() {
        return (AdminPanelModel) model;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
