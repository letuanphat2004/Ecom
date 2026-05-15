package com.ecom.repository;

import com.ecom.entity.InventoryMovement;
import java.util.List;
    import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findTop50ByOrderByCreatedAtDesc();

    List<InventoryMovement> findTop50ByProductIdOrderByCreatedAtDesc(Long productId);
}
