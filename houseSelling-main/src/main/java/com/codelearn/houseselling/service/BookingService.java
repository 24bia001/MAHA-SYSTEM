package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.BookingRequest;
import com.codelearn.houseselling.dto.BookingResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.PaymentStatus;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.CustomerRepository;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.PaymentRepository;
import com.codelearn.houseselling.repository.SaleRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private static final String SOLD_STATUS =
            "SOLD";

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final HouseRepository houseRepository;
    private final SaleRepository saleRepository;
    private final SellerRepository sellerRepository;
    private final PaymentRepository paymentRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            HouseRepository houseRepository,
            SaleRepository saleRepository,
            SellerRepository sellerRepository,
            PaymentRepository paymentRepository) {

        this.bookingRepository =
                bookingRepository;

        this.customerRepository =
                customerRepository;

        this.houseRepository =
                houseRepository;

        this.saleRepository =
                saleRepository;

        this.sellerRepository =
                sellerRepository;

        this.paymentRepository =
                paymentRepository;
    }

    public BookingResponse createBooking(
            BookingRequest request) {

        Seller seller =
                getLoggedInSeller();

        Customer customer =
                customerRepository
                        .findById(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found with id: "
                                                + request.getCustomerId()
                                )
                        );

        House house =
                houseRepository
                        .findById(
                                request.getHouseId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "House not found with id: "
                                                + request.getHouseId()
                                )
                        );

        if (!house.getSeller()
                .getSellerId()
                .equals(
                        seller.getSellerId()
                )) {

            throw new AccessDeniedException(
                    "You cannot create a booking for another seller's house"
            );
        }

        if ("SOLD_OUT".equalsIgnoreCase(house.getStatus()) || saleRepository
                .existsByHouseHouseIdAndStatus(
                        house.getHouseId(),
                        SOLD_STATUS
                )) {

            throw new IllegalArgumentException(
                    "House has already been sold"
            );
        }

        List<BookingStatus> activeStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        if (request.getStatus()
                == BookingStatus.PENDING
                || request.getStatus()
                == BookingStatus.CONFIRMED) {

            boolean duplicate =
                    bookingRepository
                            .existsByHouseHouseIdAndBookingDateAndBookingTimeAndStatusIn(
                                    house.getHouseId(),
                                    request.getBookingDate(),
                                    request.getBookingTime(),
                                    activeStatuses
                            );

            if (duplicate) {

                throw new IllegalArgumentException(
                        "House already has an active booking at this date and time"
                );
            }
        }

        Booking booking =
                new Booking();

        booking.setBookingDate(
                request.getBookingDate()
        );

        booking.setBookingTime(
                request.getBookingTime()
        );

        booking.setStatus(
                request.getStatus()
        );

        booking.setCustomer(
                customer
        );

        booking.setHouse(
                house
        );

        Booking savedBooking =
                bookingRepository.save(
                        booking
                );

        return convertToResponse(
                savedBooking
        );
    }

    public List<BookingResponse>
    getAllBookings() {

        Seller seller =
                getLoggedInSeller();

        return bookingRepository
                .findByHouseSellerSellerId(
                        seller.getSellerId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public BookingResponse getBookingById(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return null;
        }

        return convertToResponse(
                booking
        );
    }

    public BookingResponse updateBooking(
            Long id,
            BookingRequest request) {

        Seller seller =
                getLoggedInSeller();

        Booking existingBooking =
                bookingRepository
                        .findByBookingIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (existingBooking == null) {
            return null;
        }

        if (paymentRepository
                .existsByBookingBookingIdAndStatus(
                        id,
                        PaymentStatus.PAID
                )) {

            throw new IllegalArgumentException(
                    "Paid booking cannot be updated"
            );
        }

        if (existingBooking.getStatus()
                == BookingStatus.CANCELLED
                && request.getStatus()
                != BookingStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cancelled booking cannot be reopened"
            );
        }

        Customer customer =
                customerRepository
                        .findById(
                                request.getCustomerId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found with id: "
                                                + request.getCustomerId()
                                )
                        );

        House house =
                houseRepository
                        .findById(
                                request.getHouseId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "House not found with id: "
                                                + request.getHouseId()
                                )
                        );

        if (!house.getSeller()
                .getSellerId()
                .equals(
                        seller.getSellerId()
                )) {

            throw new AccessDeniedException(
                    "You cannot move a booking to another seller's house"
            );
        }

        if ("SOLD_OUT".equalsIgnoreCase(house.getStatus()) || saleRepository
                .existsByHouseHouseIdAndStatus(
                        house.getHouseId(),
                        SOLD_STATUS
                )) {

            throw new IllegalArgumentException(
                    "House has already been sold"
            );
        }

        List<BookingStatus> activeStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        if (request.getStatus()
                == BookingStatus.PENDING
                || request.getStatus()
                == BookingStatus.CONFIRMED) {

            boolean duplicate =
                    bookingRepository
                            .existsByHouseHouseIdAndBookingDateAndBookingTimeAndStatusInAndBookingIdNot(
                                    house.getHouseId(),
                                    request.getBookingDate(),
                                    request.getBookingTime(),
                                    activeStatuses,
                                    id
                            );

            if (duplicate) {

                throw new IllegalArgumentException(
                        "House already has another active booking at this date and time"
                );
            }
        }

        existingBooking.setBookingDate(
                request.getBookingDate()
        );

        existingBooking.setBookingTime(
                request.getBookingTime()
        );

        existingBooking.setStatus(
                request.getStatus()
        );

        existingBooking.setCustomer(
                customer
        );

        existingBooking.setHouse(
                house
        );

        Booking updatedBooking =
                bookingRepository.save(
                        existingBooking
                );

        return convertToResponse(
                updatedBooking
        );
    }

    // =========================
    // SELLER CONFIRMS BOOKING
    // =========================

    public BookingResponse confirmBooking(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return null;
        }

        if (booking.getStatus()
                == BookingStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cancelled booking cannot be confirmed"
            );
        }

        if (saleRepository
                .existsByHouseHouseIdAndStatus(
                        booking.getHouse()
                                .getHouseId(),
                        SOLD_STATUS
                )) {

            throw new IllegalArgumentException(
                    "House has already been sold"
            );
        }

        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        Booking updatedBooking =
                bookingRepository.save(
                        booking
                );

        return convertToResponse(
                updatedBooking
        );
    }

    // =========================
    // SELLER CANCELS BOOKING
    // =========================

    public BookingResponse cancelBooking(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return null;
        }

        if (paymentRepository
                .existsByBookingBookingIdAndStatus(
                        id,
                        PaymentStatus.PAID
                )) {

            throw new IllegalArgumentException(
                    "Paid booking cannot be cancelled"
            );
        }

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        Booking updatedBooking =
                bookingRepository.save(
                        booking
                );

        return convertToResponse(
                updatedBooking
        );
    }

    public boolean deleteBooking(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return false;
        }

        if (paymentRepository
                .existsByBookingBookingId(id)) {

            throw new IllegalArgumentException(
                    "Booking cannot be deleted because it has payments"
            );
        }

        bookingRepository.delete(
                booking
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

    private BookingResponse convertToResponse(
            Booking booking) {

        BookingResponse response =
                new BookingResponse();

        response.setBookingId(
                booking.getBookingId()
        );

        response.setBookingDate(
                booking.getBookingDate()
        );

        response.setBookingTime(
                booking.getBookingTime()
        );

        response.setStatus(
                booking.getStatus()
        );

        if (booking.getCustomer() != null) {

            response.setCustomerId(
                    booking.getCustomer()
                            .getCustomerId()
            );

            response.setCustomerName(
                    booking.getCustomer()
                            .getName()
            );
        }

        if (booking.getHouse() != null) {

            response.setHouseId(
                    booking.getHouse()
                            .getHouseId()
            );

            response.setHouseTitle(
                    booking.getHouse()
                            .getTitle()
            );
        }

        return response;
    }
}