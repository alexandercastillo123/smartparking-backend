package com.smartparking.Smartparking.repository.penalty;

import com.smartparking.Smartparking.entity.penalty.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuspensionRepository extends JpaRepository<Suspension, String> {
    List<Suspension> findByUserIdAndStatus(String userId, Suspension.Status status);
}