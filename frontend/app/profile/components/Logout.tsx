"use client";
import React from "react";
import log from "@/utils/log";

const handleLogout = async () => {
    try {
        const response = await fetch("http://localhost:8080/api/user/logout", {
            method: "POST",
            credentials: "include",
        });

        if (!response.ok) {
            throw new Error(`Logout failed with status: ${response.status}`);
        }

        log.log("Logged out successfully");
    } catch (error) {
        log.error("Error logging out:", error);
    }
};

const Logout = () => {
    return <button onClick={handleLogout}>Logout</button>;
};

export default Logout;
