package com.astrelya.katabank.Repositories;

import com.astrelya.katabank.Entities.NotificationEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
}