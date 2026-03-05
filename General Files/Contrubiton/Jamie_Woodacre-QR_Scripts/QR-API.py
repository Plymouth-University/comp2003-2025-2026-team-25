import os
import hmac
import hashlib
from fastapi import FastAPI
from dotenv import load_dotenv

load_dotenv()
app = FastAPI()

SYSTEM_SECRET = os.getenv("QR_SECRET_KEY")

def get_auth_hash(user_id: str, secret_key: str): 
    secret_bytes = secret_key.encode('utf-8')
    data_bytes = user_id.encode('utf-8')

    return hmac.new(secret_bytes, data_bytes, hashlib.sha256).hexdigest()

@app.get("/generate-hash/{user_id}") 
async def create_qr_data(user_id: str):
    auth_hash = get_auth_hash(user_id, SYSTEM_SECRET)
    
    return {
        "status": "success",
        "user_id": user_id,
        "auth_hash": auth_hash,
        "raw_string": f"{user_id}:{auth_hash}" 
    }