import client from "./client";

export const volunteersAPI = {
  getAll: () => client.get("/volunteers"),

  getById: (id) => client.get(`/volunteers/${id}`),

  create: (volunteerId, name, email) =>
    client.post("/volunteers/signup", { volunteerId, name, email }),

  delete: (id) => client.delete(`/volunteers/${id}`),

  getCount: () => client.get("/volunteers/count"),
};
