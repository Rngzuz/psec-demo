package chat.mou;

import chat.mou.events.ViewEvent;
import chat.mou.network.ConnectionManager;
import chat.mou.views.ClientView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CompletableFuture;

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
        rootScene = new Scene(applicationContext.getBean(ClientView.class), 840, 680);

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
        CompletableFuture.runAsync(() -> {
            final var connectionManager = applicationContext.getBean(ConnectionManager.class);
            connectionManager.closeSocketConnection();
        }).thenRun(() -> {
            applicationContext.close();
            Platform.exit();
        });
    }

    private void onViewEvent(ViewEvent event)
    {
        rootScene.setRoot(applicationContext.getBean(event.getView()));
    }
}
