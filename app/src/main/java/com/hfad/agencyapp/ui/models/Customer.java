package com.hfad.agencyapp.ui.models;

/**
 * Data model for customer information used across the app.
 * Fields include minimal required details: id, business/shop name, contact person and city.
 */
public class Customer {
    private String id; // unique id (can be UUID or provider id)
    private String businessName;
    private String contactPerson;
    private String city;
    private String phoneNumber;
    private String address;

    public Customer() {
    }

    public Customer(String id, String businessName, String contactPerson, String phoneNumber, String address, String city) {
        this.id = id;
        this.businessName = businessName;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
    }

    // Convenience constructor used earlier (legacy places)
    public Customer(String id, String name, String phone, String address) {
        this.id = id;
        this.businessName = name;
        this.contactPerson = "";
        this.phoneNumber = phone;
        this.address = address;
        this.city = "";
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

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

