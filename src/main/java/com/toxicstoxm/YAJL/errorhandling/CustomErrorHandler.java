package com.toxicstoxm.YAJL.errorhandling;

import com.toxicstoxm.YAJL.core.Logger;

public interface CustomErrorHandler {
    void handle(Logger logger, Class<?> originClass, String extraInfo);
}
