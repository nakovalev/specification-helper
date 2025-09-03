package io.github.kovalev.specificationhelper.enums;

/**
 * Настройка wildcard для оператора Like
 */
public enum LikeMatchMode {
    /**
     * like '%value%'
     */
    BOTH,

    /**
     * like '%value'
     */
    START_ONLY,

    /**
     * like 'value%'
     */
    END_ONLY,

    /**
     * like 'value'
     */
    NONE
}
