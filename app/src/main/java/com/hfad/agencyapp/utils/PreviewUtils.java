package com.hfad.agencyapp.utils;

import android.content.Context;
import android.content.Intent;

import com.hfad.agencyapp.ui.invoice.InvoicePreviewActivity;

public class PreviewUtils {

    public static void showInvoicePreview(Context ctx, long invoiceId) {
        Intent intent = new Intent(ctx, InvoicePreviewActivity.class);
        intent.putExtra(InvoicePreviewActivity.EXTRA_INVOICE_ID, invoiceId);
        if (!(ctx instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
    }
}
