import client from "./client";

export const authAPI = {
  signup: (username, email, password, role = "VOLUNTEER") =>
    client.post("/auth/signup", { username, email, password, role }),

  login: (username, password) =>
    client.post("/auth/login", { username, password }),

  getCurrentUser: (token) =>
    client.get("/auth/me", {
      headers: { Authorization: `Bearer ${token}` },
    }),

  logout: () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("user");
  },
};
