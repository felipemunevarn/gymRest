package com.epam.gym.workload.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    @PostMapping
    public String validate(@RequestBody @Valid DummyRequest request) {
        return "OK";
    }

    @PostMapping("/illegal")
    public String throwIllegal() {
        throw new IllegalArgumentException("Illegal argument triggered");
    }

    @PostMapping("/general")
    public String throwGeneral() {
        throw new RuntimeException("Unexpected failure");
    }

    public static class DummyRequest {
        @NotBlank(message = "must not be blank")
        public String name;
    }
}
