import Sidebar from "./Sidebar";

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-background text-on-surface font-body-md selection:bg-surface-tint selection:text-background">
      <Sidebar />
      <div className="ml-64 min-h-screen">{children}</div>
    </div>
  );
}
