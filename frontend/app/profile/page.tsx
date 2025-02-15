"use client";
import React from "react";
import Navbar from "@/components/navbar/Navbar";
import PlanList from "./components/PlanList";
import RequireAuth from "@/components/auth/RequireAuth";
import Logout from "./components/Logout";

export default function ProfilePage() {
    return (
        <RequireAuth>
            <Navbar />
            <PlanList />
            <Logout />
        </RequireAuth>
    );
}
