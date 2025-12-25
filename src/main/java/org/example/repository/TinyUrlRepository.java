package org.example.repository;


import org.example.model.entity.TinyUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TinyUrlRepository extends JpaRepository<TinyUrlEntity, Long> {
    Optional<TinyUrlEntity> findByCode(String code);
    boolean existsByCode(String code);
}
