# Use a newer Python runtime (3.11+) so we can use modern syntax like
# the PEP 604 union type (`str | None`). Python 3.9 doesn't support this syntax,
# which was causing the container to crash during startup.
FROM python:3.11-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install the dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the backend application code into the container
COPY . .

# Expose the port the app runs on
EXPOSE 8000

# Define the command to run your app
# Use --host 0.0.0.0 to make it accessible from outside the container
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]