package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.CheckParams;
import io.github.kovalev.specificationhelper.utils.Expressions;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

public class LessThanOrEqualTo<E, C extends Comparable<? super C>>
        implements CustomSpecification<E> {

    private final transient C value;
    private final String[] fields;

    public LessThanOrEqualTo(C value, String... fields) {
        this.value = value;
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(value, fields).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<E, C>(root, fields).path();
                val expression = (Expression<C>) new Expressions().get(cb, path, value);
                return cb.lessThanOrEqualTo(expression, value);
            };
        }

        return new Empty<>();
    }
}
