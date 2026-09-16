package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.SellerRequest;
import com.codelearn.houseselling.dto.SellerResponse;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final HouseRepository houseRepository;
    private final PasswordEncoder passwordEncoder;

    public SellerService(
            SellerRepository sellerRepository,
            HouseRepository houseRepository,
            PasswordEncoder passwordEncoder) {

        this.sellerRepository = sellerRepository;
        this.houseRepository = houseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // PUBLIC REGISTRATION
    public SellerResponse createSeller(
            SellerRequest request) {

        if (sellerRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Seller email already exists: "
                            + request.getEmail()
            );
        }

        Seller seller = new Seller();

        seller.setName(request.getName());
        seller.setEmail(request.getEmail());
        seller.setPhone(request.getPhone());
        seller.setAddress(request.getAddress());
        seller.setNida(request.getNida());
        seller.setImage(request.getImage());

        seller.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Seller savedSeller =
                sellerRepository.save(seller);

        return convertToResponse(savedSeller);
    }

    // GET LOGGED-IN SELLER
    public SellerResponse getMyProfile() {

        Seller seller = getLoggedInSeller();

        return convertToResponse(seller);
    }

    // UPDATE LOGGED-IN SELLER
    public SellerResponse updateMyProfile(
            SellerRequest request) {

        Seller seller = getLoggedInSeller();

        if (sellerRepository
                .existsByEmailAndSellerIdNot(
                        request.getEmail(),
                        seller.getSellerId()
                )) {

            throw new IllegalArgumentException(
                    "Seller email already exists: "
                            + request.getEmail()
            );
        }

        seller.setName(request.getName());
        seller.setEmail(request.getEmail());
        seller.setPhone(request.getPhone());
        seller.setAddress(request.getAddress());
        seller.setNida(request.getNida());
        seller.setImage(request.getImage());

        seller.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Seller updatedSeller =
                sellerRepository.save(seller);

        return convertToResponse(updatedSeller);
    }

    // DELETE LOGGED-IN SELLER
    public void deleteMyProfile() {

        Seller seller = getLoggedInSeller();

        if (houseRepository
                .existsBySellerSellerId(
                        seller.getSellerId()
                )) {

            throw new IllegalArgumentException(
                    "Seller cannot be deleted because they still own houses"
            );
        }

        sellerRepository.delete(seller);
    }

    private Seller getLoggedInSeller() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Seller is not authenticated"
            );
        }

        String email =
                authentication.getName();

        return sellerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Logged-in seller not found"
                        )
                );
    }

    private SellerResponse convertToResponse(
            Seller seller) {

        SellerResponse response =
                new SellerResponse();

        response.setSellerId(
                seller.getSellerId()
        );

        response.setName(
                seller.getName()
        );

        response.setEmail(
                seller.getEmail()
        );

        response.setPhone(
                seller.getPhone()
        );

        response.setAddress(
                seller.getAddress()
        );

        response.setNida(
                seller.getNida()
        );

        response.setImage(seller.getImage());

        return response;
    }
}