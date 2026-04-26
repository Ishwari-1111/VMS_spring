import client from "./client";

export const certificatesAPI = {
  getAll: () => client.get("/certificates"),

  generateForEvent: (eventId) =>
    client.post(`/certificates/generate/event/${eventId}`),

  generateForVolunteer: (volunteerId, eventId) =>
    client.post(`/certificates/generate/${volunteerId}/${eventId}`),

  getByVolunteer: (volunteerId) =>
    client.get(`/certificates/volunteer/${volunteerId}`),

  getByEvent: (eventId) => client.get(`/certificates/event/${eventId}`),

  getCountByEvent: (eventId) =>
    client.get(`/certificates/event/${eventId}/count`),
};
