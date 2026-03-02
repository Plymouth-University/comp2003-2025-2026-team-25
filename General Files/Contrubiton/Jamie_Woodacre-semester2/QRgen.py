#QR code generator for the QT Robot
import qrcode
import hmac
import hashlib
import time

#used to encrypt 
User_key_string = 'testString'

def generateQR(user_id: str):

    timestamp = str(int(time.time()))
    dataforQR = f"{user_id}:{timestamp}"
    authkey = hmac.new(User_key_string.encode(), dataforQR.encode(), hashlib.sha256).hexdigest()
    QRvalue = f"{user_id}.{timestamp}.{authkey[:10]}"
    code = qrcode.make(QRvalue)
    code.save(f"{user_id}_QR-code.png")
    
    return QRvalue

#test
generateQR("testUser")


#dependencies for this script to work: qrcode, pillow, hmac, hashlib, time
#to do: update the QR script and definition of the user_id to allow alphanumeric compatibility --> DONE

#feedback received : just using user id is insecure -change it 
# needs to include a unique key, needs to expire and needs to be live for robot to verify --> Mostly done

#new todo: functionality with database, robot and application