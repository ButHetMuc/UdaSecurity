package com.udacity.catpoint.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.images.FakeImageService;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private FakeImageService imageService;

//    @InjectMocks
    private SecurityService securityService;

    @Mock
    private Sensor sensor1;

    @Mock
    private Sensor sensor2;

    @Mock
    private Sensor sensor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityService = new SecurityService(securityRepository,imageService);
    }



    @Test
    void whenAlarmIsArmedAndSensorIsActivated_setPendingAlarmStatus() {
        // Arrange
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(sensor1.getActive()).thenReturn(false);

        // Act
        securityService.changeSensorActivationStatus(sensor1, true);

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void whenPendingAlarmAndSensorActivated_setAlarmStatus() {
        // Arrange
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(sensor1.getActive()).thenReturn(false);

        // Act
        securityService.changeSensorActivationStatus(sensor1, true);

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenPendingAlarmAndAllSensorsInactive_setNoAlarmStatus() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(sensor1.getActive()).thenReturn(true);
        when(sensor2.getActive()).thenReturn(false);

        // Act
        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, true);
        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);  // Ensure NO_ALARM is set
    }

    @Test
    void whenAlarmIsActive_sensorChangeDoesNotAffectAlarmStatus() {
        // Arrange
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        // Act
        securityService.changeSensorActivationStatus(sensor1, true);

        // Assert
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void whenSensorAlreadyActiveInPendingState_setAlarmStatus() {
        // Arrange
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(sensor1.getActive()).thenReturn(true);

        // Act
        securityService.changeSensorActivationStatus(sensor1, true);

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenSensorDeactivatedWhileInactive_noAlarmStateChange() {
        // Arrange
        when(sensor1.getActive()).thenReturn(false);

        // Act
        securityService.changeSensorActivationStatus(sensor1, false);

        // Assert
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void whenCatDetectedAndSystemArmedHome_setAlarmStatus() {
        // Arrange
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Act
        securityService.processImage(mock(BufferedImage.class));

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenCatNotDetectedAndSensorsInactive_setNoAlarmStatus() {
        // Arrange
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor1));
        when(sensor1.getActive()).thenReturn(false);

        // Act
        securityService.processImage(mock(BufferedImage.class));

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void whenSystemIsDisarmed_setNoAlarmStatus() {
        // Act
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void whenSystemIsArmed_resetAllSensorsToInactive() {
        // Arrange
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor1, sensor2));
        when(sensor1.getActive()).thenReturn(true);
//        when(sensor2.getActive()).thenReturn(true);

        // Act
        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);

        // Assert
        verify(sensor1).setActive(false);
    }

    @Test
    void whenSystemArmedHomeAndCatDetected_setAlarmStatus() {
        // Arrange
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Act
        securityService.processImage(mock(BufferedImage.class));

        // Assert
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }


    @ParameterizedTest
    @MethodSource("provideSensorStatesAndTypes")
    void SensorStateChangeWhileAlarmIsActive_noEffectOnAlarmStatus(SensorType sensorType, boolean isActive) {

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        sensor.setSensorType(sensorType);
        sensor.setActive(isActive);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any());
    }
    private static Stream<Arguments> provideSensorStatesAndTypes() {
        return Stream.of(
                Arguments.of(SensorType.DOOR, false),
                Arguments.of(SensorType.WINDOW, false),
                Arguments.of(SensorType.MOTION, false),
                Arguments.of(SensorType.DOOR, true),
                Arguments.of(SensorType.WINDOW, true),
                Arguments.of(SensorType.MOTION, true)
        );
    }

    @Test
    void disarmThenCatDetectedThenArmHome_shouldSetAlarmStatus() {
        // Given the system is armed at home
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Mock the image service to return true (cat detected)
        when(imageService.imageContainsCat(any(BufferedImage.class), anyFloat())).thenReturn(true);

        // When a cat is detected in the image
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        securityService.processImage(image);

        // Then the system should go to alarm state
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }
}


