import {useState} from 'react';
import {modifyPlace, selectPlacesArray} from "@/features/placesArray/placesArraySlice.ts";
import {useAppDispatch, useAppSelector} from "@/hooks/reduxHooks.ts";
import {Input} from "@chakra-ui/react";


function CustomTimePicker({placeId}: { placeId: string }) {
    const placesArray = useAppSelector(selectPlacesArray);
    const dispatch = useAppDispatch();

    const place = placesArray.find(place => place.id === placeId);
    const defaultTime = place ? new Date(place.stayTime * 1000).toISOString().substr(11, 5) : '01:00';

    const [time, setTime] = useState<string | null>(defaultTime);

    const handleTimeChange = (newTime: string | null) => {
        setTime(newTime);
        if (!newTime) return;

        const [hours, minutes] = newTime.split(':').map(Number);
        const newStayTime = hours * 3600 + minutes * 60;

        const updatedPlace = placesArray.find(place => place.id === placeId);
        if (!updatedPlace) return;

        dispatch(modifyPlace({...updatedPlace, stayTime: newStayTime}));
    };

    return (
        <Input type="time"
               size="md"
               width="110px"
               value={time ? time : '00:00'}
               onChange={(e) => handleTimeChange(e.target.value)}
               backgroundColor="white"
        />
    );
}

export default CustomTimePicker;
