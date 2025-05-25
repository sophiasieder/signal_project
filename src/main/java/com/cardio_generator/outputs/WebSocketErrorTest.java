package com.cardio_generator.outputs;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class WebSocketErrorTest {
    @Test
    public void testWebSocketConnectionFailsGracefully() {
        DataStorage storage = DataStorage.getInstance();
        WebSocketDataReader reader = new WebSocketDataReader(storage, "ws://localhost:12345");

        assertDoesNotThrow(() -> {
            reader.readData(); // no exception, if no connection
        });
    }

    @Test
    public void testInvalidURIFormatThrowsRuntimeException() {
        DataStorage storage = DataStorage.getInstance();

        // runtime exception
        assertThrows(RuntimeException.class, () -> {
            new WebSocketDataReader(storage, "ht!tp:/invalid-url");  // on purpose
        });
    }

}
