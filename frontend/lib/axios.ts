import axios, { AxiosError } from "axios";
import { store } from "./store";
import { logout } from "./features/auth/authSlice";
import { ResponseResult } from "@/types/model";
import log from "@/lib/log";

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

// Custom error class for application-specific errors
export class AppError extends Error {
    constructor(
        public message: string,
        public code?: number,
        public originalError?: any
    ) {
        super(message);
        this.name = "AppError";
    }
}

// Add response interceptor for handling errors
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ResponseResult<any>>) => {
        const responseData = error.response?.data;

        // Handle 401 Unauthorized
        if (error.response?.status === 401 && !isLoggingOut) {
            isLoggingOut = true;
            try {
                await store.dispatch(logout());
                // Redirect to login page if not already on login page
                if (
                    typeof window !== "undefined" &&
                    !window.location.pathname.includes("/login")
                ) {
                    window.localStorage.setItem(
                        "redirectAfterLogin",
                        window.location.pathname
                    );
                    window.location.href = "/login";
                }
            } finally {
                isLoggingOut = false;
            }
            return Promise.reject(new AppError("Session expired", 401, error));
        }

        // Handle other common errors
        if (responseData) {
            // Use custom error message from backend if available
            return Promise.reject(
                new AppError(responseData.msg, responseData.code, error)
            );
        }

        // Handle network errors
        if (!error.response) {
            log.error("Network error:", error);
            return Promise.reject(
                new AppError(
                    "Network error. Please check your connection.",
                    0,
                    error
                )
            );
        }

        // Handle other errors
        return Promise.reject(
            new AppError(
                "An unexpected error occurred",
                error.response.status,
                error
            )
        );
    }
);
