package eyeshead.smarthome.aggregator.kafka.deserializer;

import eyes.head.smarthome.avro.serialization.GeneralAvroDeserializer;
import eyeshead.smarthome.kafka.telemetry.event.SensorEventAvro;

public class SensorEventDeserializer extends GeneralAvroDeserializer<SensorEventAvro> {
    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}
