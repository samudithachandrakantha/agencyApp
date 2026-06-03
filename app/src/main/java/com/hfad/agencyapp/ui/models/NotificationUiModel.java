package com.hfad.agencyapp.ui.models;

public class NotificationUiModel {
    public final long id;
    public final String title;
    public final String customerName;
    public final String invoiceNumber;
    public final String amount;
    public final String chequeDateFormatted;
    public final String clearanceDateFormatted;
    public final String status; // UPCOMING, DUE_TODAY, CLEARED, OVERDUE

    public NotificationUiModel(long id, String title, String customerName, String invoiceNumber, String amount, String chequeDateFormatted, String clearanceDateFormatted, String status) {
        this.id = id;
        this.title = title;
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.chequeDateFormatted = chequeDateFormatted;
        this.clearanceDateFormatted = clearanceDateFormatted;
        this.status = status;
    }
}
