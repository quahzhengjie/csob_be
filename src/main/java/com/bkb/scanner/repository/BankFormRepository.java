package com.bkb.scanner.repository;

import com.bkb.scanner.entity.BankForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BankFormRepository extends JpaRepository<BankForm, Long> {
    List<BankForm> findByCategoryOrderByDisplayOrder(String category);
    List<BankForm> findByIsMandatoryTrueOrderByDisplayOrder();
    List<BankForm> findByApplicableEntityTypesContainingOrderByDisplayOrder(String entityType);
}