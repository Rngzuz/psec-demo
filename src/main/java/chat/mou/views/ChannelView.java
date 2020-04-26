package chat.mou.views;

import chat.mou.events.ConnectEvent;
import chat.mou.events.ReadEvent;
import chat.mou.events.ViewEvent;
import chat.mou.events.WriteEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("singleton")
public class ChannelView extends VBox
{
    private ApplicationEventPublisher eventPublisher;

    @FXML
    private VBox vBox;

    @Autowired
    public ChannelView(ApplicationEventPublisher eventPublisher)
    {
        final var loader = new FXMLLoader(getClass().getResource("/ChannelView.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        }
        catch (IOException exception) {
            // TODO: Handle ChannelView.fxml loading error better
            throw new RuntimeException(exception);
        }

        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void dispatchCloseEvent()
    {
        eventPublisher.publishEvent(new ViewEvent(this, ConnectView.class));
    }

    @FXML
    public void dispatchMessageEvent(KeyEvent event)
    {
        if (event.getCode() == KeyCode.ENTER) {
            final var textField = (TextField) event.getTarget();

            if (!textField.getText().isEmpty()) {
                vBox.getChildren().add(new Label(textField.getText()));
                textField.clear();
            }
        }

        // eventPublisher.publishEvent(new MessageEvent(this, body));
    }

    @EventListener
    public void onConnectEvent(ConnectEvent event)
    {

    }

    @EventListener
    public void onReadEvent(ReadEvent event)
    {

    }

    @EventListener
    public void onWriteEvent(WriteEvent event)
    {

    }
}
