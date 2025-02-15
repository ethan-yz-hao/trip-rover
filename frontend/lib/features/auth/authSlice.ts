import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { axiosInstance, AppError } from "@/lib/axios";
import { AxiosError } from "axios";
import { User } from "@/types/user";
import { ResponseResult } from "@/types/model";
import log from "@/lib/log";

interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    loading: boolean;
    error: string | null;
}

const initialState: AuthState = {
    user: null,
    isAuthenticated: false,
    loading: false,
    error: null,
};

// Async thunks
export const login = createAsyncThunk(
    "auth/login",
    async (
        credentials: { userName: string; password: string },
        { rejectWithValue }
    ) => {
        try {
            // Login request
            await axiosInstance.post<ResponseResult<void>>(
                "/user/login",
                credentials
            );

            // Get user profile after successful login
            const response = await axiosInstance.get<ResponseResult<User>>(
                "/user/profile"
            );

            log.log("Login successful");
            return response.data.data;
        } catch (err) {
            log.error("Login error:", err);

            if (err instanceof AppError) {
                return rejectWithValue(err.message);
            }

            const error = err as AxiosError<ResponseResult<any>>;
            return rejectWithValue(error.response?.data?.msg || "Login failed");
        }
    }
);

export const logout = createAsyncThunk(
    "auth/logout",
    async (_, { rejectWithValue }) => {
        try {
            await axiosInstance.post<ResponseResult<void>>("/user/logout");
            log.log("Logout successful");
        } catch (err) {
            log.error("Logout error:", err);

            if (err instanceof AppError) {
                return rejectWithValue(err.message);
            }

            const error = err as AxiosError<ResponseResult<any>>;
            return rejectWithValue(
                error.response?.data?.msg || "Logout failed"
            );
        }
    }
);

export const checkAuthStatus = createAsyncThunk(
    "auth/checkStatus",
    async (_, { rejectWithValue }) => {
        try {
            const response = await axiosInstance.get<ResponseResult<User>>(
                "/user/profile"
            );
            log.log("Auth check successful");
            return response.data.data;
        } catch (err) {
            log.error("Auth check error:", err);

            if (err instanceof AppError) {
                return rejectWithValue(err.message);
            }

            const error = err as AxiosError<ResponseResult<any>>;
            return rejectWithValue(
                error.response?.data?.msg || "Authentication check failed"
            );
        }
    }
);

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        clearError: (state) => {
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            // Login
            .addCase(login.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(login.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.user = action.payload;
                state.error = null;
            })
            .addCase(login.rejected, (state, action) => {
                state.loading = false;
                state.isAuthenticated = false;
                state.user = null;
                state.error = action.payload as string;
            })
            // Logout
            .addCase(logout.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(logout.fulfilled, (state) => {
                state.loading = false;
                state.user = null;
                state.isAuthenticated = false;
                state.error = null;
            })
            .addCase(logout.rejected, (state, action) => {
                state.loading = false;
                // Even if logout fails on the server, we clear the local state
                state.user = null;
                state.isAuthenticated = false;
                state.error = action.payload as string;
            })
            // Check auth status
            .addCase(checkAuthStatus.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(checkAuthStatus.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                state.user = action.payload;
                state.error = null;
            })
            .addCase(checkAuthStatus.rejected, (state, action) => {
                state.loading = false;
                state.isAuthenticated = false;
                state.user = null;
                state.error = action.payload as string;
            });
    },
});

export const { clearError } = authSlice.actions;
export default authSlice.reducer;
