package eyeshead.smarthome.analyzer.hubevent.service;

import eyeshead.smarthome.analyzer.hubevent.model.Condition;
import eyeshead.smarthome.analyzer.hubevent.repository.ConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ConditionService {
    private final ConditionRepository conditionRepository;

    @Transactional
    public void saveAll(Collection<Condition> conditions) {
        conditionRepository.saveAll(conditions);
    }
}