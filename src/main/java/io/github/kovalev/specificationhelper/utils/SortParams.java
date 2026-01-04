package io.github.kovalev.specificationhelper.utils;

import org.springframework.data.domain.Sort;

public interface SortParams {

    String getField();

    Sort.Direction getDirection();
}
