package eyeshead.smarthome.analyzer.snapshot.kafka.deserializer;

import eyes.head.smarthome.avro.serialization.GeneralAvroDeserializer;
import eyeshead.smarthome.kafka.telemetry.event.SensorsSnapshotAvro;

public class SnapshotDeserializer extends GeneralAvroDeserializer<SensorsSnapshotAvro> {
    public SnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}