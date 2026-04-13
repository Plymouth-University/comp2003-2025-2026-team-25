from fastapi.testclient import TestClient

from backend import main


client = TestClient(main.app)


def test_health_endpoint_returns_ok_status() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data.get("status") == "ok"
    assert data.get("service") == "QTrobot Backend"

