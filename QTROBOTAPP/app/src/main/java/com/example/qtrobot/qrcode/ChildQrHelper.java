package com.example.qtrobot.qrcode;

import com.example.qtrobot.data.local.entity.ChildProfile;

import java.util.UUID;

/**
 * Ensures each child has a stable unique token used in QR payloads and robot binding.
 */
public final class ChildQrHelper {

    private ChildQrHelper() {}

    public static String ensureQrToken(ChildProfile child) {
        if (child == null) {
            return "qt:" + UUID.randomUUID().toString().replace("-", "");
        }
        if (child.qr_string != null && !child.qr_string.trim().isEmpty()) {
            return child.qr_string.trim();
        }
        return "qt:" + UUID.randomUUID().toString().replace("-", "");
    }
}
