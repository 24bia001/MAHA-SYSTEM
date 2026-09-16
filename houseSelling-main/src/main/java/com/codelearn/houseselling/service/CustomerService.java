package com.codelearn.houseselling.service;

import com.codelearn.houseselling.dto.CustomerRequest;
import com.codelearn.houseselling.dto.CustomerResponse;
import com.codelearn.houseselling.entity.Booking;
import com.codelearn.houseselling.entity.Customer;
import com.codelearn.houseselling.entity.Sale;
import com.codelearn.houseselling.entity.Seller;
import com.codelearn.houseselling.repository.BookingRepository;
import com.codelearn.houseselling.repository.CustomerRepository;
import com.codelearn.houseselling.repository.SaleRepository;
import com.codelearn.houseselling.repository.SellerRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final SaleRepository saleRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            BookingRepository bookingRepository,
            SaleRepository saleRepository,
            SellerRepository sellerRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
        this.saleRepository = saleRepository;
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // SELLER CREATES CUSTOMER
    public CustomerResponse createCustomer(
            CustomerRequest request) {

        getLoggedInSeller();

        if (customerRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Customer email already exists: "
                            + request.getEmail()
            );
        }

        Customer customer = new Customer();

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

        customer.setNida(request.getNida());
        customer.setImage(request.getImage());

        customer.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Customer savedCustomer =
                customerRepository.save(
                        customer
                );

        return convertToResponse(
                savedCustomer
        );
    }

    // SELLER GETS CUSTOMERS RELATED TO HIS/HER HOUSES
    public List<CustomerResponse> getAllCustomers() {

        Seller seller =
                getLoggedInSeller();

        Map<Long, Customer> customers =
                new LinkedHashMap<>();

        List<Booking> bookings =
                bookingRepository
                        .findByHouseSellerSellerId(
                                seller.getSellerId()
                        );

        for (Booking booking : bookings) {

            Customer customer =
                    booking.getCustomer();

            if (customer != null) {

                customers.put(
                        customer.getCustomerId(),
                        customer
                );
            }
        }

        List<Sale> sales =
                saleRepository
                        .findByHouseSellerSellerId(
                                seller.getSellerId()
                        );

        for (Sale sale : sales) {

            Customer customer =
                    sale.getCustomer();

            if (customer != null) {

                customers.put(
                        customer.getCustomerId(),
                        customer
                );
            }
        }

        return customers.values()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        if (!canAccessCustomer(
                id,
                seller.getSellerId())) {

            return null;
        }

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        if (customer == null) {
            return null;
        }

        return convertToResponse(customer);
    }

    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request) {

        Seller seller =
                getLoggedInSeller();

        if (!canAccessCustomer(
                id,
                seller.getSellerId())) {

            return null;
        }

        Customer existingCustomer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        if (existingCustomer == null) {
            return null;
        }

        if (isCustomerSharedWithAnotherSeller(
                id,
                seller.getSellerId())) {

            throw new AccessDeniedException(
                    "Customer is also associated with another seller "
                            + "and cannot be updated directly"
            );
        }

        if (customerRepository
                .existsByEmail(request.getEmail())
                && !existingCustomer
                .getEmail()
                .equals(request.getEmail())) {

            throw new IllegalArgumentException(
                    "Customer email already exists: "
                            + request.getEmail()
            );
        }

        existingCustomer.setName(
                request.getName()
        );

        existingCustomer.setEmail(
                request.getEmail()
        );

        existingCustomer.setPhone(
                request.getPhone()
        );

        existingCustomer.setAddress(
                request.getAddress()
        );

        existingCustomer.setNida(request.getNida());
        existingCustomer.setImage(request.getImage());

        existingCustomer.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        Customer updatedCustomer =
                customerRepository.save(
                        existingCustomer
                );

        return convertToResponse(
                updatedCustomer
        );
    }

    public boolean deleteCustomer(
            Long id) {

        Seller seller =
                getLoggedInSeller();

        if (!canAccessCustomer(
                id,
                seller.getSellerId())) {

            return false;
        }

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        if (customer == null) {
            return false;
        }

        if (bookingRepository
                .existsByCustomerCustomerId(id)) {

            throw new IllegalArgumentException(
                    "Customer cannot be deleted because they have bookings: "
                            + id
            );
        }

        if (saleRepository
                .existsByCustomerCustomerId(id)) {

            throw new IllegalArgumentException(
                    "Customer cannot be deleted because they have sales: "
                            + id
            );
        }

        customerRepository.delete(customer);

        return true;
    }

    private boolean canAccessCustomer(
            Long customerId,
            Long sellerId) {

        boolean hasBooking =
                bookingRepository
                        .existsByCustomerCustomerIdAndHouseSellerSellerId(
                                customerId,
                                sellerId
                        );

        boolean hasSale =
                saleRepository
                        .existsByCustomerCustomerIdAndHouseSellerSellerId(
                                customerId,
                                sellerId
                        );

        return hasBooking || hasSale;
    }

    private boolean isCustomerSharedWithAnotherSeller(
            Long customerId,
            Long sellerId) {

        boolean otherBooking =
                bookingRepository
                        .existsByCustomerCustomerIdAndHouseSellerSellerIdNot(
                                customerId,
                                sellerId
                        );

        boolean otherSale =
                saleRepository
                        .existsByCustomerCustomerIdAndHouseSellerSellerIdNot(
                                customerId,
                                sellerId
                        );

        return otherBooking || otherSale;
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

    private CustomerResponse convertToResponse(
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