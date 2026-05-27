import { useMutation } from "@tanstack/react-query";
import axios from "axios";

export function useLogout() {
  return useMutation({
    mutationFn: async () => {
      return await axios.post("/logout");
    },
    onSuccess: (data) => {
      if (typeof data.headers.location === "string") {
        window.location.href = data.headers.location;
      }
    },
  });
}
