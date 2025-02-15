"use client";

import React from "react";
import { useRouter } from "next/navigation";
import { useAppSelector } from "@/lib/hooks";
import { useEffect } from "react";
import Navbar from "@/components/navbar/Navbar";
import LoginForm from "./components/LoginForm";

export default function LoginPage() {
    const router = useRouter();
    const { isAuthenticated } = useAppSelector((state) => state.auth);

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            router.push("/");
        }
    }, [isAuthenticated, router]);

    return (
        <>
            <Navbar />
            <LoginForm />
        </>
    );
}
