package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.BookingResponse;
import com.codelearn.houseselling.dto.CustomerBookingRequest;
import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.dto.HouseResponse;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.BookingStatus;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.entity.Document;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.Payment;
import com.codelearn.houseselling.entity.PaymentStatus;
import com.codelearn.houseselling.entity.Sale;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.CustomerRepository;
import com.codelearn.houseselling.repository.DocumentRepository;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.PaymentRepository;
import com.codelearn.houseselling.repository.SaleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerPortalService {

    private static final String SOLD_STATUS =
            "SOLD";

    private final CustomerRepository customerRepository;
    private final HouseRepository houseRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerPortalService(
            CustomerRepository customerRepository,
            HouseRepository houseRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            SaleRepository saleRepository,
            DocumentRepository documentRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository =
                customerRepository;

        this.houseRepository =
                houseRepository;

        this.bookingRepository =
                bookingRepository;

        this.paymentRepository =
                paymentRepository;

        this.saleRepository =
                saleRepository;

        this.documentRepository =
                documentRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    public CustomerResponse getMyProfile() {

        return convertCustomerToResponse(
                getLoggedInCustomer()
        );
    }

    public CustomerResponse updateMyProfile(
            CustomerRequest request) {

        Customer customer =
                getLoggedInCustomer();

        if (customerRepository
                .existsByEmailAndCustomerIdNot(
                        request.getEmail(),
                        customer.getCustomerId()
                )) {

            throw new IllegalArgumentException(
                    "Customer email already exists: "
                            + request.getEmail()
            );
        }

        customer.setName(
                request.getName()
        );

        customer.setEmail(
                request.getEmail()
        );

        customer.setPhone(
                request.getPhone()
        );

        customer.setAddress(
                request.getAddress()
        );

        customer.setImage(request.getImage());

        customer.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Customer updatedCustomer =
                customerRepository.save(
                        customer
                );

        return convertCustomerToResponse(
                updatedCustomer
        );
    }

    public List<HouseResponse>
    getAvailableHouses() {

        return houseRepository
                .findAll()
                .stream()
                .filter(house ->
                        !"SOLD_OUT".equalsIgnoreCase(house.getStatus())
                        && !saleRepository
                                .existsByHouseHouseIdAndStatus(
                                        house.getHouseId(),
                                        SOLD_STATUS
                                )
                )
                .map(this::convertHouseToResponse)
                .toList();
    }

    public HouseResponse getAvailableHouseById(
            Long houseId) {

        House house =
                houseRepository
                        .findById(houseId)
                        .orElse(null);

        if (house == null) {
            return null;
        }

        if ("SOLD_OUT".equalsIgnoreCase(house.getStatus()) || saleRepository
                .existsByHouseHouseIdAndStatus(
                        houseId,
                        SOLD_STATUS
                )) {

            return null;
        }

        return convertHouseToResponse(
                house
        );
    }

    public BookingResponse createMyBooking(
            CustomerBookingRequest request) {

        Customer customer =
                getLoggedInCustomer();

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

        if ("SOLD_OUT".equalsIgnoreCase(house.getStatus()) || saleRepository
                .existsByHouseHouseIdAndStatus(
                        house.getHouseId(),
                        SOLD_STATUS
                )) {

            throw new IllegalArgumentException(
                    "House is no longer available for booking"
            );
        }

        List<BookingStatus> activeStatuses =
                List.of(
                        BookingStatus.PENDING,
                        BookingStatus.CONFIRMED
                );

        if (bookingRepository
                .existsByHouseHouseIdAndBookingDateAndBookingTimeAndStatusIn(
                        house.getHouseId(),
                        request.getBookingDate(),
                        request.getBookingTime(),
                        activeStatuses
                )) {

            throw new IllegalArgumentException(
                    "House already has an active booking at this date and time"
            );
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
                BookingStatus.PENDING
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

        return convertBookingToResponse(
                savedBooking
        );
    }

    public List<BookingResponse>
    getMyBookings() {

        Customer customer =
                getLoggedInCustomer();

        return bookingRepository
                .findByCustomerCustomerId(
                        customer.getCustomerId()
                )
                .stream()
                .map(this::convertBookingToResponse)
                .toList();
    }

    public BookingResponse getMyBookingById(
            Long bookingId) {

        Customer customer =
                getLoggedInCustomer();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndCustomerCustomerId(
                                bookingId,
                                customer.getCustomerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return null;
        }

        return convertBookingToResponse(
                booking
        );
    }

    public BookingResponse cancelMyBooking(
            Long bookingId) {

        Customer customer =
                getLoggedInCustomer();

        Booking booking =
                bookingRepository
                        .findByBookingIdAndCustomerCustomerId(
                                bookingId,
                                customer.getCustomerId()
                        )
                        .orElse(null);

        if (booking == null) {
            return null;
        }

        if (paymentRepository
                .existsByBookingBookingIdAndStatus(
                        bookingId,
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

        return convertBookingToResponse(
                updatedBooking
        );
    }

    @Transactional
    public PaymentResponse createMyPayment(
            com.codelearn.houseselling.dto.PaymentRequest request) {

        Customer customer = getLoggedInCustomer();

        Booking booking = bookingRepository
                .findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found with id: " + request.getBookingId()));

        if (booking.getCustomer() == null
                || !booking.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new AccessDeniedException("You can only pay for your own booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Payment can only be made for a CONFIRMED booking");
        }

        if (paymentRepository.existsByBookingBookingIdAndStatus(
                booking.getBookingId(), PaymentStatus.PENDING)) {
            throw new IllegalArgumentException("A payment is already awaiting seller receipt confirmation for this booking");
        }

        double paid = paymentRepository.findByBookingBookingIdAndStatus(
                        booking.getBookingId(), PaymentStatus.PAID)
                .stream()
                .mapToDouble(p -> p.getAmount() == null ? 0d : p.getAmount())
                .sum();
        double price = booking.getHouse().getPrice();
        double remaining = Math.max(0d, price - paid);

        if (remaining <= 0.0) {
            throw new IllegalArgumentException("This house has already been fully paid");
        }

        if (request.getAmount() > remaining) {
            throw new IllegalArgumentException(
                    "Payment cannot exceed the remaining house balance of " + remaining);
        }

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setBooking(booking);

        return convertPaymentToResponse(paymentRepository.save(payment));
    }

    public List<PaymentResponse>
    getMyPayments() {

        Customer customer =
                getLoggedInCustomer();

        return paymentRepository
                .findByBookingCustomerCustomerId(
                        customer.getCustomerId()
                )
                .stream()
                .map(this::convertPaymentToResponse)
                .toList();
    }

    public List<SaleResponse>
    getMySales() {

        Customer customer =
                getLoggedInCustomer();

        return saleRepository
                .findByCustomerCustomerId(
                        customer.getCustomerId()
                )
                .stream()
                .map(this::convertSaleToResponse)
                .toList();
    }

    public List<DocumentResponse>
    getMyDocuments() {

        Customer customer =
                getLoggedInCustomer();

        List<Sale> soldSales =
                saleRepository
                        .findByCustomerCustomerIdAndStatus(
                                customer.getCustomerId(),
                                SOLD_STATUS
                        );

        List<Long> houseIds =
                soldSales
                        .stream()
                        .filter(sale ->
                                sale.getHouse() != null
                        )
                        .map(sale ->
                                sale.getHouse()
                                        .getHouseId()
                        )
                        .distinct()
                        .toList();

        if (houseIds.isEmpty()) {
            return List.of();
        }

        return documentRepository
                .findByHouseHouseIdIn(
                        houseIds
                )
                .stream()
                .map(this::convertDocumentToResponse)
                .toList();
    }

    private Customer getLoggedInCustomer() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Customer is not authenticated"
            );
        }

        String email =
                authentication.getName();

        return customerRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Logged-in customer not found"
                        )
                );
    }

    private CustomerResponse convertCustomerToResponse(
            Customer customer) {

        CustomerResponse response =
                new CustomerResponse();

        response.setCustomerId(
                customer.getCustomerId()
        );

        response.setName(
                customer.getName()
        );

        response.setEmail(
                customer.getEmail()
        );

        response.setPhone(
                customer.getPhone()
        );

        response.setAddress(
                customer.getAddress()
        );

        response.setImage(customer.getImage());

        return response;
    }

    private HouseResponse convertHouseToResponse(
            House house) {

        HouseResponse response =
                new HouseResponse();

        response.setHouseId(
                house.getHouseId()
        );

        response.setTitle(
                house.getTitle()
        );

        response.setLocation(
                house.getLocation()
        );

        response.setDescription(
                house.getDescription()
        );

        response.setPrice(
                house.getPrice()
        );
        response.setStatus(house.getStatus());

        response.setBedrooms(
                house.getBedrooms()
        );

        response.setBathrooms(
                house.getBathrooms()
        );
        response.setHalls(house.getHalls());
        response.setKitchens(house.getKitchens());

        response.setImage(house.getImage());

        if (house.getSeller() != null) {

            response.setSellerId(
                    house.getSeller()
                            .getSellerId()
            );

            response.setSellerName(
                    house.getSeller()
                            .getName()
            );
            response.setSellerEmail(
                    house.getSeller().getEmail()
            );
            response.setSellerPhone(
                    house.getSeller().getPhone()
            );
            response.setSellerImage(
                    house.getSeller().getImage()
            );
        }

        return response;
    }

    private BookingResponse convertBookingToResponse(
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

    private PaymentResponse convertPaymentToResponse(
            Payment payment) {

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());

        if (payment.getBooking() != null) {
            Booking booking = payment.getBooking();
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
                    Seller seller = house.getSeller();
                    response.setSellerId(seller.getSellerId());
                    response.setSellerName(seller.getName());
                    response.setSellerImage(seller.getImage());
                    response.setSellerEmail(seller.getEmail());
                    response.setSellerPhone(seller.getPhone());
                    response.setSellerAddress(seller.getAddress());
                    response.setSellerNida(seller.getNida());
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

    private SaleResponse convertSaleToResponse(
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

    private DocumentResponse convertDocumentToResponse(
            Document document) {

        DocumentResponse response =
                new DocumentResponse();

        response.setDocumentId(
                document.getDocumentId()
        );

        response.setDocumentName(
                document.getDocumentName()
        );

        response.setDocumentType(
                document.getDocumentType()
        );

        response.setDocumentNumber(
                document.getDocumentNumber()
        );

        response.setIssueDate(
                document.getIssueDate()
        );

        response.setStatus(
                document.getStatus()
        );

        if (document.getHouse() != null) {

            response.setHouseId(
                    document.getHouse()
                            .getHouseId()
            );

            response.setHouseTitle(
                    document.getHouse()
                            .getTitle()
            );
        }

        return response;
    }
}