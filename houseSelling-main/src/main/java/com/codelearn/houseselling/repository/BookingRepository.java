package com.codelearn.houseselling.repository;

import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    boolean existsByHouseHouseIdAndBookingDateAndStatusIn(
            Long houseId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );

    boolean existsByHouseHouseIdAndBookingDateAndBookingTimeAndStatusIn(
            Long houseId,
            LocalDate bookingDate,
            java.time.LocalTime bookingTime,
            List<BookingStatus> statuses
    );

    boolean existsByHouseHouseIdAndBookingDateAndBookingTimeAndStatusInAndBookingIdNot(
            Long houseId,
            LocalDate bookingDate,
            java.time.LocalTime bookingTime,
            List<BookingStatus> statuses,
            Long bookingId
    );

    boolean existsByHouseHouseIdAndBookingDateAndStatusInAndBookingIdNot(
            Long houseId,
            LocalDate bookingDate,
            List<BookingStatus> statuses,
            Long bookingId
    );

    boolean existsByCustomerCustomerId(
            Long customerId
    );

    // =========================
    // SELLER
    // =========================

    List<Booking> findByHouseSellerSellerId(
            Long sellerId
    );

    Optional<Booking> findByBookingIdAndHouseSellerSellerId(
            Long bookingId,
            Long sellerId
    );

    boolean existsByCustomerCustomerIdAndHouseSellerSellerId(
            Long customerId,
            Long sellerId
    );

    boolean existsByCustomerCustomerIdAndHouseSellerSellerIdNot(
            Long customerId,
            Long sellerId
    );

    // =========================
    // CUSTOMER
    // =========================

    List<Booking> findByCustomerCustomerId(
            Long customerId
    );

    Optional<Booking> findByBookingIdAndCustomerCustomerId(
            Long bookingId,
            Long customerId
    );

    // =========================
    // SALE WORKFLOW
    // =========================

    List<Booking> findByCustomerCustomerIdAndHouseHouseIdAndStatus(
            Long customerId,
            Long houseId,
            BookingStatus status
    );

    List<Booking> findByHouseHouseIdAndStatusIn(
            Long houseId,
            List<BookingStatus> statuses
    );
}