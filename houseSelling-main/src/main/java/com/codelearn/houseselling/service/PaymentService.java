package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.PaymentRequest;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import com.codelearn.houseselling.entity.Payment;
import com.codelearn.houseselling.entity.PaymentStatus;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.Sale;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.repository.SaleRepository;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.PaymentRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SellerRepository sellerRepository;
    private final SaleRepository saleRepository;
    private final HouseRepository houseRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            SellerRepository sellerRepository,
            SaleRepository saleRepository,
            HouseRepository houseRepository) {

        this.paymentRepository =
                paymentRepository;

        this.bookingRepository =
                bookingRepository;

        this.sellerRepository =
                sellerRepository;

        this.saleRepository =
                saleRepository;

        this.houseRepository =
                houseRepository;
    }

    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request) {

        Seller seller = getLoggedInSeller();
        Booking booking = getSellerBooking(request.getBookingId(), seller);
        validateConfirmed(booking);

        double remaining = getRemainingAmount(booking);
        validateAmount(request.getAmount(), remaining);

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PAID);
        payment.setBooking(booking);

        Payment saved = paymentRepository.save(payment);
        markHouseSoldOutAndMaybeCompleteSale(booking);
        return convertToResponse(saved);
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

    @Transactional
    public PaymentResponse updatePayment(
            Long id,
            PaymentRequest request) {

        Seller seller = getLoggedInSeller();
        Payment existingPayment = paymentRepository
                .findByPaymentIdAndBookingHouseSellerSellerId(id, seller.getSellerId())
                .orElse(null);

        if (existingPayment == null) return null;
        if (existingPayment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("PAID payment cannot be modified");
        }

        Booking booking = getSellerBooking(request.getBookingId(), seller);
        validateConfirmed(booking);

        double paidExcludingThis = paymentRepository
                .findByBookingBookingIdAndStatus(booking.getBookingId(), PaymentStatus.PAID)
                .stream()
                .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                .sum();
        double remaining = Math.max(0d, booking.getHouse().getPrice() - paidExcludingThis);
        validateAmount(request.getAmount(), remaining);

        existingPayment.setAmount(request.getAmount());
        existingPayment.setPaymentDate(request.getPaymentDate());
        existingPayment.setPaymentMethod(request.getPaymentMethod());
        existingPayment.setStatus(PaymentStatus.PAID);
        existingPayment.setBooking(booking);

        Payment updated = paymentRepository.save(existingPayment);
        markHouseSoldOutAndMaybeCompleteSale(booking);
        return convertToResponse(updated);
    }

    /**
     * Seller-only confirmation of a payment submitted by a customer.
     * The seller confirms the existing amount; the client cannot change it.
     */
    @Transactional
    public PaymentResponse receivePayment(Long id) {
        Seller seller = getLoggedInSeller();
        Payment payment = paymentRepository
                .findByPaymentIdAndBookingHouseSellerSellerId(id, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for this seller"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return convertToResponse(payment);
        }

        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new IllegalArgumentException("Payment is not linked to a booking");
        }
        validateConfirmed(booking);

        double paidBefore = paymentRepository
                .findByBookingBookingIdAndStatus(booking.getBookingId(), PaymentStatus.PAID)
                .stream()
                .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                .sum();
        validateAmount(payment.getAmount(), Math.max(0d, booking.getHouse().getPrice() - paidBefore));

        payment.setStatus(PaymentStatus.PAID);
        Payment saved = paymentRepository.save(payment);

        // A received payment reserves the house and explicitly keeps the booking approved.
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        markHouseSoldOutAndMaybeCompleteSale(booking);
        return convertToResponse(saved);
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

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());

        Booking booking = payment.getBooking();
        if (booking != null) {
            response.setBookingId(booking.getBookingId());
            response.setBookingDate(booking.getBookingDate());
            response.setBookingTime(booking.getBookingTime());
            if (booking.getStatus() != null) response.setBookingStatus(booking.getStatus().name());
            if (booking.getCustomer() != null) {
                Customer customer = booking.getCustomer();
                response.setCustomerId(customer.getCustomerId());
                response.setCustomerName(customer.getName());
                response.setCustomerEmail(customer.getEmail());
                response.setCustomerPhone(customer.getPhone());
                response.setCustomerAddress(customer.getAddress());
                response.setCustomerNida(customer.getNida());
                response.setCustomerImage(customer.getImage());
            }
            House house = booking.getHouse();
            if (house != null) {
                response.setHouseId(house.getHouseId());
                response.setHouseTitle(house.getTitle());
                response.setHousePrice(house.getPrice());
                response.setHouseStatus(house.getStatus());
                response.setHouseLocation(house.getLocation());
                response.setHouseDescription(house.getDescription());
                response.setBedrooms(house.getBedrooms());
                response.setBathrooms(house.getBathrooms());
                response.setHalls(house.getHalls());
                response.setKitchens(house.getKitchens());
                response.setHouseImage(house.getImage());
                if (house.getSeller() != null) {
                    Seller houseSeller = house.getSeller();
                    response.setSellerId(houseSeller.getSellerId());
                    response.setSellerName(houseSeller.getName());
                    response.setSellerImage(houseSeller.getImage());
                    response.setSellerEmail(houseSeller.getEmail());
                    response.setSellerPhone(houseSeller.getPhone());
                    response.setSellerAddress(houseSeller.getAddress());
                    response.setSellerNida(houseSeller.getNida());
                }
                double totalPaid = paymentRepository
                        .findByBookingBookingIdAndStatus(booking.getBookingId(), PaymentStatus.PAID)
                        .stream()
                        .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                        .sum();
                response.setTotalPaid(totalPaid);
                response.setRemainingAmount(Math.max(0d, house.getPrice() - totalPaid));
            }
        }
        return response;
    }

    private Booking getSellerBooking(Long bookingId, Seller seller) {
        return bookingRepository.findByBookingIdAndHouseSellerSellerId(bookingId, seller.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for this seller"));
    }

    private void validateConfirmed(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Payment can only be recorded for a CONFIRMED booking");
        }
    }

    private void validateAmount(Double amount, double remaining) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (amount > remaining + 0.000001d) {
            throw new IllegalArgumentException("Payment cannot exceed the remaining balance of " + remaining);
        }
    }

    private double getRemainingAmount(Booking booking) {
        double paid = paymentRepository
                .findByBookingBookingIdAndStatus(booking.getBookingId(), PaymentStatus.PAID)
                .stream()
                .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                .sum();
        return Math.max(0d, booking.getHouse().getPrice() - paid);
    }

    private void markHouseSoldOutAndMaybeCompleteSale(Booking booking) {
        House house = booking.getHouse();
        house.setStatus("SOLD_OUT");
        houseRepository.save(house);

        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        List<Booking> otherBookings = bookingRepository
                .findByHouseHouseIdAndStatusIn(house.getHouseId(), activeStatuses);
        for (Booking other : otherBookings) {
            if (!other.getBookingId().equals(booking.getBookingId())) {
                other.setStatus(BookingStatus.CANCELLED);
            }
        }
        bookingRepository.saveAll(otherBookings);

        double totalPaid = paymentRepository
                .findByBookingBookingIdAndStatus(booking.getBookingId(), PaymentStatus.PAID)
                .stream()
                .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                .sum();

        if (totalPaid + 0.000001d >= house.getPrice()
                && !saleRepository.existsByHouseHouseIdAndStatus(house.getHouseId(), "SOLD")) {
            Sale sale = new Sale();
            sale.setSalePrice(house.getPrice());
            sale.setSaleDate(java.time.LocalDate.now());
            sale.setStatus("SOLD");
            sale.setHouse(house);
            sale.setCustomer(booking.getCustomer());
            saleRepository.save(sale);
        }
    }

}