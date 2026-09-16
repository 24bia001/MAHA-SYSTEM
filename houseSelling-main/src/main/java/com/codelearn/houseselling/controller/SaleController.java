package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.SaleRequest;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(
            SaleService saleService) {

        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse>
    createSale(
            @Valid @RequestBody
            SaleRequest request) {

        SaleResponse response =
                saleService.createSale(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>>
    getAllSales() {

        return ResponseEntity.ok(
                saleService.getAllSales()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse>
    getSaleById(
            @PathVariable Long id) {

        SaleResponse response =
                saleService.getSaleById(id);

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponse>
    updateSale(
            @PathVariable Long id,
            @Valid @RequestBody
            SaleRequest request) {

        SaleResponse response =
                saleService.updateSale(
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
    public ResponseEntity<Void>
    deleteSale(
            @PathVariable Long id) {

        boolean deleted =
                saleService.deleteSale(id);

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