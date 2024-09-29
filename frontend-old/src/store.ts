import { configureStore } from '@reduxjs/toolkit'
import PlacesArrayReducer from './features/placesArray/placesArraySlice'
import startTimeReducer from './features/startTime/startTimeSlice'
import arrivalTimeArrayReducer from './features/arrivalTimeArray/arrivalTimeArraySlice';


export const store = configureStore({
    reducer: {
        placesArray: PlacesArrayReducer,
        startTime: startTimeReducer,
        arrivalTimeArray: arrivalTimeArrayReducer
    }
})

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch