package com.andrei.demo.repository;

import com.andrei.demo.model.GamePatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GamePatchRepository extends JpaRepository<GamePatch, UUID> {
    List<GamePatch> findByVideoGameIdOrderByReleaseDateAsc(UUID videoGameId);
}