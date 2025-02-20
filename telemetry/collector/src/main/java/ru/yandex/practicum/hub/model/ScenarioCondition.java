package ru.yandex.practicum.hub.model;

import lombok.*;

// Условие сценария
@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private OperationType operation;
    private Integer value;
}
