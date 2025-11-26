#!/bin/bash
# run_main.sh - A simple script to run main.py

# Absolute path to the Python script
PYTHON_SCRIPT="/home/qtrobot/scripts/main.py"

# Check if the script exists
if [ ! -f "$PYTHON_SCRIPT" ]; then
    echo "Error: $PYTHON_SCRIPT not found!"
    exit 1
fi

# Run the Python script with Python 3
python3 "$PYTHON_SCRIPT"