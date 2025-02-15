"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAppDispatch, useAppSelector } from "@/lib/hooks";
import { login } from "@/lib/features/auth/authSlice";
import log from "@/lib/log";

const LoginForm: React.FC = () => {
    const router = useRouter();
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector((state) => state.auth);

    // State to hold form data
    const [userName, setUserName] = useState<string>("");
    const [password, setPassword] = useState<string>("");

    // Handle form submission
    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        try {
            const resultAction = await dispatch(
                login({ userName, password })
            ).unwrap();
            log.log("Login success:", resultAction);

            // Handle successful login redirect
            const redirectPath =
                window.localStorage.getItem("redirectAfterLogin");
            window.localStorage.removeItem("redirectAfterLogin");
            router.push(redirectPath || "/");
        } catch (error) {
            log.error("Login error:", error);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input
                        type="text"
                        id="username"
                        value={userName}
                        onChange={(e) => setUserName(e.target.value)}
                        required
                        disabled={loading}
                    />
                </div>
                <div>
                    <label htmlFor="password">Password:</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        disabled={loading}
                    />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>
            </form>
            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
};

export default LoginForm;
