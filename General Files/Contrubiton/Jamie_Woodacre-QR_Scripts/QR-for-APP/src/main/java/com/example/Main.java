package com.example;
    public class Main {
        public static void main(String[] args) {
            AppRequester requester = new AppRequester();
            QRCodeGenerator generator = new QRCodeGenerator();

            try {
                String qrString = requester.getQrDataFromServer("100");
                System.out.println("Received from Server: " + qrString);

                generator.generateQRCodeImage(qrString, "user_100_qr.png");
                
                System.out.println("Success! QR Code is ready for the Robot to scan.");

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }