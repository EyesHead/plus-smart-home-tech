package eyeshead.smarthome.analyzer.hubevent.service;

import eyeshead.smarthome.analyzer.hubevent.model.Action;
import eyeshead.smarthome.analyzer.hubevent.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ActionService {
    private final ActionRepository actionRepository;

    @Transactional
    public void saveAll(Collection<Action> actions) {
        actionRepository.saveAll(actions);
    }
}
