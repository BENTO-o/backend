package bento.backend.repository;

import bento.backend.domain.Summary;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
}
