package _07_Phy;

import java.util.*;
import java.math.*;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class _01_exeTest {
    
    @InjectMocks
    private _01_exe _01_exe;
    
    private MockedStatic<StreamExecutionEnvironment> mockedEnv;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockedEnv = mockStatic(StreamExecutionEnvironment.class);
    }
    
    @Test
    void main_DoesNotThrowException() {
        // Arrange
        StreamExecutionEnvironment mockEnv = Mockito.mock(StreamExecutionEnvironment.class);
        mockedEnv.when(StreamExecutionEnvironment :: getExecutionEnvironment).thenReturn(mockEnv);
        Mockito.when(mockEnv.fromElements(Mockito.any(Integer[].class))).thenReturn(Mockito.mock(DataStreamSource.class));
        Mockito.doNothing().when(mockEnv).execute();
        
        // Act & Assert
        assertDoesNotThrow(() -> _01_exe.main(new String[]{}));
        
        // Cleanup
        mockedEnv.close();
    }
}
