package com.hfad.agencyapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static byte[] compressJpeg(byte[] inputBytes, int maxWidth, int quality) {
        if (inputBytes == null || inputBytes.length == 0) return inputBytes;

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bmp = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.length, options);
        if (bmp == null) return inputBytes;

        float scale = Math.min(1f, (float) maxWidth / bmp.getWidth());
        int w = Math.max(1, (int) (bmp.getWidth() * scale));
        int h = Math.max(1, (int) (bmp.getHeight() * scale));

        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, os);

        bmp.recycle();
        scaled.recycle();

        return os.toByteArray();
    }

    public static byte[] compressJpeg(Bitmap bitmap, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
        return os.toByteArray();
    }
}

