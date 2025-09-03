package io.github.kovalev.specificationhelper.enums;

/**
 * Политика разрешения ситуаций, когда value == null.
 */
public enum NullHandling {
    /**
     * не добавлять where
     */
    IGNORE,

    /**
     * добавить where field is (not) null
     */
    USE_IS_NULL
}
