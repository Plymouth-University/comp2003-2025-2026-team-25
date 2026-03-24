#!/usr/bin/env python3

import rospy
from sensor_msgs.msg import Image
from std_msgs.msg import String, Float64MultiArray
from cv_bridge import CvBridge
import cv2
import numpy as np
import time
from pyzbar import pyzbar
import hmac
import hashlib

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

# State machine states
SCANNING = 0
TRACKING = 1

# Parameters
SCANNING_TIME = 5  # Time for scanning before switching (in seconds)
TRACKING_TIME = 5  # Time to track face after detection (in seconds)
QR_PAUSE_TIME = 2  # Pause time in seconds when QR is detected

# Variables for face tracking
state = SCANNING
tracking_start_time = None
frame_count = 0
speechSay_pub = None
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

# Variables for QR code detection
last_scan_time = 0
FLASH_DURATION = 0.3
flash_end_time = 0
flash_color = None
flash_rects = []
is_qr_detected = False
qr_detection_time = 0  # Track the time QR was detected

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
    global state, tracking_start_time, is_qr_detected, qr_detection_time

    frame_count += 1
    current_time = time.time()

    try:
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # If a QR code is detected, stop head movement for QR reading
        if is_qr_detected and (current_time - qr_detection_time < QR_PAUSE_TIME):
            rospy.loginfo("QR detected, pausing head movement.")
            # Skip the head movement logic here
            cv2.putText(frame, "Reading QR code...", (10, 90), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 0), 2)
            rospy.loginfo("Paused head movement while reading QR code.")
        else:
            # Face tracking logic
            faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))

            # If in scanning mode, move head back and forth
            if state == SCANNING:
                if len(faces) > 0:
                    state = TRACKING
                    tracking_start_time = current_time
                    rospy.loginfo("Face detected! Switching to tracking mode.")
                else:
                    # In scanning mode, move the head back and forth
                    head_yaw_angle = np.sin(current_time * 0.5) * 45  # Sine wave movement for scanning
                    rospy.loginfo(f"Scanning... HeadYaw = {head_yaw_angle}")

                    # Publish head position
                    head_position_msg = Float64MultiArray()
                    head_position_msg.data = [head_yaw_angle, 0.0]
                    rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(head_position_msg)

                    # Check if the scanning time has exceeded the limit
                    if current_time - last_scan_time >= SCANNING_TIME:
                        last_scan_time = current_time
                        rospy.loginfo("Switching back to scanning mode after timeout.")
                        state = SCANNING  # Reset to scanning mode

            elif state == TRACKING:
                largest_face = None
                max_area = 0

                for (x, y, w, h) in faces:
                    area = w * h
                    if area > max_area:
                        max_area = area
                        largest_face = (x, y, w, h)

                if largest_face:
                    x, y, w, h = largest_face
                    cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                    face_center_x = x + w // 2
                    face_center_y = y + h // 2
                    cv2.circle(frame, (face_center_x, face_center_y), 5, (0, 255, 255), -1)

                    center_x = 320
                    offset_x = face_center_x - center_x
                    head_yaw_angle = np.clip(offset_x * 0.14, -45, 45)

                    rospy.loginfo(f"Tracking face at ({face_center_x}, {face_center_y}), HeadYaw = {head_yaw_angle}")

                    # After detecting face, move the head after tracking time has elapsed
                    if current_time - tracking_start_time >= TRACKING_TIME:
                        rospy.loginfo(f"Tracking time exceeded. Moving head to track face.")
                        head_position_msg = Float64MultiArray()
                        head_position_msg.data = [head_yaw_angle, 0.0]  # [HeadYaw, HeadPitch]
                        rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(head_position_msg)
                        state = SCANNING
                        rospy.loginfo(f"Switching back to scanning mode after tracking.")

                else:
                    # No face detected while tracking, go back to scanning mode
                    rospy.loginfo("No face detected during tracking. Returning to scanning mode.")
                    state = SCANNING
                    reset_head_position()

        # QR code detection logic
        decoded_objects = pyzbar.decode(gray)

        for obj in decoded_objects:
            qr_text = obj.data.decode("utf-8")
            rospy.loginfo(f"Detected QR Text: {qr_text}")

            is_valid, user_id = verify_qr_data(qr_text)
            color = (0, 255, 0) if is_valid else (0, 0, 255)

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

            current_time = time.time()
            if current_time - last_scan_time > 3:
                last_scan_time = current_time
                if is_valid:
                    rospy.loginfo(f"User {user_id} authenticated")
                    if speechSay_pub is not None:
                        speechSay_pub.publish(f"Hello {user_id}, Welcome!")
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []
                else:
                    rospy.loginfo("Unrecognized QR")
                    if speechSay_pub is not None:
                        speechSay_pub.publish("Sorry, I don't recognize your QR!")
                    flash_end_time = current_time + FLASH_DURATION
                    flash_color = color
                    flash_rects = [rect_coords] if rect_coords else []

                # Set the QR detection flag and timestamp
                is_qr_detected = True
                qr_detection_time = current_time

        if flash_color and current_time < flash_end_time:
            for pts in flash_rects:
                if pts:
                    overlay = frame.copy()
                    cv2.fillPoly(overlay, [np.array(pts)], flash_color)
                    cv2.addWeighted(overlay, 0.4, frame, 0.6, 0, frame)

        # Display debug info
        cv2.putText(frame, f"Frames: {frame_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)
        cv2.putText(frame, f"QR detected: {len(decoded_objects)}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)

        cv2.imshow("QT Robot Face & QR Scanner", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Image processing error: {e}")

# ==========================
# Reset head position to center
# ==========================
def reset_head_position():
    head_position_msg = Float64MultiArray()
    head_position_msg.data = [0.0, 0.0]
    rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(head_position_msg)
    rospy.loginfo("Head reset to center.")

# ==========================
# MAIN FUNCTION
# ==========================
def main():
    global speechSay_pub
    rospy.init_node("qtrobot_face_qr_tracker")

    rospy.loginfo("===================================")
    rospy.loginfo("QT Robot Face & QR Scanner Started")
    rospy.loginfo("===================================")

    # Initialize speech publisher
    speechSay_pub = rospy.Publisher('/qt_robot/speech/say', String, queue_size=10)

    # Subscribe to the camera topic
    rospy.Subscriber("/camera/color/image_raw", Image, image_callback, queue_size=1)

    # Spin to keep the node alive
    rospy.spin()

# ==========================
# ENTRY POINT
# ==========================
if __name__ == "__main__":
    try:
        main()
    except rospy.ROSInterruptException:
        rospy.loginfo("QT Robot Face & QR Scanner node terminated.")
        cv2.destroyAllWindows()