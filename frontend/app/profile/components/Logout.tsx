"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAppDispatch } from "@/lib/hooks";
import { logout } from "@/lib/features/auth/authSlice";
import { Button, CircularProgress, Snackbar, Alert } from "@mui/material";
import LogoutIcon from "@mui/icons-material/Logout";
import log from "@/lib/log";

const Logout = () => {
    const router = useRouter();
    const dispatch = useAppDispatch();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogout = async () => {
        try {
            setLoading(true);
            setError(null);
            await dispatch(logout()).unwrap();
            log.log("Logged out successfully");
            router.push("/");
        } catch (error) {
            log.error("Error logging out:", error);
            setError(typeof error === "string" ? error : "Failed to logout");
        } finally {
            setLoading(false);
        }
    };

    const handleCloseError = () => {
        setError(null);
    };

    return (
        <>
            <Button
                color="inherit"
                startIcon={
                    loading ? (
                        <CircularProgress size={20} color="inherit" />
                    ) : (
                        <LogoutIcon />
                    )
                }
                onClick={handleLogout}
                disabled={loading}
            >
                {loading ? "Logging out..." : "Logout"}
            </Button>

            <Snackbar
                open={!!error}
                autoHideDuration={6000}
                onClose={handleCloseError}
                anchorOrigin={{ vertical: "top", horizontal: "center" }}
            >
                <Alert
                    onClose={handleCloseError}
                    severity="error"
                    variant="filled"
                    sx={{ width: "100%" }}
                >
                    {error}
                </Alert>
            </Snackbar>
        </>
    );
};

export default Logout;
