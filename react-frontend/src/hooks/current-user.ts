import { useQuery } from "@tanstack/react-query";
import axios, { isAxiosError } from "axios";

export function useCurrentUser() {
  return useQuery({
    queryKey: ["me"],
    queryFn: async () => {
      try {
        return await axios.get("/api/me");
      } catch (e) {
        if (isAxiosError(e) && e.status === 401) {
          return null;
        } else {
          throw e;
        }
      }
    },
    retry: false,
  });
}
