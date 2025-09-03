package io.github.kovalev.specificationhelper.domain.jpa;

import io.github.kovalev.specificationhelper.domain.entity.ComparableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ComparableRepository extends JpaRepository<ComparableEntity, Long>, JpaSpecificationExecutor<ComparableEntity> {
}
