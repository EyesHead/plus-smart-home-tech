package ru.yandex.practicum.hub.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// Событие удаления сценария
@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    @NotBlank
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}
