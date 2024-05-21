import {createSlice, PayloadAction} from '@reduxjs/toolkit'
import type {RootState} from '../../store'

export interface PlacesArrayItem {
    id: string;
    place_id: string;
    name: string;
    lat: number;
    lng: number;
    stayTime: number;
}

export interface PlacesArrayState {
    value: Array<PlacesArrayItem>
}

const initialState: PlacesArrayState = {
    value: []
}

export const placesArraySlice = createSlice({
    name: 'placesArray',
    initialState,
    reducers: {
        setPlacesArray: (state, action: PayloadAction<Array<PlacesArrayItem>>) => {
            state.value = action.payload
        },
        addPlace: (state, action: PayloadAction<PlacesArrayItem>) => {
            state.value.push(action.payload);
        },
        modifyPlace: (state, action: PayloadAction<PlacesArrayItem>) => {
            state.value = state.value.map(place => {
                if (place.id === action.payload.id) {
                    return action.payload;
                }
                return place;
            });
        }
    }
})

export const {setPlacesArray, addPlace, modifyPlace} = placesArraySlice.actions

export const selectPlacesArray = (state: RootState) => state.placesArray.value

export default placesArraySlice.reducer