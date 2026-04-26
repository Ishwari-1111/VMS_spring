import { useState, useEffect } from "react";
import { jsPDF } from "jspdf";
import { certificatesAPI } from "../../api/certificates";
import { volunteersAPI } from "../../api/volunteers";
import LoadingSpinner from "../../components/LoadingSpinner";
import ErrorAlert from "../../components/ErrorAlert";
import EmptyState from "../../components/EmptyState";
import { useAuth } from "../../hooks/useAuth";

function formatDate(date) {
  return new Date(date).toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function downloadCertificatePdf(certificate) {
  const doc = new jsPDF({ orientation: "landscape", unit: "pt", format: "a4" });
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const navy = [15, 33, 62];
  const gold = [191, 148, 63];

  doc.setFillColor(255, 255, 255);
  doc.rect(0, 0, pageWidth, pageHeight, "F");

  doc.setDrawColor(...gold);
  doc.setLineWidth(3);
  doc.rect(36, 36, pageWidth - 72, pageHeight - 72);
  doc.setLineWidth(1);
  doc.rect(48, 48, pageWidth - 96, pageHeight - 96);

  doc.setFillColor(...navy);
  doc.roundedRect(pageWidth / 2 - 42, 64, 84, 42, 6, 6, "F");
  doc.setTextColor(255, 255, 255);
  doc.setFont("times", "bold");
  doc.setFontSize(20);
  doc.text("VMS", pageWidth / 2, 92, { align: "center" });

  doc.setDrawColor(...gold);
  doc.setLineWidth(2);
  doc.circle(pageWidth - 120, 120, 44, "S");
  doc.setFont("times", "bold");
  doc.setFontSize(10);
  doc.setTextColor(...navy);
  doc.text("OFFICIAL", pageWidth - 120, 114, { align: "center" });
  doc.text("SEAL", pageWidth - 120, 128, { align: "center" });

  doc.setTextColor(...navy);
  doc.setFont("times", "bold");
  doc.setFontSize(36);
  doc.text("CERTIFICATE OF COMPLETION", pageWidth / 2, 170, {
    align: "center",
  });

  doc.setFont("times", "italic");
  doc.setFontSize(18);
  doc.text("This is to certify that", pageWidth / 2, 220, { align: "center" });

  doc.setTextColor(...gold);
  doc.setFont("times", "bold");
  doc.setFontSize(38);
  doc.text(certificate.volunteerName, pageWidth / 2, 280, { align: "center" });

  doc.setTextColor(...navy);
  doc.setFont("times", "normal");
  doc.setFontSize(18);
  doc.text("has successfully completed", pageWidth / 2, 325, {
    align: "center",
  });

  doc.setFont("times", "bold");
  doc.setFontSize(28);
  doc.text(certificate.eventName, pageWidth / 2, 365, { align: "center" });

  const completionDate = certificate.completionDate || certificate.issuedDate;
  doc.setFont("times", "normal");
  doc.setFontSize(16);
  doc.text(
    `Completion Date: ${formatDate(completionDate)}`,
    pageWidth / 2,
    410,
    {
      align: "center",
    },
  );

  doc.setDrawColor(...navy);
  doc.setLineWidth(1);
  doc.line(
    pageWidth / 2 - 140,
    pageHeight - 120,
    pageWidth / 2 + 140,
    pageHeight - 120,
  );
  doc.setFont("times", "normal");
  doc.setFontSize(14);
  doc.text("Authorized Signature", pageWidth / 2, pageHeight - 102, {
    align: "center",
  });

  doc.setFontSize(11);
  doc.text(
    `Certificate Code: ${certificate.certificateCode}`,
    58,
    pageHeight - 58,
  );

  const safeName = `${certificate.volunteerName}-${certificate.eventName}`
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-");
  doc.save(`${safeName}-certificate.pdf`);
}

export default function CertificatesList() {
  const { user } = useAuth();
  const [certificates, setCertificates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const roleValue = String(user?.role || "").toLowerCase();
  const isAdmin = roleValue === "admin";

  useEffect(() => {
    fetchCertificates();
  }, [isAdmin, user?.email, user?.username]);

  const fetchCertificates = async () => {
    try {
      setLoading(true);
      setError(null);

      if (isAdmin) {
        const response = await certificatesAPI.getAll();
        setCertificates(response.data?.certificates || []);
        return;
      }

      const volunteersResponse = await volunteersAPI.getAll();
      const matchedVolunteer = volunteersResponse.data.find(
        (vol) =>
          (vol.email && vol.email === user?.email) ||
          (vol.name && vol.name === user?.username),
      );

      if (!matchedVolunteer) {
        setCertificates([]);
        return;
      }

      const response = await certificatesAPI.getByVolunteer(
        matchedVolunteer.volunteerId,
      );
      setCertificates(response.data?.certificates || []);
    } catch (err) {
      setError("Failed to load certificates");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div>
        <h1 className="text-3xl font-bold text-neutral-900">
          {isAdmin ? "Certificates" : "My Certificates"}
        </h1>
        <p className="text-neutral-600 mt-1">
          {isAdmin
            ? "View all published certificates"
            : "View your published certificates"}
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      ) : certificates.length === 0 ? (
        <div className="card">
          <EmptyState
            title="No Certificates"
            description="Certificates will appear automatically after event completion"
          />
        </div>
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-neutral-50 border-b border-neutral-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Certificate Code
                  </th>
                  {isAdmin && (
                    <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                      Volunteer
                    </th>
                  )}
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Event
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Completion Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-neutral-700">
                    Status
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-neutral-700">
                    Action
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-neutral-200">
                {certificates.map((cert) => (
                  <tr
                    key={cert.certificateId}
                    className="hover:bg-neutral-50 transition-colors"
                  >
                    <td className="px-6 py-4 text-sm font-mono text-neutral-600">
                      {cert.certificateCode}
                    </td>
                    {isAdmin && (
                      <td className="px-6 py-4 text-sm font-medium text-neutral-900">
                        {cert.volunteerName}
                      </td>
                    )}
                    <td className="px-6 py-4 text-sm text-neutral-600">
                      {cert.eventName}
                    </td>
                    <td className="px-6 py-4 text-sm text-neutral-600">
                      {formatDate(cert.completionDate || cert.issuedDate)}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <span className="badge-success">{cert.status}</span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => downloadCertificatePdf(cert)}
                        className="btn-secondary"
                      >
                        Download PDF
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
