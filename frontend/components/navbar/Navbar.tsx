import React from "react";
import { AppBar, Toolbar, Button, Box } from "@mui/material";
import Link from "next/link";
import HomeIcon from "@mui/icons-material/Home";
import LoginIcon from "@mui/icons-material/Login";
import PersonIcon from "@mui/icons-material/Person";

export default function Navbar() {
    return (
        <AppBar position="static">
            <Toolbar>
                <Box sx={{ flexGrow: 1, display: "flex", gap: 2 }}>
                    <Link href="/" passHref style={{ textDecoration: "none" }}>
                        <Button color="inherit" startIcon={<HomeIcon />}>
                            Home
                        </Button>
                    </Link>

                    <Link
                        href="/login"
                        passHref
                        style={{ textDecoration: "none" }}
                    >
                        <Button color="inherit" startIcon={<LoginIcon />}>
                            Login
                        </Button>
                    </Link>

                    <Link
                        href="/profile"
                        passHref
                        style={{ textDecoration: "none" }}
                    >
                        <Button color="inherit" startIcon={<PersonIcon />}>
                            Profile
                        </Button>
                    </Link>
                </Box>
            </Toolbar>
        </AppBar>
    );
}
