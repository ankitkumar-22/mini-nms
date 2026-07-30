import axios from "axios";

export const apiClient = axios.create({
  baseURL: "/api",
  headers: { "Content-Type": "application/json" },
});

export function extractApiError(error) {
  const data = error?.response?.data;
  if (data?.fieldErrors) {
    return { message: data.message, fieldErrors: data.fieldErrors };
  }
  if (data?.message) {
    return { message: data.message, fieldErrors: {} };
  }
  return { message: "Something went wrong. Please try again.", fieldErrors: {} };
}
