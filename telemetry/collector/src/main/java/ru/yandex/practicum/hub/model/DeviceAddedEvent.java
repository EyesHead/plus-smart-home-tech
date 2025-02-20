package ru.yandex.practicum.hub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Событие добавления устройства
@Getter
@Setter
@NoArgsConstructor
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}

