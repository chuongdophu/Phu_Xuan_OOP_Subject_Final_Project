package com.autoparking.service.impl;

import com.autoparking.core.interfaces.ITimeProvider;
import java.time.LocalDateTime;

public class RealTimeProviderImpl implements ITimeProvider {
    @Override
    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
}