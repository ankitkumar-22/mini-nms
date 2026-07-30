import { apiClient } from "./client";

export async function fetchDevices() {
  const { data } = await apiClient.get("/devices");
  return data;
}

export async function fetchDevice(id) {
  const { data } = await apiClient.get(`/devices/${id}`);
  return data;
}

export async function createDevice({ name, ipAddress }) {
  const { data } = await apiClient.post("/devices", { name, ipAddress });
  return data;
}

export async function deleteDevice(id) {
  await apiClient.delete(`/devices/${id}`);
}
