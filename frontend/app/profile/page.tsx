import Navbar from "@/components/navbar/Navbar";
import React from "react";
import PlanList from "./components/PlanList";
import Logout from "./components/Logout";

export default function ProfilePage() {
    return (
        <>
            <Navbar />
            <PlanList />
            <Logout />
        </>
    );
}
