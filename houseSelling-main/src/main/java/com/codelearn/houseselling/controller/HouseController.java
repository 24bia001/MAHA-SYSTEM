package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.HouseRequest;
import com.codelearn.houseselling.dto.HouseResponse;
import com.codelearn.houseselling.service.HouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/houses")
public class HouseController {

    private final HouseService houseService;

    public HouseController(
            HouseService houseService) {

        this.houseService = houseService;
    }

    @PostMapping
    public ResponseEntity<HouseResponse> createHouse(
            @Valid @RequestBody HouseRequest request) {

        HouseResponse response =
                houseService.createHouse(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<HouseResponse>>
    getAllHouses() {

        return ResponseEntity.ok(
                houseService.getAllHouses()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HouseResponse>
    getHouseById(
            @PathVariable Long id) {

        HouseResponse response =
                houseService.getHouseById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HouseResponse>
    updateHouse(
            @PathVariable Long id,
            @Valid @RequestBody HouseRequest request) {

        HouseResponse response =
                houseService.updateHouse(
                        id,
                        request
                );

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHouse(
            @PathVariable Long id) {

        boolean deleted =
                houseService.deleteHouse(id);

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}