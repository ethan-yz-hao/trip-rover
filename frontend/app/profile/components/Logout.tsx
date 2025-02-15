"use client";
import React from "react";
import { useRouter } from "next/navigation";
import { useAppDispatch } from "@/lib/hooks";
import { logout } from "@/lib/features/auth/authSlice";
import log from "@/lib/log";
import { Button } from "@mui/material";
import LogoutIcon from "@mui/icons-material/Logout";

const Logout: React.FC = () => {
    const router = useRouter();
    const dispatch = useAppDispatch();

    const handleLogout = async () => {
        try {
            await dispatch(logout()).unwrap();
            log.log("Logged out successfully");
            router.push("/");
        } catch (error) {
            log.error("Error logging out:", error);
        }
    };

    return (
        <Button
            color="inherit"
            startIcon={<LogoutIcon />}
            onClick={handleLogout}
        >
            Logout
        </Button>
    );
};

export default Logout;
