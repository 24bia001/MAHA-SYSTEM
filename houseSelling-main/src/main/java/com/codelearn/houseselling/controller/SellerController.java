package com.codelearn.houseselling.controller;

import com.codelearn.houseselling.dto.SellerRequest;
import com.codelearn.houseselling.dto.SellerResponse;
import com.codelearn.houseselling.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(
            SellerService sellerService) {

        this.sellerService = sellerService;
    }

    // LOGGED-IN SELLER PROFILE
    @GetMapping("/me")
    public ResponseEntity<SellerResponse>
    getMyProfile() {

        return ResponseEntity.ok(
                sellerService.getMyProfile()
        );
    }

    // UPDATE LOGGED-IN SELLER
    @PutMapping("/me")
    public ResponseEntity<SellerResponse>
    updateMyProfile(
            @Valid @RequestBody SellerRequest request) {

        return ResponseEntity.ok(
                sellerService.updateMyProfile(
                        request
                )
        );
    }

    // DELETE LOGGED-IN SELLER
    @DeleteMapping("/me")
    public ResponseEntity<Void>
    deleteMyProfile() {

        sellerService.deleteMyProfile();

        return ResponseEntity
                .noContent()
                .build();
    }
}