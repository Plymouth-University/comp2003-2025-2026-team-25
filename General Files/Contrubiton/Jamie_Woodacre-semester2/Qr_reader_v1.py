#!/usr/bin/env python3
import rospy
from sensor_msgs.msg import Image
from cv_bridge import CvBridge
import cv2
from pyzbar import pyzbar
import numpy as np

bridge = CvBridge()


def image_callback(msg):
    try:
        # Convert ROS Image to OpenCV image
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')

        # Convert to grayscale for better detection
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Detect QR codes in the frame
        decoded_objects = pyzbar.decode(gray)

        for obj in decoded_objects:
            # Get bounding box coordinates
            pts = obj.polygon
            if pts:
                pts = [(point.x, point.y) for point in pts]
                for i in range(len(pts)):
                    cv2.line(frame, pts[i], pts[(i + 1) % len(pts)], (0, 255, 0), 2)

            # Draw the QR code data above the bounding box
            x, y, w, h = obj.rect
            cv2.putText(frame, obj.data.decode('utf-8'), (x, y - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
            rospy.loginfo(f"Detected QR: {obj.data.decode('utf-8')}")

        # Show the frame
        cv2.imshow("QR Code Detection", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Error processing image: {e}")


def main():
    rospy.init_node('qtrobot_qr_reader')
    rospy.loginfo("QR reader node started. Press Ctrl+C to exit.")

    # Subscribe to the RealSense color image topic
    rospy.Subscriber("/camera/color/image_raw", Image, image_callback)

    # Spin until node is shutdown
    rospy.spin()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()