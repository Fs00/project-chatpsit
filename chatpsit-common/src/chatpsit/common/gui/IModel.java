package chatpsit.common.gui;

public interface IModel
{
    void attachController(IController controller);
    void detachController(IController controller);
    void detachControllers();
}
