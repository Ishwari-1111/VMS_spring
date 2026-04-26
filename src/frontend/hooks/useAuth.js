import { useState, useEffect, useCallback } from "react";
import { authAPI } from "../api/auth";

function extractApiError(err, fallbackMessage) {
  const data = err?.response?.data;
  if (!data) return fallbackMessage;

  return (
    data.error ||
    data.message ||
    data.details ||
    (typeof data.role === "string" && data.role) ||
    fallbackMessage
  );
}

export function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (err) {
        setError("Failed to load user");
        localStorage.removeItem("user");
      }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (username, password) => {
    try {
      setError(null);
      const { data } = await authAPI.login(username, password);
      localStorage.setItem("authToken", data.token);
      localStorage.setItem(
        "user",
        JSON.stringify({
          username: data.username,
          email: data.email,
          role: data.role,
        }),
      );
      setUser({
        username: data.username,
        email: data.email,
        role: data.role,
      });
      return data;
    } catch (err) {
      const message = extractApiError(err, "Login failed");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const signup = useCallback(async (username, email, password, role) => {
    try {
      setError(null);
      const { data } = await authAPI.signup(username, email, password, role);
      localStorage.setItem("authToken", data.token);
      localStorage.setItem(
        "user",
        JSON.stringify({
          username: data.username,
          email: data.email,
          role: data.role,
        }),
      );
      setUser({
        username: data.username,
        email: data.email,
        role: data.role,
      });
      return data;
    } catch (err) {
      const message = extractApiError(err, "Signup failed");
      setError(message);
      throw new Error(message);
    }
  }, []);

  const logout = useCallback(() => {
    authAPI.logout();
    setUser(null);
  }, []);

  return { user, loading, error, login, signup, logout };
}
