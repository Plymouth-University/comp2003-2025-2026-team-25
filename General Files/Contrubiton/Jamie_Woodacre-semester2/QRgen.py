#QR code generator for the QT Robot
import qrcode
#function to take user id as a parameter for QR generation
def generateQR(user_id):
    code = qrcode.make(user_id)
    code.save(f"{user_id}_QR-code.png")
    return 0



#test data
generateQR(1)
generateQR(2)

#dependencies for this script to work: qrcode, pillow