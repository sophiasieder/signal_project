package com.alerts;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SingletonTest {
    @Test
    public void testSingletonReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }
}

