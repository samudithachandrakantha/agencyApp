package com.hfad.agencyapp.ui.models;

/**
 * Data model for customer information used across the app.
 * Fields include required details (id, business/shop name, contact person and address)
 * and optional fields (phone, business registration number, id number, allowed payment methods).
 */
@SuppressWarnings("unused")
public class Customer {
    private String id; // unique id (can be UUID or provider id)
    private String businessName;
    private String contactPerson;
    private String address;

    // Optional fields
    private String phone;
    private String brNumber; // business registration number
    private String idNumber; // national ID / tax id etc.
    private String paymentMethods; // CSV of allowed methods e.g. CASH,CHEQUE

    public Customer() {
    }

    /**
     * Backwards-compatible constructor used across the app.
     */
    public Customer(String id, String businessName, String contactPerson, String address) {
        this.id = id;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.address = address;
    }

    /**
     * Extended constructor including optional fields. Any of the optional values may be null.
     */
    public Customer(String id, String businessName, String contactPerson, String address,
                    String phone, String brNumber, String idNumber) {
        this.id = id;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.address = address;
        this.phone = phone;
        this.brNumber = brNumber;
        this.idNumber = idNumber;
    }

    /**
     * Extended constructor including optional payment methods.
     */
    public Customer(String id, String businessName, String contactPerson, String address,
                    String phone, String brNumber, String idNumber, String paymentMethods) {
        this(id, businessName, contactPerson, address, phone, brNumber, idNumber);
        this.paymentMethods = paymentMethods;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    /**
     * Backwards-compatible alias for older code paths.
     */
    @Deprecated
    public String getCity() { return address; }

    /**
     * Backwards-compatible alias for older code paths.
     */
    @Deprecated
    public void setCity(String city) { this.address = city; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBrNumber() { return brNumber; }
    public void setBrNumber(String brNumber) { this.brNumber = brNumber; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(String paymentMethods) { this.paymentMethods = paymentMethods; }
}

