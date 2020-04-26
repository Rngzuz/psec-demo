package chat.mou.views;

import chat.mou.events.ViewEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
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
            // TODO: Handle ConnectView.fxml loading error better
            throw new RuntimeException(exception);
        }

        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void dispatchViewEvent(MouseEvent event)
    {
        eventPublisher.publishEvent(new ViewEvent(this, ChannelView.class));
    }
}
