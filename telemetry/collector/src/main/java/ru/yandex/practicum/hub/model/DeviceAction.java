package ru.yandex.practicum.hub.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

// Действие устройства
@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Integer value;
}
