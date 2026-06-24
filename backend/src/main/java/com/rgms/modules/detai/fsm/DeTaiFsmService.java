package com.rgms.modules.detai.fsm;

import com.rgms.modules.detai.entity.DeTai;

public interface DeTaiFsmService {

    void transition(DeTai deTai, String action, Long actorId);

    boolean canTransition(DeTai deTai, String action);
}
