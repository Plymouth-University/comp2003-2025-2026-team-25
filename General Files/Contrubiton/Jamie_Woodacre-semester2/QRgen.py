import qrcode
import hmac
import hashlib


SECRET_KEY = b'testString' 

def generateQR(user_id: str):
    signature = hmac.new(SECRET_KEY, user_id.encode(), hashlib.sha256).hexdigest()
    short_sig = signature[:10]
    qr_data = f"{user_id}:{short_sig}"
    qr = qrcode.QRCode(box_size=10, border=4)
    qr.add_data(qr_data)
    qr.make(fit=True)
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(f"user_{user_id}_qr.png")
    print(f"Generated QR for {user_id} with data: {qr_data}")
    return qr_data
