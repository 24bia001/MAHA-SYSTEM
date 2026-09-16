package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.PaymentRequest;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import com.codelearn.houseselling.entity.Payment;
import com.codelearn.houseselling.entity.PaymentStatus;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.PaymentRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SellerRepository sellerRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            SellerRepository sellerRepository) {

        this.paymentRepository =
                paymentRepository;

        this.bookingRepository =
                bookingRepository;

        this.sellerRepository =
                sellerRepository;
    }

    public PaymentResponse createPayment(
            PaymentRequest request) {

        Seller seller =
                getLoggedInSeller();

        Booking booking =
                bookingRepository
                        .findById(
                                request.getBookingId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Booking not found with id: "
                                                + request.getBookingId()
                                )
                        );

        if (!booking.getHouse()
                .getSeller()
                .getSellerId()
                .equals(
                        seller.getSellerId()
                )) {

            throw new AccessDeniedException(
                    "You cannot create a payment for another seller's booking"
            );
        }

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "Payment can only be recorded for a CONFIRMED booking"
            );
        }

        if (request.getStatus()
                == PaymentStatus.PAID
                && paymentRepository
                .existsByBookingBookingIdAndStatus(
                        booking.getBookingId(),
                        PaymentStatus.PAID
                )) {

            throw new IllegalArgumentException(
                    "This booking already has a PAID payment"
            );
        }

        Payment payment =
                new Payment();

        payment.setAmount(
                request.getAmount()
        );

        payment.setPaymentDate(
                request.getPaymentDate()
        );

        payment.setPaymentMethod(
                request.getPaymentMethod()
        );

        payment.setStatus(
                request.getStatus()
        );

        payment.setBooking(
                booking
        );

        Payment savedPayment =
                paymentRepository.save(
                        payment
                );

        return convertToResponse(
                savedPayment
        );
    }

    public List<PaymentResponse>
    getAllPayments() {

        Seller seller =
                getLoggedInSeller();

        return paymentRepository
                .findByBookingHouseSellerSellerId(
                        seller.getSellerId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public PaymentResponse getPaymentById(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Payment payment =
                paymentRepository
                        .findByPaymentIdAndBookingHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (payment == null) {
            return null;
        }

        return convertToResponse(
                payment
        );
    }

    public PaymentResponse updatePayment(
            Long id,
            PaymentRequest request) {

        Seller seller =
                getLoggedInSeller();

        Payment existingPayment =
                paymentRepository
                        .findByPaymentIdAndBookingHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (existingPayment == null) {
            return null;
        }

        if (existingPayment.getStatus()
                == PaymentStatus.PAID) {

            throw new IllegalArgumentException(
                    "PAID payment cannot be modified"
            );
        }

        Booking booking =
                bookingRepository
                        .findById(
                                request.getBookingId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Booking not found with id: "
                                                + request.getBookingId()
                                )
                        );

        if (!booking.getHouse()
                .getSeller()
                .getSellerId()
                .equals(
                        seller.getSellerId()
                )) {

            throw new AccessDeniedException(
                    "You cannot use another seller's booking"
            );
        }

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "Payment can only be recorded for a CONFIRMED booking"
            );
        }

        if (request.getStatus()
                == PaymentStatus.PAID
                && paymentRepository
                .existsByBookingBookingIdAndStatusAndPaymentIdNot(
                        booking.getBookingId(),
                        PaymentStatus.PAID,
                        id
                )) {

            throw new IllegalArgumentException(
                    "This booking already has another PAID payment"
            );
        }

        existingPayment.setAmount(
                request.getAmount()
        );

        existingPayment.setPaymentDate(
                request.getPaymentDate()
        );

        existingPayment.setPaymentMethod(
                request.getPaymentMethod()
        );

        existingPayment.setStatus(
                request.getStatus()
        );

        existingPayment.setBooking(
                booking
        );

        Payment updatedPayment =
                paymentRepository.save(
                        existingPayment
                );

        return convertToResponse(
                updatedPayment
        );
    }

    public boolean deletePayment(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Payment payment =
                paymentRepository
                        .findByPaymentIdAndBookingHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (payment == null) {
            return false;
        }

        if (payment.getStatus()
                == PaymentStatus.PAID) {

            throw new IllegalArgumentException(
                    "PAID payment cannot be deleted"
            );
        }

        paymentRepository.delete(
                payment
        );

        return true;
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

    private PaymentResponse convertToResponse(
            Payment payment) {

        PaymentResponse response =
                new PaymentResponse();

        response.setPaymentId(
                payment.getPaymentId()
        );

        response.setAmount(
                payment.getAmount()
        );

        response.setPaymentDate(
                payment.getPaymentDate()
        );

        response.setPaymentMethod(
                payment.getPaymentMethod()
        );

        response.setStatus(
                payment.getStatus()
        );

        if (payment.getBooking() != null) {

            response.setBookingId(
                    payment.getBooking()
                            .getBookingId()
            );

            response.setBookingDate(
                    payment.getBooking()
                            .getBookingDate()
            );

            if (payment.getBooking()
                    .getStatus() != null) {

                response.setBookingStatus(
                        payment.getBooking()
                                .getStatus()
                                .name()
                );
            }
        }

        return response;
    }
}