package com.example.fas.mapper;

import java.util.List;
import java.util.Set;

public interface EntityMapper<D, E, R> {
    E toEntity(R requestDto);

    D toDto(E entity);

    Set<D> toDtoSet(List<E> entityList);
}
