#!/usr/bin/env python3

import rospy
from sensor_msgs.msg import Image
from std_msgs.msg import String
from cv_bridge import CvBridge
import cv2
from pyzbar import pyzbar
import hmac
import hashlib
import time
import numpy as np
import os
from dotenv import load_dotenv

# ==========================
# Load secret key from .env
# ==========================
load_dotenv()
SYSTEM_SECRET = os.getenv("QR_SECRET_KEY")
if not SYSTEM_SECRET:
    raise ValueError("QR_SECRET_KEY not found in .env file!")

# ==========================
# ROS Bridge
# ==========================
bridge = CvBridge()
speechSay_pub = None

# ==========================
# Frame / flash tracking
# ==========================
frame_count = 0
last_scan_time = 0
FLASH_DURATION = 0.3
flash_end_time = 0
flash_color = None
flash_rects = []

# ==========================
# QR Verification
# ==========================
def verify_qr_data(scanned_text):
    try:
        # Expected format: user_id:hash
        parts = scanned_text.rsplit(":", 1)
        if len(parts) != 2:
            rospy.logwarn("Invalid QR format (expected user_id:hash)")
            return False, None

        user_id, received_hash = parts
        expected_hash = hmac.new(
            SYSTEM_SECRET.encode('utf-8'),
            user_id.encode('utf-8'),
            hashlib.sha256
        ).hexdigest()

        if hmac.compare_digest(received_hash, expected_hash):
            return True, user_id
        return False, None
    except Exception as e:
        rospy.logerr(f"QR verification error: {e}")
        return False, None

# ==========================
# Image callback
# ==========================
def image_callback(msg):
    global frame_count, last_scan_time, flash_end_time, flash_color, flash_rects, speechSay_pub

    frame_count += 1
    try:
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        decoded_objects = pyzbar.decode(gray)

        current_time = time.time()
        qr_count = len(decoded_objects)

        for obj in decoded_objects:
            qr_text = obj.data.decode('utf-8')
            is_valid, user_id = verify_qr_data(qr_text)
            color = (0, 255, 0) if is_valid else (0, 0, 255)

            # Draw bounding box
            pts = [(p.x, p.y) for p in obj.polygon] if obj.polygon else []
            rect_coords = pts if pts else None
            if rect_coords:
                for i in range(len(rect_coords)):
                    cv2.line(frame, rect_coords[i], rect_coords[(i + 1) % len(rect_coords)], color, 3)

            # Draw label
            x, y, w, h = obj.rect
            label = f"VALID: {user_id}" if is_valid else "INVALID QR"
            cv2.putText(frame, label, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2)

            # Process only every 3 seconds
            if current_time - last_scan_time > 3:
                last_scan_time = current_time
                if is_valid:
                    rospy.loginfo(f"User {user_id} authenticated")
                    if speechSay_pub:
                        speechSay_pub.publish(f"Hello {user_id}! Access granted.")
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []
                else:
                    rospy.loginfo("Unrecognized QR")
                    if speechSay_pub:
                        speechSay_pub.publish("Access denied. Unrecognized QR.")
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []

        # Flash overlay
        if flash_color and current_time < flash_end_time:
            for pts in flash_rects:
                if pts:
                    overlay = frame.copy()
                    cv2.fillPoly(overlay, [np.array(pts)], flash_color)
                    cv2.addWeighted(overlay, 0.4, frame, 0.6, 0, frame)

        # Display frame (optional, can be removed in headless mode)
        cv2.imshow("QT Robot Secure QR Scanner", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Image processing error: {e}")

# ==========================
# Main function
# ==========================
def main():
    global speechSay_pub
    rospy.init_node("qtrobot_secure_qr_node")

    rospy.loginfo("QT Robot Secure QR Scanner Started")
    rospy.loginfo("Waiting for camera frames...")

    # Speech publisher
    speechSay_pub = rospy.Publisher('/qt_robot/speech/say', String, queue_size=10)

    # Subscribe to camera topic
    rospy.Subscriber("/camera/color/image_raw", Image, image_callback, queue_size=1)

    rospy.spin()
    cv2.destroyAllWindows()

# ==========================
# Entry point
# ==========================
if __name__ == "__main__":
    main()