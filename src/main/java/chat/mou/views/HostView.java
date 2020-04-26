package chat.mou.views;

import chat.mou.events.AcceptEvent;
import chat.mou.events.ConnectEvent;
import chat.mou.events.ErrorEvent;
import chat.mou.events.ViewEvent;
import chat.mou.network.ClientSocketConnection;
import chat.mou.network.HostSocketConnection;
import chat.mou.network.SocketConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

@Component
@Scope("singleton")
public class HostView extends VBox
{
    private final ApplicationEventPublisher eventPublisher;
    private final SocketConnectionManager connectionManager;
    private final HostSocketConnection socketConnection;

    @FXML
    private TextField port;

    @Autowired
    public HostView(
        ApplicationEventPublisher eventPublisher,
        SocketConnectionManager connectionManager,
        HostSocketConnection socketConnection
    )
    {
        final var loader = new FXMLLoader(getClass().getResource("/HostView.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        try {
            loader.load();
        }
        catch (IOException exception) {
            // TODO: Handle HostView.fxml loading error better
            throw new RuntimeException(exception);
        }

        this.eventPublisher = eventPublisher;
        this.connectionManager = connectionManager;
        this.socketConnection = socketConnection;
    }

    @FXML
    public void handleSubmit(MouseEvent event)
    {
        final var portText = port.getText();

        try {
            setDisable(true);

            final var port = Integer.parseInt(portText);
            connectionManager.setSocketConnection(socketConnection);
            connectionManager.openSocketConnection(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
        }
        catch (NumberFormatException exception) {
            // Invalid ip address
            exception.printStackTrace();
            setDisable(false);
        }
    }

    @EventListener
    public void onConnectEvent(AcceptEvent event)
    {
        setDisable(false);
        eventPublisher.publishEvent(new ViewEvent(this, ChannelView.class));
    }

    @EventListener
    public void onErrorEvent(ErrorEvent event)
    {
        if (event.getType().equals(ErrorEvent.Type.ACCEPT_ERROR)) {
            setDisable(false);
        }
    }
}
