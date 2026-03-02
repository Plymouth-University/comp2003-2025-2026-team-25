
import cv2

def read_qr_from_qt_camera(camera_index=0):
    cap = cv2.VideoCapture(camera_index)
    detector = cv2.QRCodeDetector()

    if not cap.isOpened():
        print("Error: Could not open camera.")
        return None

    print("Scanning for QR code...")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Failed to grab frame.")
            break

        value, points, _ = detector.detectAndDecode(frame)

        if value:
            print("QR Code detected:", value)
            cap.release()
            cv2.destroyAllWindows()
            return value

        cv2.imshow("QT Robot QR Scanner", frame)

        # Press ESC to cancel
        if cv2.waitKey(1) & 0xFF == 27:
            break

    cap.release()
    cv2.destroyAllWindows()
    return None

