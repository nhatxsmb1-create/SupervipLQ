import React from "react";

interface NavigationProps {
  activeTab: string;
  onChangeTab: (tab: "LIVE" | "HISTORY" | "STRATEGY" | "SETTINGS") => void;
}

export const Navigation: React.FC<NavigationProps> = ({ activeTab, onChangeTab }) => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 h-16 bg-[#090F1D] border-t border-slate-800 z-50 flex items-center justify-around px-2 text-slate-400">
      <button
        onClick={() => onChangeTab("LIVE")}
        className={`flex flex-col items-center justify-center gap-1 w-16 h-full transition-all cursor-pointer ${
          activeTab === "LIVE" ? "text-cyan-400 font-extrabold scale-105" : "hover:text-white"
        }`}
      >
        <span className="text-[10px] uppercase tracking-wider font-extrabold block text-center">Trợ Lý Live</span>
      </button>

      <button
        onClick={() => onChangeTab("HISTORY")}
        className={`flex flex-col items-center justify-center gap-1 w-16 h-full transition-all cursor-pointer ${
          activeTab === "HISTORY" ? "text-cyan-400 font-extrabold scale-105" : "hover:text-white"
        }`}
      >
        <span className="text-[10px] uppercase tracking-wider font-extrabold block text-center">Lịch Sử Đấu</span>
      </button>

      <button
        onClick={() => onChangeTab("STRATEGY")}
        className={`flex flex-col items-center justify-center gap-1 w-16 h-full transition-all cursor-pointer ${
          activeTab === "STRATEGY" ? "text-cyan-400 font-extrabold scale-105" : "hover:text-white"
        }`}
      >
        <span className="text-[10px] uppercase tracking-wider font-extrabold block text-center">Khắc Chế</span>
      </button>

      <button
        onClick={() => onChangeTab("SETTINGS")}
        className={`flex flex-col items-center justify-center gap-1 w-16 h-full transition-all cursor-pointer ${
          activeTab === "SETTINGS" ? "text-cyan-400 font-extrabold scale-105" : "hover:text-white"
        }`}
      >
        <span className="text-[10px] uppercase tracking-wider font-extrabold block text-center">Cài Đặt</span>
      </button>
    </nav>
  );
};
