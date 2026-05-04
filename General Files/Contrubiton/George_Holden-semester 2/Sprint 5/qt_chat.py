#!/usr/bin/env python3

import rospy
from sensor_msgs.msg import Image
from std_msgs.msg import String
from cv_bridge import CvBridge
from qt_robot_interface.srv import speech_say
from qt_vosk_app.srv import speech_recognize
import cv2
from pyzbar import pyzbar
import time
import base64
import json
import numpy as np
import threading
import requests
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad

# ==========================
# AES-128-CBC KEY AND IV
# ==========================
AES_KEY = b"QTRobotSecretKey"
AES_IV  = b"QTRobotInitVect1"

# ==========================
# OLLAMA CONFIG
# ==========================
OLLAMA_URL   = "http://localhost:11434/api/chat"
OLLAMA_MODEL = "gemma2:2b"

SYSTEM_PROMPT = """You are QTrobot, a friendly robot in a dentist waiting room. Your sole purpose is to console and emotionally support the child. Give quick, warm, simple responses. Max 2 short sentences. Never mention pain, needles, drills, treatment or anything clinical. No asterisks, no smiley faces, no emojis. Do not speak punctuation. Speak like a kind friendly robot to a young child."""# ==========================
ANXIETY_CONTEXT = {

    "HAPPY":   "The child is feeling happy and positive about their visit today.",
    "OK":      "The child is feeling okay about their visit today, neither excited nor worried.",
    "UNSURE":  "The child is feeling unsure and a little uncertain about their visit today.",
    "SAD":     "The child is feeling sad today and may need extra warmth and comfort.",
    "ANXIOUS": "The child is feeling anxious and nervous about their visit today and needs lots of reassurance.",
}

# ==========================
# AES DECRYPTION
# ==========================
def decrypt_qr(b64_payload):
    try:
        ciphertext = base64.b64decode(b64_payload)
        cipher     = AES.new(AES_KEY, AES.MODE_CBC, AES_IV)
        plaintext  = unpad(cipher.decrypt(ciphertext), AES.block_size).decode("utf-8").strip()

        rospy.loginfo(f"Decrypted plaintext: {plaintext}")

        try:
            data = json.loads(plaintext)
            if isinstance(data, dict) and "name" in data:
                return str(data["name"]).strip(), data.get("status", "OK").upper()
        except json.JSONDecodeError:
            pass

        # Format: role|name|status
        parts = plaintext.split("|")
        if len(parts) >= 3:
            return parts[1].strip(), parts[2].strip().upper()
        elif len(parts) == 2:
            return parts[1].strip(), "OK"
        return plaintext.strip(), "OK"

    except Exception as e:
        rospy.logwarn(f"Decryption failed: {e}")
        return None, None

# ==========================
# OLLAMA CALL
# ==========================
def ask_ollama(messages):
    result = [None]
    def call():
        try:
            payload = {
                "model":    OLLAMA_MODEL,
                "messages": messages,
                "stream":   False
            }
            resp = requests.post(OLLAMA_URL, json=payload, timeout=25)
            data = resp.json()
            result[0] = data["message"]["content"].strip()
        except Exception as e:
            rospy.logerr(f"Ollama error: {e}")

    t = threading.Thread(target=call)
    t.start()
    t.join(timeout=30)

    if t.is_alive():
        rospy.logwarn("Ollama timed out - skipping response")
        return None

    return result[0]

# ==========================
# BLOCKING SPEECH
# ==========================
def say(text):
    """Call the blocking speech service so we wait until robot finishes speaking."""
    try:
        rospy.wait_for_service('/qt_robot/speech/say', timeout=5.0)
        speech_service = rospy.ServiceProxy('/qt_robot/speech/say', speech_say)
        speech_service(text)
        rospy.loginfo(f"Robot said: {text}")
    except Exception as e:
        rospy.logerr(f"Speech service error: {e}")

# ==========================
# CONVERSATION THREAD
# ==========================
def run_conversation(name, anxiety_level):
    try:
        anxiety_desc = ANXIETY_CONTEXT.get(anxiety_level, ANXIETY_CONTEXT["OK"])

        # Build the full system prompt with child context
        full_system = (
            f"{SYSTEM_PROMPT}\n\n"
            f"CURRENT CHILD: You are talking to a child called {name}. "
            f"{anxiety_desc} "
            f"Tailor everything you say to how they are feeling."
        )

        # Conversation history
        messages = [{"role": "system", "content": full_system}]

        # Initial greeting from robot
        rospy.loginfo(f"Starting conversation with {name} ({anxiety_level})")
        greeting_prompt = f"Greet {name} warmly and ask how they are feeling about their visit today."
        messages.append({"role": "user", "content": greeting_prompt})

        greeting = ask_ollama(messages)
        if not greeting:
            say(f"Hello {name}, how are you today?")
            return

        messages.append({"role": "assistant", "content": greeting})
        say(greeting)
        time.sleep(1.5)  # wait for audio to fully stop before listening

        # Listen and respond loop (up to 3 exchanges)
        rospy.wait_for_service('/qt_robot/speech/recognize', timeout=5.0)
        recognize = rospy.ServiceProxy('/qt_robot/speech/recognize', speech_recognize)

        for _ in range(100):
            rospy.loginfo("Listening for child response...")
            resp = recognize("en_US", [], 8)
            transcript = resp.transcript.strip()

            if not transcript:
                rospy.loginfo("No speech detected, ending conversation.")
                say(f"It was lovely to meet you {name}. Good luck today!")
                break

            rospy.loginfo(f"Child said: {transcript}")
            messages.append({"role": "user", "content": transcript})

            response = ask_ollama(messages)
            if not response:
                response = f"That's really interesting {name}! Tell me more, I am listening."

            messages.append({"role": "assistant", "content": response})
            say(response)
            time.sleep(1.5)  # wait for audio to fully stop before listening again

    except Exception as e:
        rospy.logerr(f"Conversation error: {e}")

# ==========================
# ROS / CV GLOBALS
# ==========================
bridge         = CvBridge()
frame_count    = 0
last_scan_time = 0

FLASH_DURATION = 0.3
flash_end_time = 0
flash_color    = None
flash_rects    = []

# ==========================
# IMAGE CALLBACK
# ==========================
def image_callback(msg):
    global frame_count, last_scan_time
    global flash_end_time, flash_color, flash_rects

    frame_count += 1

    try:
        frame = bridge.imgmsg_to_cv2(msg, desired_encoding='bgr8')
        gray  = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        decoded_objects = pyzbar.decode(gray)

        qr_count     = len(decoded_objects)
        current_time = time.time()

        if qr_count > 0:
            rospy.loginfo(f"{qr_count} QR code(s) detected")

        for obj in decoded_objects:
            raw_text = obj.data.decode("utf-8")

            # Ignore garbage/partial QR reads
            if len(raw_text) < 20:
                rospy.logwarn(f"Ignoring short QR payload: {raw_text}")
                continue

            rospy.loginfo(f"Raw QR payload: {raw_text}")

            name, anxiety_level = decrypt_qr(raw_text)
            valid = name is not None
            color = (0, 255, 0) if valid else (0, 0, 255)

            # Draw bounding polygon
            pts         = obj.polygon
            rect_coords = None
            if pts:
                pts = [(p.x, p.y) for p in pts]
                rect_coords = pts
                for i in range(len(pts)):
                    cv2.line(frame, pts[i], pts[(i + 1) % len(pts)], color, 3)

            x, y, w, h = obj.rect
            label = f"{name} - {anxiety_level}" if valid else "INVALID / WRONG KEY"
            cv2.putText(frame, label, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2)

            # Throttle to every 3 seconds
            if current_time - last_scan_time > 3:
                last_scan_time = current_time
                if valid:
                    rospy.loginfo(f"Authenticated: {name} | Anxiety: {anxiety_level}")
                    t = threading.Thread(target=run_conversation, args=(name, anxiety_level), daemon=True)
                    t.start()
                else:
                    rospy.logwarn("QR could not be decrypted")
                    threading.Thread(target=say, args=("Sorry, I could not read your QR code!",), daemon=True).start()

                flash_end_time = current_time + FLASH_DURATION
                flash_color    = color
                flash_rects    = [rect_coords] if rect_coords else []

        # Flash overlay
        if flash_color and current_time < flash_end_time:
            for pts in flash_rects:
                if pts:
                    overlay = frame.copy()
                    cv2.fillPoly(overlay, [np.array(pts)], flash_color)
                    cv2.addWeighted(overlay, 0.4, frame, 0.6, 0, frame)

        # Debug HUD
        cv2.putText(frame, f"Frames: {frame_count}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255), 2)
        cv2.putText(frame, f"QR detected: {qr_count}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255), 2)

        cv2.imshow("QT Robot AES-128-CBC QR Scanner", frame)
        cv2.waitKey(1)

    except Exception as e:
        rospy.logerr(f"Image processing error: {e}")

# ==========================
# MAIN
# ==========================
def main():
    rospy.init_node("qtrobot_aes_qr_scanner")

    rospy.loginfo("===================================")
    rospy.loginfo("QT Robot AES-128-CBC QR Scanner")
    rospy.loginfo(f"Using key: {AES_KEY} / IV: {AES_IV}")
    rospy.loginfo("Waiting for camera frames...")
    rospy.loginfo("===================================")

    rospy.Subscriber("/camera/color/image_raw", Image, image_callback, queue_size=1)

    rospy.spin()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()