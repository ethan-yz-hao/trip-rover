import { createSlice, PayloadAction } from '@reduxjs/toolkit'
import type { RootState } from '../../store'

interface ArrivalTimeArrayState {
    value: string[];
}

// Define the initial state
const initialState: ArrivalTimeArrayState = {
    value: []
};

// Create the slice
const arrivalTimeArraySlice = createSlice({
    name: 'arrivalTimeArray',
    initialState,
    reducers: {
        setArrivalTimeArray: (state, action: PayloadAction<string[]>) => {
            state.value = action.payload;
        },
        addArrivalTime: (state, action: PayloadAction<string>) => {
            state.value.push(action.payload);
        },
        clearArrivalTimeArray: (state) => {
            state.value = [];
        }
    }
});

// Export the actions
export const { setArrivalTimeArray, addArrivalTime, clearArrivalTimeArray } = arrivalTimeArraySlice.actions;

export const selectArrivalTimeArray = (state: RootState) => state.arrivalTimeArray.value;

// Other code such as selectors can use the imported `RootState` type
export default arrivalTimeArraySlice.reducer
