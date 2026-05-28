package com.nms.repository;

import com.nms.model.NetworkMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NetworkMetricRepository extends MongoRepository<NetworkMetric, String> {
    List<NetworkMetric> findByDeviceId(String deviceId);
    List<NetworkMetric> findByDeviceIdAndTimestampBetween(
            String deviceId, LocalDateTime start, LocalDateTime end
    );
    List<NetworkMetric> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);
}