import { createSlice } from '@reduxjs/toolkit';
import type { RootState } from '../../store'

export interface StartTimeState {
    value: string;
}

const getCurrentISOTime = () => new Date().toISOString();

export const startTimeSlice = createSlice({
    name: 'startTime',
    initialState: {
        value: getCurrentISOTime(),
    },
    reducers: {
        setStartTime: (state, action) => {
            state.value = action.payload;
        },
    },
});

export const { setStartTime } = startTimeSlice.actions;

export const selectStartTime = (state: RootState) => state.startTime.value;

export default startTimeSlice.reducer;
