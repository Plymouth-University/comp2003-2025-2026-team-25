# QTrobot Backend (Python)

## Overview
This backend service provides a RESTful API that enables communication
between an Android application (written in Java) and the QTrobot platform.
The backend is responsible for processing user input, managing application
logic, and triggering QTrobot behaviours such as speech and gestures.

The system follows a modular and service-based architecture to ensure
maintainability, scalability, and clear separation of concerns.

---

## Technologies Used

### Python 3
Python is used as the backend programming language due to its strong
support for robotics, rapid development capabilities, and extensive
ecosystem of libraries.

---

### FastAPI
FastAPI is a modern, high-performance web framework used to build the
backend’s REST API. It provides:
- Automatic request validation
- JSON serialization
- Interactive API documentation
- Asynchronous request handling

FastAPI enables efficient communication between the Android frontend
and the QTrobot backend.

---

### Uvicorn
Uvicorn is an ASGI server used to run the FastAPI application.
It supports asynchronous execution and is suitable for both development
and production environments.

---

### Pydantic
Pydantic is used for defining data models and validating incoming
and outgoing JSON data. This ensures:
- Correct request structure
- Type safety
- Reliable communication between frontend and backend

---

### RESTful API Architecture
The backend exposes RESTful endpoints that can be accessed by any client,
regardless of programming language. This allows seamless integration
with the Android application developed in Java.

---

### Modular Service-Based Design
The backend logic is divided into services (e.g. chat handling, robot control),
making the system easier to extend and maintain. This approach supports:
- Cleaner code
- Easier testing
- Future feature expansion

---

### QTrobot Integration
The backend is designed to interface with QTrobot’s Python SDK.
Robot-specific functionality (such as speech and gestures) is abstracted
into dedicated service classes, allowing the backend logic to remain
independent of the robot hardware.

---

### Testing
The backend includes support for unit testing using `pytest` and API
testing using `httpx`. This ensures correctness and reliability of backend
functionality.

---

## Installation & Setup

1. Navigate to the backend directory:
   ```bash
   cd backend









cd backend
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000




Later Docker Maybe ?? for versions



python -m venv venv
venv\Scripts\activate



pip install -r requirements.txt


uvicorn app.main:app --host 0.0.0.0 --port 8000



📱 Android (Frontend)

Handles UI

Sends requests (e.g. user data, chat messages)

Displays responses

Does NOT directly access the server database

🧠 Python Backend (Your Job)

Exposes API endpoints

Applies business logic

Validates data

Talks to the database

Optionally controls QTrobot

🗄 Database

Stores:

User profiles

Progress

Appointments

Chat history (if needed)