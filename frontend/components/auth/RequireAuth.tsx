"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAppSelector } from "@/lib/hooks";

interface RequireAuthProps {
    children: React.ReactNode;
}

const RequireAuth: React.FC<RequireAuthProps> = ({ children }) => {
    const router = useRouter();
    const { isAuthenticated, loading } = useAppSelector((state) => state.auth);

    useEffect(() => {
        if (!loading && !isAuthenticated) {
            // Store the current path for redirect after login
            window.localStorage.setItem(
                "redirectAfterLogin",
                window.location.pathname
            );
            router.push("/login");
        }
    }, [isAuthenticated, loading, router]);

    // Show nothing while checking authentication
    if (loading || !isAuthenticated) {
        return null;
    }

    return <>{children}</>;
};

export default RequireAuth;
