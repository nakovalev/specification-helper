package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.CheckParams;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class In<E, I> implements CustomSpecification<E> {

    private final transient Collection<I> values;
    private final String[] fields;

    public In(Collection<I> values, String... fields) {
        this.values = values;
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(values, fields).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<E, Collection<I>>(root, fields).path();
                return cb.in(path).value(values);
            };
        }

        return new Empty<>();
    }
}
