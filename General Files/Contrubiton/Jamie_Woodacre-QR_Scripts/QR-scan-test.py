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

# ==========================
# DEBUG SECRET KEY
# ==========================
SYSTEM_SECRET = "debug_secret_key_123"

# ==========================
# HARD-CODED DEBUG CREDENTIALS
# ==========================
DEBUG_USER_ID = "debug_user"
DEBUG_QR_HASH = hmac.new(
    SYSTEM_SECRET.encode(),
    DEBUG_USER_ID.encode(),
    hashlib.sha256
).hexdigest()

# Full QR string to use for testing
DEBUG_QR_STRING = f"{DEBUG_USER_ID}:{DEBUG_QR_HASH}"

# ROS Bridge
bridge = CvBridge()

# Frame counter for debugging
frame_count = 0
last_scan_time = 0

# Speech publisher (will be initialized in main)
speechSay_pub = None

# Duration of flash (seconds)
FLASH_DURATION = 0.3
flash_end_time = 0
flash_color = None
flash_rects = []

# ==========================
# QR VERIFICATION FUNCTION
# ==========================
def verify_qr_data(scanned_text):
    rospy.loginfo("----- QR VERIFICATION START -----")
    rospy.loginfo(f"Raw QR Data: {scanned_text}")

    try:
        if scanned_text == DEBUG_QR_STRING:
            rospy.loginfo("Detected DEBUG QR - automatically valid")
            return True, DEBUG_USER_ID

        parts = scanned_text.rsplit(":", 1)
        if len(parts) != 2:
            rospy.logwarn("QR FORMAT INVALID (expected user_id:hash)")
            return False, None

        user_id, received_hash = parts
        rospy.loginfo(f"User ID: {user_id}")
        rospy.loginfo(f"Received Hash: {received_hash}")

        expected_hash = hmac.new(
            SYSTEM_SECRET.encode(),
            user_id.encode(),
            hashlib.sha256
        ).hexdigest()
        rospy.loginfo(f"Expected Hash: {expected_hash}")

        if hmac.compare_digest(received_hash, expected_hash):
            rospy.loginfo("RESULT: VALID QR")
            return True, user_id
        else:
            rospy.logwarn("RESULT: INVALID SIGNATURE")
            return False, None

    except Exception as e:
        rospy.logerr(f"Verification Exception: {e}")
        return False, None

# ==========================
# IMAGE CALLBACK
# ==========================
def image_callback(msg):
    global frame_count, last_scan_time
    global speechSay_pub, flash_end_time, flash_color, flash_rects

    frame_count += 1

    try:
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        decoded_objects = pyzbar.decode(gray)

        qr_count = len(decoded_objects)
        if qr_count > 0:
            rospy.loginfo(f"{qr_count} QR code(s) detected")

        current_time = time.time()
        for obj in decoded_objects:
            qr_text = obj.data.decode("utf-8")
            rospy.loginfo(f"Detected QR Text: {qr_text}")

            is_valid, user_id = verify_qr_data(qr_text)
            color = (0, 255, 0) if is_valid else (0, 0, 255)

            # Draw bounding box
            pts = obj.polygon
            rect_coords = None
            if pts:
                pts = [(p.x, p.y) for p in pts]
                rect_coords = pts
                for i in range(len(pts)):
                    cv2.line(frame, pts[i], pts[(i + 1) % len(pts)], color, 3)

            x, y, w, h = obj.rect
            label = f"VALID: {user_id}" if is_valid else "INVALID QR"
            cv2.putText(frame, label, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2)

            # Only act every 3 seconds to prevent spam
            if current_time - last_scan_time > 3:
                last_scan_time = current_time
                if is_valid:
                    rospy.loginfo(f"User {user_id} authenticated")
                    if speechSay_pub is not None:
                        speechSay_pub.publish(f"Hello {user_id}! Welcome to the dentist!")
                    # Flash effect
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []
                else:
                    rospy.loginfo("Unrecognized QR")
                    if speechSay_pub is not None:
                        speechSay_pub.publish("Sorry, I don't recognise your QR!")
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []

        # Apply flash if within duration
        if flash_color and current_time < flash_end_time:
            for pts in flash_rects:
                if pts:
                    overlay = frame.copy()
                    cv2.fillPoly(overlay, [np.array(pts)], flash_color)
                    cv2.addWeighted(overlay, 0.4, frame, 0.6, 0, frame)

        # Display debug info
        cv2.putText(frame, f"Frames: {frame_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255),2)
        cv2.putText(frame, f"QR detected: {qr_count}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255),2)

        cv2.imshow("QT Robot Secure QR Scanner (DEBUG)", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Image processing error: {e}")

# ==========================
# MAIN FUNCTION
# ==========================
def main():
    global speechSay_pub
    rospy.init_node("qtrobot_secure_qr_debug")

    rospy.loginfo("===================================")
    rospy.loginfo("QT Robot Secure QR Scanner Started")
    rospy.loginfo(f"Secret key: {SYSTEM_SECRET}")
    rospy.loginfo(f"DEBUG QR string: {DEBUG_QR_STRING}")
    rospy.loginfo("Waiting for camera frames...")
    rospy.loginfo("===================================")

    # Initialize speech publisher once
    speechSay_pub = rospy.Publisher('/qt_robot/speech/say', String, queue_size=10)

    # Subscribe to camera topic
    rospy.Subscriber("/camera/color/image_raw", Image, image_callback, queue_size=1)

    rospy.spin()
    cv2.destroyAllWindows()

# ==========================
# ENTRY POINT
# ==========================
if __name__ == "__main__":
    main()