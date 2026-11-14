/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        bg: {
          main: "#0B0D10",
          secondary: "#11151A",
          elevated: "#171C23",
        },
        surface: {
          card: "#151A21",
          hover: "#1D242D",
          border: "#29313B",
        },
        text: {
          primary: "#E8EDF3",
          secondary: "#9BA7B5",
          muted: "#687586",
        },
        brand: {
          DEFAULT: "#A3E635", // Electric green
          hover: "#84CC16",
          subtle: "rgba(163, 230, 53, 0.12)",
        },
        cyan: {
          DEFAULT: "#38BDF8",
          subtle: "rgba(56, 189, 248, 0.12)",
        },
        warning: {
          DEFAULT: "#FBBF24",
          subtle: "rgba(251, 191, 36, 0.12)",
        },
        danger: {
          DEFAULT: "#F87171",
          subtle: "rgba(248, 113, 113, 0.12)",
        },
        success: {
          DEFAULT: "#4ADE80",
          subtle: "rgba(74, 222, 128, 0.12)",
        },
      },
      fontFamily: {
        sans: ["var(--font-inter)", "system-ui", "sans-serif"],
        mono: ["var(--font-mono)", "ui-monospace", "monospace"],
      },
      borderRadius: {
        sm: "4px",
        DEFAULT: "6px",
        md: "8px",
        lg: "10px",
        xl: "12px",
      },
      boxShadow: {
        subtle: "0 1px 2px 0 rgba(0, 0, 0, 0.4)",
        border: "inset 0 0 0 1px #29313B",
      },
    },
  },
  plugins: [],
};
