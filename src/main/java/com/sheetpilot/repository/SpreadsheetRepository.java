package com.sheetpilot.repository;

import com.sheetpilot.model.Spreadsheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpreadsheetRepository extends JpaRepository<Spreadsheet, Long> {

    List<Spreadsheet> findByNameContainingIgnoreCase(String name);

    List<Spreadsheet> findByUploadedAtBetween(Instant start, Instant end);

    List<Spreadsheet> findByOrderByUploadedAtDesc();
}
