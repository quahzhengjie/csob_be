package com.bkb.scanner.repository;

import com.bkb.scanner.entity.RelatedParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatedPartyRepository extends JpaRepository<RelatedParty, Long> {}