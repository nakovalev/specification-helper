package io.github.kovalev.specificationhelper.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class SortUtils {

    private final List<SortParams> sortParams;

    public Sort sort() {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>(sortParams.size());

        for (SortParams param : sortParams) {
            orders.add(new Sort.Order(param.getDirection(), param.getField()));
        }

        return Sort.by(orders);
    }
}
