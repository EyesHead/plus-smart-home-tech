package eyeshead.smarthome.analyzer.hubevent.factory;

public interface ScenarioComponentFactory<T, R> {
    T create(R request);
}