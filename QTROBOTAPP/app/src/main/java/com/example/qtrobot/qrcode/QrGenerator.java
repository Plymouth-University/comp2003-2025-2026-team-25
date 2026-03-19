package com.example.qtrobot.qrcode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QrGenerator {
    public Bitmap generateQRCodeImage(String dataFromApi) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(dataFromApi, BarcodeFormat.QR_CODE, 400, 400);


        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
        for (int x = 0; x < 400; x++) {
            for (int y = 0; y < 400; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

//        try (FileOutputStream out = new FileOutputStream(filePath)) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//        }
        return bitmap;
    }
}
