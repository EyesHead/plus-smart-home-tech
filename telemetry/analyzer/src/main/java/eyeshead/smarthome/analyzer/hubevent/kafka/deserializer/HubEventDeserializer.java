package eyeshead.smarthome.analyzer.hubevent.kafka.deserializer;

import eyes.head.smarthome.avro.serialization.GeneralAvroDeserializer;
import eyeshead.smarthome.kafka.telemetry.event.HubEventAvro;

public class HubEventDeserializer extends GeneralAvroDeserializer<HubEventAvro> {
    public HubEventDeserializer() {
        super(HubEventAvro.getClassSchema());
    }
}