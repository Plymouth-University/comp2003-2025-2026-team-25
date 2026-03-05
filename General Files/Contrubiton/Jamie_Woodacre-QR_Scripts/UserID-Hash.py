import hmac
import hashlib

def get_auth_hash(user_id: str, secret_key: str):                                   #takes both of these from the api 

    secret_bytes = secret_key.encode('utf-8')                                       #making sure both are in bytes so that the HMAC has no errors
    data_bytes = user_id.encode('utf-8')

    signature = hmac.new(secret_bytes, data_bytes, hashlib.sha256).hexdigest()      #creates the signature

    return signature                                                                #outputs hash of user id and a secret key