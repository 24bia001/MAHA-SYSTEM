package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.SaleRequest;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.PaymentStatus;
import com.codelearn.houseselling.entity.Sale;
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
public class SaleService {

    private static final String SOLD_STATUS =
            "SOLD";

    private final SaleRepository saleRepository;
    private final HouseRepository houseRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public SaleService(
            SaleRepository saleRepository,
            HouseRepository houseRepository,
            CustomerRepository customerRepository,
            SellerRepository sellerRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository) {

        this.saleRepository =
                saleRepository;

        this.houseRepository =
                houseRepository;

        this.customerRepository =
                customerRepository;

        this.sellerRepository =
                sellerRepository;

        this.bookingRepository =
                bookingRepository;

        this.paymentRepository =
                paymentRepository;
    }

    public SaleResponse createSale(
            SaleRequest request) {

        Seller seller =
                getLoggedInSeller();

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
                    "You cannot create a sale for another seller's house"
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

        if (saleRepository
                .existsByHouseHouseIdAndStatus(
                        house.getHouseId(),
                        SOLD_STATUS
                )) {

            throw new IllegalArgumentException(
                    "House has already been sold"
            );
        }

        Booking paidConfirmedBooking =
                findPaidConfirmedBooking(
                        customer.getCustomerId(),
                        house.getHouseId()
                );

        Sale sale =
                new Sale();

        sale.setSalePrice(
                request.getSalePrice()
        );

        sale.setSaleDate(
                request.getSaleDate()
        );

        sale.setStatus(
                SOLD_STATUS
        );

        sale.setHouse(
                house
        );

        sale.setCustomer(
                customer
        );

        Sale savedSale =
                saleRepository.save(
                        sale
                );

        cancelOtherBookings(
                house.getHouseId(),
                paidConfirmedBooking.getBookingId()
        );

        return convertToResponse(
                savedSale
        );
    }

    public List<SaleResponse>
    getAllSales() {

        Seller seller =
                getLoggedInSeller();

        return saleRepository
                .findByHouseSellerSellerId(
                        seller.getSellerId()
                )
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public SaleResponse getSaleById(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Sale sale =
                saleRepository
                        .findBySaleIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (sale == null) {
            return null;
        }

        return convertToResponse(
                sale
        );
    }

    public SaleResponse updateSale(
            Long id,
            SaleRequest request) {

        Seller seller =
                getLoggedInSeller();

        Sale existingSale =
                saleRepository
                        .findBySaleIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (existingSale == null) {
            return null;
        }

        if (SOLD_STATUS.equalsIgnoreCase(
                existingSale.getStatus())) {

            throw new IllegalArgumentException(
                    "Completed SOLD sale cannot be modified"
            );
        }

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
                    "You cannot use another seller's house"
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

        findPaidConfirmedBooking(
                customer.getCustomerId(),
                house.getHouseId()
        );

        existingSale.setSalePrice(
                request.getSalePrice()
        );

        existingSale.setSaleDate(
                request.getSaleDate()
        );

        existingSale.setStatus(
                SOLD_STATUS
        );

        existingSale.setHouse(
                house
        );

        existingSale.setCustomer(
                customer
        );

        Sale updatedSale =
                saleRepository.save(
                        existingSale
                );

        return convertToResponse(
                updatedSale
        );
    }

    public boolean deleteSale(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        Sale sale =
                saleRepository
                        .findBySaleIdAndHouseSellerSellerId(
                                id,
                                seller.getSellerId()
                        )
                        .orElse(null);

        if (sale == null) {
            return false;
        }

        if (SOLD_STATUS.equalsIgnoreCase(
                sale.getStatus())) {

            throw new IllegalArgumentException(
                    "Completed SOLD sale cannot be deleted"
            );
        }

        saleRepository.delete(
                sale
        );

        return true;
    }

    // =========================
    // FIND CONFIRMED + PAID BOOKING
    // =========================

    private Booking findPaidConfirmedBooking(
            Long customerId,
            Long houseId) {

        List<Booking> confirmedBookings =
                bookingRepository
                        .findByCustomerCustomerIdAndHouseHouseIdAndStatus(
                                customerId,
                                houseId,
                                BookingStatus.CONFIRMED
                        );

        for (Booking booking :
                confirmedBookings) {

            boolean paid =
                    paymentRepository
                            .existsByBookingBookingIdAndStatus(
                                    booking.getBookingId(),
                                    PaymentStatus.PAID
                            );

            if (paid) {
                return booking;
            }
        }

        throw new IllegalArgumentException(
                "Sale cannot be completed. "
                        + "Customer must have a CONFIRMED booking "
                        + "with a PAID payment for this house"
        );
    }

    // =========================
    // CANCEL OTHER BOOKINGS
    // AFTER HOUSE IS SOLD
    // =========================

    private void cancelOtherBookings(
            Long houseId,
            Long successfulBookingId) {

        List<BookingStatus> activeStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        List<Booking> bookings =
                bookingRepository
                        .findByHouseHouseIdAndStatusIn(
                                houseId,
                                activeStatuses
                        );

        for (Booking booking :
                bookings) {

            if (!booking.getBookingId()
                    .equals(
                            successfulBookingId
                    )) {

                booking.setStatus(
                        BookingStatus.CANCELLED
                );
            }
        }

        bookingRepository.saveAll(
                bookings
        );
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

    private SaleResponse convertToResponse(
            Sale sale) {

        SaleResponse response =
                new SaleResponse();

        response.setSaleId(
                sale.getSaleId()
        );

        response.setSalePrice(
                sale.getSalePrice()
        );

        response.setSaleDate(
                sale.getSaleDate()
        );

        response.setStatus(
                sale.getStatus()
        );

        if (sale.getHouse() != null) {

            response.setHouseId(
                    sale.getHouse()
                            .getHouseId()
            );

            response.setHouseTitle(
                    sale.getHouse()
                            .getTitle()
            );
        }

        if (sale.getCustomer() != null) {

            response.setCustomerId(
                    sale.getCustomer()
                            .getCustomerId()
            );

            response.setCustomerName(
                    sale.getCustomer()
                            .getName()
            );
        }

        return response;
    }
}