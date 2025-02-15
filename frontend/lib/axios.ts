import axios from "axios";
import { store } from "./store";
import { logout } from "./features/auth/authSlice";

const baseURL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export const axiosInstance = axios.create({
    baseURL,
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true, // Important for cookies
});

// Keep track of logout action to prevent multiple redirects
let isLoggingOut = false;

// Add response interceptor for handling errors
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401 && !isLoggingOut) {
            isLoggingOut = true;

            try {
                // Dispatch logout action to clear auth state
                await store.dispatch(logout());

                // If we're not already on the login page, redirect to it
                if (
                    typeof window !== "undefined" &&
                    !window.location.pathname.includes("/login")
                ) {
                    // Store the current path for redirect after login
                    const currentPath = window.location.pathname;
                    if (currentPath !== "/") {
                        window.localStorage.setItem(
                            "redirectAfterLogin",
                            currentPath
                        );
                    }

                    // Redirect to login page
                    window.location.href = "/login";
                }
            } finally {
                isLoggingOut = false;
            }
        }

        return Promise.reject(error);
    }
);
