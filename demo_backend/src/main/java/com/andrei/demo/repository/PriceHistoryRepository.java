package com.andrei.demo.repository;

import com.andrei.demo.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {
    // Aducem prețurile în ordine cronologică (ASC) pentru a arăta corect pe grafic
    List<PriceHistory> findByVideoGameIdOrderByRecordedAtAsc(UUID videoGameId);
}