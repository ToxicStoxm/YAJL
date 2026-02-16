package com.toxicstoxm.YAJL.errorhandling;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorHandling {
    String customMessage() default "";
}
