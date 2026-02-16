from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

# --- Pydantic Models ---
# These models define the structure of the data your API will handle.
# Pydantic ensures that incoming data matches this structure.

class UserBase(BaseModel):
    """Base model for user data. All user models will share these fields."""
    name: str
    age: Optional[int] = None

class UserCreate(UserBase):
    """Model used specifically for creating a new user."""
    pass  # No extra fields needed for creation

class User(UserBase):
    """Model representing a user as it is stored and returned by the API."""
    id: int

# --- In-Memory Database ---
# This is a temporary stand-in for a real database.
# It's just a list that will store our user data while the server is running.
# The data will be lost if you restart the server.
db_users: List[User] = []
next_user_id = 1

# --- FastAPI Application ---
app = FastAPI(title="QTrobot Backend")

@app.get("/")
def read_root():
    """A simple endpoint to confirm the server is running."""
    return {"message": "Hello from the QTrobot Backend"}

# --- User API Endpoints ---

@app.post("/users", response_model=User, status_code=201)
def create_user(user: UserCreate):
    """
    Creates a new user profile.

    - Receives a `UserCreate` object in the request body.
    - Assigns a unique ID.
    - Adds the new user to our in-memory database.
    - Returns the newly created `User` object.
    """
    global next_user_id
    # Check if user already exists
    for existing_user in db_users:
        if existing_user.name.lower() == user.name.lower():
            raise HTTPException(status_code=400, detail=f"User with name '{user.name}' already exists.")

    new_user = User(id=next_user_id, name=user.name, age=user.age)
    db_users.append(new_user)
    next_user_id += 1
    return new_user

@app.get("/users", response_model=List[User])
def get_users():
    """Returns a list of all registered users."""
    return db_users