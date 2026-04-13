# QTrobot Backend

## Overview

This backend service provides a RESTful API for the QTrobot Android application. It is responsible for managing application logic, handling data storage, and providing a central point of communication for the app.

The backend is built with Python and FastAPI, ensuring high performance and a modern, maintainable codebase.

---

## Technologies Used

- **Python 3**: The core programming language.
- **FastAPI**: A high-performance web framework for building APIs.
- **Uvicorn**: An ASGI server to run the FastAPI application.
- **Pydantic**: Used for data validation and defining the structure of API requests and responses.

---

## Getting Started

Follow these instructions to set up and run the backend server on your local machine for development and testing.

### Prerequisites

- Python 3.8 or newer.

### Installation & Setup

1.  **Navigate to the Backend Directory**
    Open your terminal or command prompt and change into the `backend` directory.
    

>> # 1. Create the virtual environment (only do this the first time)
>> python -m venv venv
>>
>> # 2. Activate it (do this every time you open a new terminal)
>> venv\Scripts\activate
   # 3. Pip Req
   pip install -r requirements.txt
   # 4. UviCORN
   uvicorn main:app --reload





   Keycloak: http://localhost:8080
   SwaggerUi: http://localhost:8000
   docker-compose up -d keycloak




docker build -t qtrobot-backend:v6 .
docker tag qtrobot-backend:v6 okyanusalbas/qtrobot-backend:v6
docker tag qtrobot-backend:v6 okyanusalbas/qtrobot-backend:latest
docker push okyanusalbas/qtrobot-backend:v6
docker push okyanusalbas/qtrobot-backend:latest

Run using Keycloak
docker compose up -d keycloak backend

docker pull okyanusalbas/qtrobot-backend:latest

docker compose pull
docker compose up -d


To restart without changing the files
docker compose restart backend


Testing
docker build -t qtrobot-backend:vtest .
docker tag qtrobot-backend:vtest okyanusalbas/qtrobot-backend:vtest
docker tag qtrobot-backend:vtest okyanusalbas/qtrobot-backend:vtest
docker push okyanusalbas/qtrobot-backend:vtest
docker push okyanusalbas/qtrobot-backend:vtest
docker compose up -d keycloak backend
