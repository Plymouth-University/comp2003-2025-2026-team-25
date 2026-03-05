import cv2
import hmac
import hashlib
import os
from dotenv import load_dotenv

# Load the secret key from the .env file on the robot
load_dotenv()
SYSTEM_SECRET = os.getenv("QR_SECRET_KEY")

def verify_qr_data(scanned_text):
    """Checks if the scanned string is a valid, signed ID."""
    try:
        # 1. Split the string 'user_id:hash'
        user_id, received_hash = scanned_text.split(":")
        
        #calculate what the hash should be using ssecret
        expected_hash = hmac.new(
            SYSTEM_SECRET.encode('utf-8'),
            user_id.encode('utf-8'),
            hashlib.sha256
        ).hexdigest()
        
        #compare the two
        if hmac.compare_digest(received_hash, expected_hash):
            return True, user_id
        return False, None
    except Exception:
        return False, None

def run_robot_scanner(camera_index=0):
    cap = cv2.VideoCapture(camera_index)
    detector = cv2.QRCodeDetector()

    print("QT Robot: Standing by for QR scan...")

    while True:
        ret, frame = cap.read()
        if not ret: break

        #scan to find QR pixels
        value, points, _ = detector.detectAndDecode(frame)

        if value:
            #VERIFICATION
            is_valid, user_id = verify_qr_data(value)
            
            if is_valid:
                print(f"ACCESS GRANTED: Welcome User {user_id}")
                # Place robot movement or speech commands here!
                break 
            else:
                print("ACCESS DENIED: Invalid QR Code.")

        cv2.imshow("QT Robot QR Scanner", frame)
        if cv2.waitKey(1) & 0xFF == 27: break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    run_robot_scanner()