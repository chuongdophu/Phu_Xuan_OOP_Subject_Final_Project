package com.autoparking.core.interfaces;

import java.time.LocalDateTime;

public interface ITimeProvider {
    LocalDateTime getCurrentTime();
}