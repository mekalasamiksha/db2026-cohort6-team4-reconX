// TICKET-ADV124 — ThemeProvider: context flips data-theme; CSS owns colours.
import React, { createContext, useContext, useState, useCallback, useEffect, } from 'react';

const ThemeContext = createContext(null);

const STORAGE_KEY = "reconx-theme";

function initialTheme() {
  if (typeof window === "undefined") {
    return "light";
  }

  const stored = localStorage.getItem(STORAGE_KEY);

  if (stored === "light" || stored === "dark") {
    return stored;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}


export function ThemeProvider({ children }) {

   const [theme, setTheme] = useState(initialTheme);

  // TODO(TICKET-ADV124): lazy-init from localStorage('reconx-theme') — fall back
  //                     to 'light' if nothing is stored.
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);
  // TODO(TICKET-ADV124): useEffect that:
  //                     1. sets document.documentElement.dataset.theme = theme
  //                     2. persists `theme` to localStorage on every change.

  const toggle = useCallback(() => {
    setTheme((prev) => (prev === "light" ? "dark" : "light"));
  }, []);
  return (
    <ThemeContext.Provider value={{ theme, toggle, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}



export function useTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error("useTheme must be used within ThemeProvider");
  }

  return context;
}
