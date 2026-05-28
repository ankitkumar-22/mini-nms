package com.nms.repository;

import com.nms.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
    Optional<Device> findByIpAddress(String ipAddress);
    boolean existsByIpAddress(String ipAddress);
}