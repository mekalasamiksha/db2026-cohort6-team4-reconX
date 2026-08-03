// TICKET-ADV125 — Jest-DOM matchers for React Testing Library
import '@testing-library/jest-dom/vitest';

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),          // deprecated
    removeListener: vi.fn(),       // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});