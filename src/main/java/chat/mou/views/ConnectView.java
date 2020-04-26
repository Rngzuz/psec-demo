package chat.mou.views;

import chat.mou.events.ConnectEvent;
import chat.mou.events.ErrorEvent;
import chat.mou.events.ViewEvent;
import chat.mou.network.ClientSocketConnection;
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
import java.net.InetSocketAddress;
import java.net.URI;

@Component
@Scope("singleton")
public class ConnectView extends VBox
{
    private final ApplicationEventPublisher eventPublisher;
    private final SocketConnectionManager connectionManager;
    private final ClientSocketConnection socketConnection;

    @FXML
    private TextField ipAddress;

    @Autowired
    public ConnectView(
        ApplicationEventPublisher eventPublisher,
        SocketConnectionManager connectionManager,
        ClientSocketConnection socketConnection
    )
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
        this.connectionManager = connectionManager;
        this.socketConnection = socketConnection;
    }

    @FXML
    public void handleSubmit(MouseEvent event)
    {
        final var ipAddressText = ipAddress.getText();

        try {
            setDisable(true);

            final var uri = URI.create("scheme://" + ipAddressText);

            if (uri.getHost() != null && uri.getPort() != -1) {
                final var address = new InetSocketAddress(uri.getHost(), uri.getPort());

                connectionManager.setSocketConnection(socketConnection);
                connectionManager.openSocketConnection(address);
            }
        }
        catch (IllegalArgumentException exception) {
            // Invalid ip address
            exception.printStackTrace();
            setDisable(false);
        }
    }

    @EventListener
    public void onConnectEvent(ConnectEvent event)
    {
        setDisable(false);
        eventPublisher.publishEvent(new ViewEvent(this, ChannelView.class));
    }

    @EventListener
    public void onErrorEvent(ErrorEvent event)
    {
        if (event.getType().equals(ErrorEvent.Type.CONNECT_ERROR)) {
            setDisable(false);
        }
    }
}
