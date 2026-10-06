package com.hackerrank.sample.repository;

import com.hackerrank.sample.model.Model;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("modelRepository")
public interface ModelRepository extends JpaRepository<Model, Long> {
    void deleteById(Long id);

    List<Model> findAllByIdIn(List<Long> ids);

    List<Model> findByNameContainingIgnoreCase(String name);
}
