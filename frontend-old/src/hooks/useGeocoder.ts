import {useMap, useMapsLibrary} from "@vis.gl/react-google-maps";
import {useEffect, useState} from "react";

const useGeocoder = () => {
    const map = useMap(); // Use the map instance
    const geocodingLibrary = useMapsLibrary('geocoding');
    const [geocoder, setGeocoder] = useState<google.maps.Geocoder | null>(null);

    useEffect(() => {
        if (geocodingLibrary && map) {
            // Initialize the geocoder object
            setGeocoder(new geocodingLibrary.Geocoder());
        }
    }, [geocodingLibrary, map]);

    return geocoder;

}

export default useGeocoder;