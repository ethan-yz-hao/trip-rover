"use client";

import React from "react";
import { AppBar, Toolbar, Button, Box } from "@mui/material";
import Link from "next/link";
import HomeIcon from "@mui/icons-material/Home";
import LoginIcon from "@mui/icons-material/Login";
import PersonIcon from "@mui/icons-material/Person";
import LogoutIcon from "@mui/icons-material/Logout";
import { useAppDispatch, useAppSelector } from "@/lib/hooks";
import { logout } from "@/lib/features/auth/authSlice";

export default function Navbar() {
    const dispatch = useAppDispatch();
    const { isAuthenticated, user } = useAppSelector((state) => state.auth);

    const handleLogout = () => {
        dispatch(logout());
    };

    return (
        <AppBar position="static">
            <Toolbar>
                <Box sx={{ flexGrow: 1, display: "flex", gap: 2 }}>
                    <Link href="/" passHref style={{ textDecoration: "none" }}>
                        <Button color="inherit" startIcon={<HomeIcon />}>
                            Home
                        </Button>
                    </Link>

                    {!isAuthenticated ? (
                        <Link
                            href="/login"
                            passHref
                            style={{ textDecoration: "none" }}
                        >
                            <Button color="inherit" startIcon={<LoginIcon />}>
                                Login
                            </Button>
                        </Link>
                    ) : (
                        <>
                            <Link
                                href="/profile"
                                passHref
                                style={{ textDecoration: "none" }}
                            >
                                <Button
                                    color="inherit"
                                    startIcon={<PersonIcon />}
                                >
                                    {user?.userName}
                                </Button>
                            </Link>
                            <Button
                                color="inherit"
                                startIcon={<LogoutIcon />}
                                onClick={handleLogout}
                            >
                                Logout
                            </Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
}
