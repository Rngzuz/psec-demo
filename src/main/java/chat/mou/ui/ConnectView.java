package chat.mou.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("singleton")
public class ConnectView extends VBox
{
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public ConnectView(ApplicationEventPublisher eventPublisher)
    {
        final var loader = new FXMLLoader(getClass().getResource("/ConnectView.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void handleConnect()
    {
        eventPublisher.publishEvent(new ViewEvent(ConnectView.class.getSimpleName(),
            ChannelView.class.getSimpleName()
        ));
    }
}
