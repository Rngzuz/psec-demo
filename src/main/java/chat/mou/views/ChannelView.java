package chat.mou.views;

import chat.mou.events.*;
import chat.mou.network.ConnectionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    private final ApplicationEventPublisher eventPublisher;
    private final ConnectionManager connectionManager;

    @FXML
    private Label title;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox vBox;

    @Autowired
    public ChannelView(
        ApplicationEventPublisher eventPublisher, ConnectionManager connectionManager
    )
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
        this.connectionManager = connectionManager;

        // Scroll to bottom when a new message is added
        vBox.heightProperty().addListener(observable -> {
            scrollPane.setVvalue(1);
        });
    }

    private void addMessage(String message)
    {
        vBox.getChildren().add(new Label(message));
    }

    @FXML
    public void dispatchCloseEvent()
    {
        connectionManager.closeSocketConnection();

        if (connectionManager.isHost()) {
            eventPublisher.publishEvent(new ViewEvent(this, HostView.class));
        }
        else {
            eventPublisher.publishEvent(new ViewEvent(this, ClientView.class));
        }
    }

    @FXML
    public void dispatchMessageEvent(KeyEvent event)
    {
        if (event.getCode() == KeyCode.ENTER) {
            final var textField = (TextField) event.getTarget();

            if (!textField.getText().isEmpty()) {
                addMessage(textField.getText());
                eventPublisher.publishEvent(new MessageEvent(this, textField.getText()));
                textField.clear();
            }
        }
    }

    @EventListener
    public void onReadEvent(ReadEvent event)
    {
        Platform.runLater(() -> {
            addMessage(new String(event.getBody()));
        });
    }

    @EventListener
    public void onErrorEvent(ErrorEvent event)
    {
        if (event.getType().equals(ErrorEvent.Type.READ_ERROR)) {
            dispatchCloseEvent();
        }
    }

    @EventListener(classes = {AcceptEvent.class, ConnectEvent.class})
    public void onAcceptAndConnect()
    {
        if (connectionManager.isOpen()) {

            final var titleText = (connectionManager.isHost() ? "Hosted on " : "Connected to ") +
                connectionManager.getSocketConnection().getSimplifiedAddress();

            title.setText(titleText);
        }
    }
}
