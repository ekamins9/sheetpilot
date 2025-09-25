# SheetPilot Frontend

Modern, responsive frontend for the SheetPilot spreadsheet automation platform built with SvelteKit and TailwindCSS.

## Tech Stack

- **SvelteKit** - Full-stack framework for Svelte
- **TypeScript** - Type-safe development
- **TailwindCSS** - Utility-first CSS framework
- **Vite** - Fast development and build tool

## Project Structure

```
frontend/
├── src/
│   ├── lib/
│   │   ├── components/    # Reusable Svelte components
│   │   ├── stores/        # Svelte stores for state management
│   │   └── api/          # API client and utilities
│   ├── routes/           # SvelteKit file-based routing
│   └── app.css          # Global styles and Tailwind imports
├── static/              # Static assets
└── vercel.json          # Vercel deployment config
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm

### Installation

\`\`\`bash
# Install dependencies
npm install

# Copy environment variables
cp .env.example .env
\`\`\`

### Environment Variables

Create a \`.env\` file in the frontend directory:

\`\`\`
PUBLIC_API_URL=http://localhost:8080
\`\`\`

### Development

\`\`\`bash
# Start development server (default: http://localhost:5173)
npm run dev

# Start with custom host/port
npm run dev -- --host --port 3000
\`\`\`

### Building

\`\`\`bash
# Build for production
npm run build

# Preview production build
npm run preview
\`\`\`

## Development Guidelines

- Use TypeScript for all new files
- Follow Svelte component best practices
- Use TailwindCSS utility classes for styling
- Keep components small and reusable
- Use stores for shared state
- Use the api client in \`lib/api/client.ts\` for all API calls

## Deployment

### Vercel

The project is configured for Vercel deployment with \`vercel.json\`. Simply connect your repository to Vercel and it will automatically deploy.

Environment variables to set in Vercel:
- \`PUBLIC_API_URL\` - Your backend API URL

## Color Scheme

The project uses a modern blue and purple color scheme:

- **Primary (Blue)**: Used for main actions and branding
- **Secondary (Purple)**: Used for accents and highlights

Both colors have full shade ranges (50-950) available via Tailwind classes:
- \`bg-primary-600\`, \`text-primary-700\`, etc.
- \`bg-secondary-500\`, \`text-secondary-600\`, etc.
