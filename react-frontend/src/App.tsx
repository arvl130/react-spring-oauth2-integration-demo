import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthenticatedStatus } from "./components/authenticated-status";

const queryClient = new QueryClient();

function App() {
  return (
    <main className="min-h-svh flex justify-center items-center">
      <QueryClientProvider client={queryClient}>
        <AuthenticatedStatus />
      </QueryClientProvider>
    </main>
  );
}

export default App;
