import { useCurrentUser } from "../hooks/current-user";
import { useLogout } from "../hooks/logout";
import { Button } from "./ui/button";
import { LoaderCircle } from "lucide-react";

function HasAuthenticatedStatus({
  isRefreshPending,
  onRefresh,
}: {
  isRefreshPending: boolean;
  onRefresh: () => void;
}) {
  const { mutate, status } = useLogout();
  return (
    <div>
      <p className="mb-4">Congratulations! You are signed in.</p>
      <div className="space-x-4">
        <Button
          type="button"
          variant="destructive"
          disabled={status === "pending"}
          onClick={() => {
            mutate();
          }}
        >
          Logout
        </Button>
        <Button
          type="button"
          variant="outline"
          disabled={isRefreshPending}
          onClick={onRefresh}
        >
          Refresh
        </Button>
      </div>
    </div>
  );
}

function HasUnauthenticatedStatus() {
  return (
    <div>
      <p className="mb-4">You are not signed in.</p>
      <Button asChild>
        <a href="/oauth2/authorization/keycloak">Sign In</a>
      </Button>
    </div>
  );
}

export function AuthenticatedStatus() {
  const { status, data, error, refetch, isPending } = useCurrentUser();

  return (
    <section>
      {status === "pending" ? (
        <LoaderCircle className="animate-spin" />
      ) : (
        <>
          {status === "error" ? (
            <p>Error error occured: {error.message}</p>
          ) : (
            <>
              {data === null ? (
                <HasUnauthenticatedStatus />
              ) : (
                <HasAuthenticatedStatus
                  isRefreshPending={isPending}
                  onRefresh={refetch}
                />
              )}
            </>
          )}
        </>
      )}
    </section>
  );
}
