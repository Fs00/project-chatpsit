package chatpsit.adminpanel;

import com.gluonhq.particle.application.ParticleApplication;
import javafx.scene.Scene;
import static org.controlsfx.control.action.ActionMap.actions;

public class Main extends ParticleApplication {

    public Main() {
        super("Gluon Desktop Application");
    }

    @Override
    public void postInit(Scene scene) {
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        setTitle("Gluon Desktop Application");

        getParticle().buildMenu("File -> [signin,---, exit]", "Help -> [about]");
        
        getParticle().getToolBarActions().addAll(actions("signin"));
    }
}