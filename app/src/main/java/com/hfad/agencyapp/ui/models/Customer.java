package com.hfad.agencyapp.ui.models;

/**
 * Data model for customer information used across the app.
 * Fields include required details (id, business/shop name, contact person and city)
 * and optional fields (phone, business registration number, id number).
 */
public class Customer {
    private String id; // unique id (can be UUID or provider id)
    private String businessName;
    private String contactPerson;
    private String city;

    // Optional fields
    private String phone;
    private String brNumber; // business registration number
    private String idNumber; // national ID / tax id etc.

    public Customer() {
    }

    /**
     * Backwards-compatible constructor used across the app.
     */
    public Customer(String id, String businessName, String contactPerson, String city) {
        this.id = id;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.city = city;
    }

    /**
     * Extended constructor including optional fields. Any of the optional values may be null.
     */
    public Customer(String id, String businessName, String contactPerson, String city,
                    String phone, String brNumber, String idNumber) {
        this.id = id;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.city = city;
        this.phone = phone;
        this.brNumber = brNumber;
        this.idNumber = idNumber;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBrNumber() { return brNumber; }
    public void setBrNumber(String brNumber) { this.brNumber = brNumber; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
}

