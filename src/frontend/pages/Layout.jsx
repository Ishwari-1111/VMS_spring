import Header from "../components/Header";
import Sidebar from "../components/Sidebar";

export default function Layout({ children }) {
  return (
    <div className="h-screen flex flex-col bg-transparent">
      <Header />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-auto">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
