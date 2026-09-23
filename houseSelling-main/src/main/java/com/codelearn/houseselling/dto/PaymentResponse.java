package com.codelearn.houseselling.dto;

import com.codelearn.houseselling.entity.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class PaymentResponse {

    private Long paymentId;
    private Double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private PaymentStatus status;

    private Long bookingId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String bookingStatus;

    private Long houseId;
    private String houseTitle;
    private Double housePrice;
    private String houseStatus;
    private Double totalPaid;
    private Double remainingAmount;
    private Long customerId;
    private String customerName;
    private String sellerName;
    private String sellerImage;
    private Long sellerId;
    private String sellerEmail;
    private String sellerPhone;
    private String sellerAddress;
    private String sellerNida;

    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerNida;
    private String customerImage;

    private String houseLocation;
    private String houseDescription;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer halls;
    private Integer kitchens;
    private String houseImage;


    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalTime bookingTime) { this.bookingTime = bookingTime; }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Long getHouseId() { return houseId; }
    public void setHouseId(Long houseId) { this.houseId = houseId; }
    public String getHouseTitle() { return houseTitle; }
    public void setHouseTitle(String houseTitle) { this.houseTitle = houseTitle; }
    public Double getHousePrice() { return housePrice; }
    public void setHousePrice(Double housePrice) { this.housePrice = housePrice; }
    public String getHouseStatus() { return houseStatus; }
    public void setHouseStatus(String houseStatus) { this.houseStatus = houseStatus; }
    public Double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }
    public Double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(Double remainingAmount) { this.remainingAmount = remainingAmount; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerImage() { return sellerImage; }
    public void setSellerImage(String sellerImage) { this.sellerImage = sellerImage; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }
    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
    public String getSellerAddress() { return sellerAddress; }
    public void setSellerAddress(String sellerAddress) { this.sellerAddress = sellerAddress; }
    public String getSellerNida() { return sellerNida; }
    public void setSellerNida(String sellerNida) { this.sellerNida = sellerNida; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getCustomerNida() { return customerNida; }
    public void setCustomerNida(String customerNida) { this.customerNida = customerNida; }
    public String getCustomerImage() { return customerImage; }
    public void setCustomerImage(String customerImage) { this.customerImage = customerImage; }
    public String getHouseLocation() { return houseLocation; }
    public void setHouseLocation(String houseLocation) { this.houseLocation = houseLocation; }
    public String getHouseDescription() { return houseDescription; }
    public void setHouseDescription(String houseDescription) { this.houseDescription = houseDescription; }
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    public Integer getHalls() { return halls; }
    public void setHalls(Integer halls) { this.halls = halls; }
    public Integer getKitchens() { return kitchens; }
    public void setKitchens(Integer kitchens) { this.kitchens = kitchens; }

    public String getHouseImage() { return houseImage; }
    public void setHouseImage(String houseImage) { this.houseImage = houseImage; }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}