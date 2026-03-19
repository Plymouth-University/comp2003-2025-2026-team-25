package com.example.qtrobot.qrcode;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class QrGeneratorTest extends TestCase {

//
//    @Test
//    public void testGenerateQRCodeImage() throws Exception {
//            String qr_string = "f9f694f2-a217-48ac-b984-f527a7e530f7:5b01bdb74ca1775649572c26668c744224fc94633394c3b39d02edbfd0a35cb6";
//        // Use app's cache directory — writable on device/emulator
//        String path = InstrumentationRegistry.getInstrumentation()
//                .getTargetContext().getCacheDir().getAbsolutePath()
//                + "/qr_code.png";
//
//
//        QrGenerator generator = new QrGenerator();
//            generator.generateQRCodeImage(qr_string, path);
//
//            Log.d("QrTest", "!!! FILE SAVED AT: " + path);
//
//            File file = new File(path);
//            Assert.assertTrue("QR file was not created", file.exists());
//            assertTrue("QR file is empty", file.length() > 0);
//    }
}