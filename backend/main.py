from fastapi import FastAPI
from pydantic import BaseModel
from qt_robot import QTRobot

app = FastAPI()
robot = QTRobot()

class ChatRequest(BaseModel):
    message: str

@app.post("/chat")
def chat(req: ChatRequest):
    reply = f"I heard you say: {req.message}"
    robot.speak(reply)
    return {
        "reply": reply
    }

@app.get("/status")
def status():
    return {"robot": "online"}
