package io.github.kovalev.specificationhelper.specifications;


import io.github.kovalev.specificationhelper.utils.CheckFields;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

public class IsNull<E> implements CustomSpecification<E> {

    private final String[] fields;

    public IsNull(String... fields) {
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckFields(fields).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<>(root, fields).path();
                return cb.isNull(path);
            };
        }

        return new Empty<>();
    }
}
