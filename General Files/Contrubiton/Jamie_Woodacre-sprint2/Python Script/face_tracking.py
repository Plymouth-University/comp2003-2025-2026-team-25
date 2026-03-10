#!/usr/bin/env python3

import rospy
from sensor_msgs.msg import Image
from std_msgs.msg import String, Float64MultiArray
from cv_bridge import CvBridge
import cv2
import numpy as np
import time

# ROS Bridge
bridge = CvBridge()

# State machine states
SCANNING = 0
TRACKING = 1

# Parameters
SCANNING_TIME = 5  # Time for scanning before switching (in seconds)
TRACKING_TIME = 5  # Time to track face after detection (in seconds)

# Variables
state = SCANNING
tracking_start_time = None
frame_count = 0
speechSay_pub = None
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')


# ==========================
# IMAGE CALLBACK
# ==========================
def image_callback(msg):
    global frame_count
    global state
    global tracking_start_time
    global speechSay_pub

    frame_count += 1

    try:
        # Convert the ROS image message to OpenCV image
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Detect faces in the image
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))

        # If in scanning mode, keep scanning
        if state == SCANNING:
            # In scanning mode, just move the head back and forth and check for faces
            if len(faces) > 0:
                # First face detected, switch to tracking mode
                state = TRACKING
                tracking_start_time = time.time()  # Start tracking timer
                rospy.loginfo("Face detected! Switching to tracking mode.")
        elif state == TRACKING:
            # In tracking mode, track the first face
            # Track the largest face
            largest_face = None
            max_area = 0

            for (x, y, w, h) in faces:
                area = w * h
                if area > max_area:
                    max_area = area
                    largest_face = (x, y, w, h)

            if largest_face:
                x, y, w, h = largest_face
                # Draw bounding box around the largest face
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                face_center_x = x + w // 2
                face_center_y = y + h // 2

                # Draw center of face
                cv2.circle(frame, (face_center_x, face_center_y), 5, (0, 255, 255), -1)

                # Calculate the offset from the center of the frame
                center_x = 320  # For a 640x480 image
                offset_x = face_center_x - center_x

                # Calculate head yaw angle
                head_yaw_angle = np.clip(offset_x * 0.14, -45, 45)
                rospy.loginfo(f"Tracking face at ({face_center_x}, {face_center_y}), HeadYaw = {head_yaw_angle}")

                # After detecting face, only move the head after tracking time has elapsed
                if time.time() - tracking_start_time >= TRACKING_TIME:
                    rospy.loginfo(f"Tracking time exceeded. Moving head to track face.")
                    # Publish the head position command to track the face
                    head_position_msg = Float64MultiArray()
                    head_position_msg.data = [head_yaw_angle, 0.0]  # [HeadYaw, HeadPitch]
                    rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(
                        head_position_msg)

                    # Check if tracking time has passed and stop tracking after a while
                    state = SCANNING
                    rospy.loginfo(f"Switching back to scanning mode after tracking.")

            else:
                # No face detected while tracking, go back to scanning mode
                rospy.loginfo("No face detected during tracking. Returning to scanning mode.")
                state = SCANNING
                reset_head_position()

        # If no face detected, keep scanning
        if state == SCANNING:
            # Implement a simple scanning pattern by moving the head back and forth
            head_yaw_angle = np.sin(time.time() * 0.5) * 45  # Sine wave movement for scanning
            rospy.loginfo(f"Scanning... HeadYaw = {head_yaw_angle}")

            # Publish the head position command
            head_position_msg = Float64MultiArray()
            head_position_msg.data = [head_yaw_angle, 0.0]
            rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(
                head_position_msg)

        # Display the image with bounding boxes
        cv2.putText(frame, f"Frames: {frame_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)
        cv2.putText(frame, f"Faces detected: {len(faces)}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)

        cv2.imshow("QT Robot Face Tracker", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Image processing error: {e}")


# ==========================
# Reset head position to center
# ==========================
def reset_head_position():
    head_position_msg = Float64MultiArray()
    head_position_msg.data = [0.0, 0.0]  # Center position [HeadYaw, HeadPitch]
    rospy.Publisher('/qt_robot/head_position/command', Float64MultiArray, queue_size=10).publish(head_position_msg)
    rospy.loginfo("Head reset to center.")


# ==========================
# MAIN FUNCTION
# ==========================
def main():
    global speechSay_pub
    rospy.init_node("qtrobot_face_tracker")

    rospy.loginfo("===================================")
    rospy.loginfo("QT Robot Face Tracker Started")
    rospy.loginfo("Waiting for camera frames...")
    rospy.loginfo("===================================")

    # Initialize speech publisher once
    speechSay_pub = rospy.Publisher('/qt_robot/speech/say', String, queue_size=10)

    # Subscribe to camera topic
    rospy.Subscriber("/camera/color/image_raw", Image, image_callback, queue_size=1)

    # Ensure OpenCV is working in a GUI-enabled environment
    if not cv2.useOptimized():
        rospy.logwarn("OpenCV is not optimized for your environment!")

    # ROS loop
    rospy.spin()
    cv2.destroyAllWindows()


# ==========================
# ENTRY POINT
# ==========================
if __name__ == "__main__":
    try:
        main()
    except rospy.ROSInterruptException:
        rospy.loginfo("QT Robot Face Tracker node terminated.")
        cv2.destroyAllWindows()

#rosrun rqt_image_view rqt_image_view