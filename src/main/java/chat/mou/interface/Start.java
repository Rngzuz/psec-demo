package chat.mou.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class Start extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Mou Client");

        final var scene = new Scene(context.getBean(ConnectView.class), 840, 680);

        context.addApplicationListener(event -> {
            if (event instanceof ViewEvent) {
                final var nextViewName = ((ViewEvent) event).getNextViewName();
                System.out.println(nextViewName);

                if (nextViewName.equals("ConnectView")) {
                    scene.setRoot(context.getBean(ConnectView.class));
                }
                else if (nextViewName.equals("ChannelView")) {
                    scene.setRoot(context.getBean(ChannelView.class));
                }
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
