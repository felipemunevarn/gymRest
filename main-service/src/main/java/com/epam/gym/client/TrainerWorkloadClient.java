package com.epam.gym.client;

import com.epam.gym.dto.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "trainer-workload-service", fallback = TrainerWorkloadClientFallback.class)
public interface TrainerWorkloadClient {

    @PostMapping("/api/v1/trainers/workload")
    ResponseEntity<Void> updateTrainerWorkload(
            @RequestBody TrainerWorkloadRequest request,
            @RequestHeader("Authorization") String authToken
    );
}
