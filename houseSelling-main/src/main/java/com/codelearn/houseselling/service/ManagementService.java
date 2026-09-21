package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.BookingResponse;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.dto.CustomerUpdateRequest;
import com.codelearn.houseselling.dto.DocumentResponse;
import com.codelearn.houseselling.dto.HouseResponse;
import com.codelearn.houseselling.dto.ManagementRequest;
import com.codelearn.houseselling.dto.ManagementResponse;
import com.codelearn.houseselling.dto.PaymentResponse;
import com.codelearn.houseselling.dto.SaleResponse;
import com.codelearn.houseselling.dto.SellerResponse;
import com.codelearn.houseselling.dto.SellerRequest;
import com.codelearn.houseselling.dto.SellerUpdateRequest;

import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.entity.Document;
import com.codelearn.houseselling.entity.House;
import com.codelearn.houseselling.entity.Management;
import com.codelearn.houseselling.entity.Payment;
import com.codelearn.houseselling.entity.Sale;
import com.codelearn.houseselling.entity.Seller;

import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.CustomerRepository;
import com.codelearn.houseselling.repository.DocumentRepository;
import com.codelearn.houseselling.repository.HouseRepository;
import com.codelearn.houseselling.repository.ManagementRepository;
import com.codelearn.houseselling.repository.PaymentRepository;
import com.codelearn.houseselling.repository.SaleRepository;
import com.codelearn.houseselling.repository.SellerRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagementService {

    private final ManagementRepository managementRepository;
    private final SellerRepository sellerRepository;
    private final HouseRepository houseRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagementService(
            ManagementRepository managementRepository,
            SellerRepository sellerRepository,
            HouseRepository houseRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            SaleRepository saleRepository,
            DocumentRepository documentRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.managementRepository = managementRepository;
        this.sellerRepository = sellerRepository;
        this.houseRepository = houseRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.saleRepository = saleRepository;
        this.documentRepository = documentRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // MANAGEMENT CRUD
    // =========================

    public ManagementResponse createManagement(
            ManagementRequest request) {

        if (managementRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Management email already exists: "
                            + request.getEmail()
            );
        }

        Management management = new Management();

        management.setName(
                request.getName()
        );

        management.setEmail(
                request.getEmail()
        );

        management.setPhone(
                request.getPhone()
        );

        management.setRole(
                request.getRole()
        );

        management.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Management savedManagement =
                managementRepository.save(
                        management
                );

        return convertManagementToResponse(
                savedManagement
        );
    }

    public List<ManagementResponse>
    getAllManagement() {

        return managementRepository
                .findAll()
                .stream()
                .map(this::convertManagementToResponse)
                .toList();
    }

    public ManagementResponse getManagementById(
            Long id) {

        Management management =
                managementRepository
                        .findById(id)
                        .orElse(null);

        if (management == null) {
            return null;
        }

        return convertManagementToResponse(
                management
        );
    }

    public ManagementResponse updateManagement(
            Long id,
            ManagementRequest request) {

        Management existingManagement =
                managementRepository
                        .findById(id)
                        .orElse(null);

        if (existingManagement == null) {
            return null;
        }

        if (managementRepository
                .existsByEmail(request.getEmail())
                && !existingManagement
                .getEmail()
                .equals(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Management email already exists: "
                            + request.getEmail()
            );
        }

        existingManagement.setName(
                request.getName()
        );

        existingManagement.setEmail(
                request.getEmail()
        );

        existingManagement.setPhone(
                request.getPhone()
        );

        existingManagement.setRole(
                request.getRole()
        );

        existingManagement.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Management updatedManagement =
                managementRepository.save(
                        existingManagement
                );

        return convertManagementToResponse(
                updatedManagement
        );
    }

    public void deleteManagement(
            Long id) {

        if (!managementRepository
                .existsById(id)) {

            throw new IllegalArgumentException(
                    "Management not found with id: "
                            + id
            );
        }

        managementRepository.deleteById(id);
    }

    // =========================
    // ADMIN - CREATE SELLER
    // =========================

    public SellerResponse createSeller(SellerRequest request) {

        if (sellerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Seller email already exists: " + request.getEmail()
            );
        }

        Seller seller = new Seller();
        seller.setName(request.getName());
        seller.setEmail(request.getEmail());
        seller.setPhone(request.getPhone());
        seller.setAddress(request.getAddress());
        seller.setNida(request.getNida());
        seller.setImage(request.getImage());
        seller.setPassword(passwordEncoder.encode(request.getPassword()));
        seller.setFailedLoginAttempts(0);
        seller.setAccountLockedUntil(null);

        return convertSellerToResponse(sellerRepository.save(seller));
    }

    // =========================
    // ADMIN - SELLERS
    // =========================

    public List<SellerResponse> getAllSellers() {

        return sellerRepository
                .findAll()
                .stream()
                .map(this::convertSellerToResponse)
                .toList();
    }

    public SellerResponse updateSeller(Long id, SellerUpdateRequest request) {
        Seller seller = sellerRepository.findById(id).orElse(null);
        if (seller == null) return null;
        if (sellerRepository.existsByEmailAndSellerIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Seller email already exists: " + request.getEmail());
        }
        seller.setName(request.getName());
        seller.setEmail(request.getEmail());
        seller.setPhone(request.getPhone());
        seller.setAddress(request.getAddress());
        seller.setNida(request.getNida());
        seller.setImage(request.getImage());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            seller.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return convertSellerToResponse(sellerRepository.save(seller));
    }

    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id).orElse(null);
        if (seller == null) throw new IllegalArgumentException("Seller not found with id: " + id);
        if (houseRepository.existsBySellerSellerId(id)) {
            throw new IllegalArgumentException("Seller cannot be deleted while they still own houses. Delete or transfer the houses first.");
        }
        sellerRepository.delete(seller);
    }

    // =========================
    // ADMIN - HOUSES
    // =========================

    public List<HouseResponse> getAllHouses() {

        return houseRepository
                .findAll()
                .stream()
                .map(this::convertHouseToResponse)
                .toList();
    }

    // =========================
    // ADMIN - BOOKINGS
    // =========================

    public List<BookingResponse> getAllBookings() {

        return bookingRepository
                .findAll()
                .stream()
                .map(this::convertBookingToResponse)
                .toList();
    }

    // =========================
    // ADMIN - PAYMENTS
    // =========================

    public List<PaymentResponse> getAllPayments() {

        return paymentRepository
                .findAll()
                .stream()
                .map(this::convertPaymentToResponse)
                .toList();
    }

    // =========================
    // ADMIN - SALES
    // =========================

    public List<SaleResponse> getAllSales() {

        return saleRepository
                .findAll()
                .stream()
                .map(this::convertSaleToResponse)
                .toList();
    }

    // =========================
    // ADMIN - DOCUMENTS
    // =========================

    public List<DocumentResponse> getAllDocuments() {

        return documentRepository
                .findAll()
                .stream()
                .map(this::convertDocumentToResponse)
                .toList();
    }

    // =========================
    // ADMIN - CUSTOMERS
    // =========================

    public List<CustomerResponse> getAllCustomers() {

        return customerRepository
                .findAll()
                .stream()
                .map(this::convertCustomerToResponse)
                .toList();
    }

    public CustomerResponse updateCustomer(
            Long id, CustomerUpdateRequest request) {

        Customer customer =
                customerRepository.findById(id).orElse(null);

        if (customer == null) {
            return null;
        }

        if (customerRepository.existsByEmailAndCustomerIdNot(
                request.getEmail(), id)) {
            throw new IllegalArgumentException(
                    "Customer email already exists: " + request.getEmail()
            );
        }

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setNida(request.getNida());
        customer.setImage(request.getImage());

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new IllegalArgumentException(
                        "Password must be at least 6 characters"
                );
            }
            customer.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return convertCustomerToResponse(
                customerRepository.save(customer)
        );
    }

    public void deleteCustomer(Long id) {

        Customer customer =
                customerRepository.findById(id).orElse(null);

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer not found with id: " + id
            );
        }

        if (bookingRepository.existsByCustomerCustomerId(id)) {
            throw new IllegalArgumentException(
                    "Customer cannot be deleted because they have bookings. "
                            + "Delete or resolve the bookings first."
            );
        }

        if (saleRepository.existsByCustomerCustomerId(id)) {
            throw new IllegalArgumentException(
                    "Customer cannot be deleted because they have sales. "
                            + "Delete or resolve the sales first."
            );
        }

        customerRepository.delete(customer);
    }

    // =========================
    // MANAGEMENT CONVERTER
    // =========================

    private ManagementResponse convertManagementToResponse(
            Management management) {

        ManagementResponse response =
                new ManagementResponse();

        response.setManagementId(
                management.getManagementId()
        );

        response.setName(
                management.getName()
        );

        response.setEmail(
                management.getEmail()
        );

        response.setPhone(
                management.getPhone()
        );

        response.setRole(
                management.getRole()
        );

        return response;
    }

    // =========================
    // SELLER CONVERTER
    // =========================

    private SellerResponse convertSellerToResponse(
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

    // =========================
    // HOUSE CONVERTER
    // =========================

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

        response.setBedrooms(
                house.getBedrooms()
        );

        response.setBathrooms(
                house.getBathrooms()
        );

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
        }

        return response;
    }

    // =========================
    // BOOKING CONVERTER
    // =========================

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

    // =========================
    // PAYMENT CONVERTER
    // =========================

    private PaymentResponse convertPaymentToResponse(
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

    // =========================
    // SALE CONVERTER
    // =========================

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

    // =========================
    // DOCUMENT CONVERTER
    // =========================

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

    // =========================
    // CUSTOMER CONVERTER
    // =========================

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

        response.setNida(customer.getNida());
        response.setImage(customer.getImage());

        return response;
    }
}