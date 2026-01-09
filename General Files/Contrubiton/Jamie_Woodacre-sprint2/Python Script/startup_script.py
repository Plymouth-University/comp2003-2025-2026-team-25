import rospy
from qt_robot_interface.srv import emotion_show
from std_msgs.msg import String

def main():
    
    #node
    rospy.init_node('qt_robot_tts_publisher', anonymous=True)
    
    #publishers for actions
    speechSay_pub = rospy.Publisher('/qt_robot/speech/say', String, queue_size=10)
    gesturePlay_pub = rospy.Publisher('/qt_robot/gesture/play', String, queue_size=10)
    emotionsShow_pub = rospy.Publisher('/qt_robot/emotion/show', String, queue_size=10)
    #quick break for publishers to start properly
    rospy.sleep(1)
    
    #actions to be carried out for greeting
    rospy.loginfo("Publishing Speech: greeting message")
    speechSay_pub.publish("Hello! I am QT! I am here to help you at the dentist!")
    rospy.loginfo("Publishing Gesture: 'QT/waving'")
    gesturePlay_pub.publish("QT/bye")
    rospy.loginfo("Publishing emotion: 'QT/happy'")
    emotionsShow_pub.publish("QT/happy")

if __name__ == '__main__':
    try:
        main()
    except rospy.ROSInterruptException:
        Pass
