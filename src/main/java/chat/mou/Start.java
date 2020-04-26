package chat.mou;

import chat.mou.events.ViewEvent;
import chat.mou.views.ConnectView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class Start extends Application
{
    private ConfigurableApplicationContext applicationContext;
    private Scene rootScene;

    @Override
    public void init()
    {
        applicationContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage stage)
    {
        stage.setTitle("Mou");
        rootScene = new Scene(applicationContext.getBean(ConnectView.class), 840, 680);

        applicationContext.addApplicationListener(event -> {
            if (event instanceof ViewEvent) {
                onViewEvent((ViewEvent) event);
            }
        });

        stage.setScene(rootScene);
        stage.show();
    }

    @Override
    public void stop()
    {
        applicationContext.close();
        Platform.exit();
    }

    private void onViewEvent(ViewEvent event)
    {
        rootScene.setRoot(applicationContext.getBean(event.getView()));
    }
}
