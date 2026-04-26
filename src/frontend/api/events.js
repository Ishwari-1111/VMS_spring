import client from "./client";

export const eventsAPI = {
  getAll: () => client.get("/events"),

  getById: (id) => client.get(`/events/${id}`),

  create: (eventName, date, finishDate) =>
    client.post("/events", { eventName, date, finishDate }),

  delete: (id) => client.delete(`/events/${id}`),

  enrollVolunteer: (eventId, volunteerId) =>
    client.post(`/events/${eventId}/volunteers/${volunteerId}`),

  unenrollVolunteer: (eventId, volunteerId) =>
    client.delete(`/events/${eventId}/volunteers/${volunteerId}`),

  logHours: (eventId, volunteerId, hours) =>
    client.post(`/events/${eventId}/volunteers/${volunteerId}/hours`, {
      hours,
    }),

  getStats: (eventId) => client.get(`/events/${eventId}/stats`),

  completeEvent: (eventId, finishDate) =>
    client.post(`/events/${eventId}/complete`, { finishDate }),

  markIncomplete: (eventId) => client.post(`/events/${eventId}/incomplete`),
};
