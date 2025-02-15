import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { axiosInstance } from "@/lib/axios";
import { AxiosError } from "axios";
import { User } from "@/types/user";
import { ResponseResult } from "@/types/model";

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
            await axiosInstance.post("/user/login", credentials);
            const response = await axiosInstance.get<ResponseResult<User>>(
                "/user/profile"
            );
            return response.data.data;
        } catch (err) {
            const error = err as AxiosError<ResponseResult<any>>;
            return rejectWithValue(error.response?.data?.msg || "Login failed");
        }
    }
);

export const logout = createAsyncThunk(
    "auth/logout",
    async (_, { rejectWithValue }) => {
        try {
            await axiosInstance.post("/user/logout");
        } catch (err) {
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
            return response.data.data;
        } catch (err) {
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
            })
            .addCase(login.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // Logout
            .addCase(logout.fulfilled, (state) => {
                state.user = null;
                state.isAuthenticated = false;
            })
            // Check auth status
            .addCase(checkAuthStatus.fulfilled, (state, action) => {
                state.isAuthenticated = true;
                state.user = action.payload;
            })
            .addCase(checkAuthStatus.rejected, (state) => {
                state.isAuthenticated = false;
                state.user = null;
            });
    },
});

export const { clearError } = authSlice.actions;
export default authSlice.reducer;
