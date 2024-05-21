import {useMap, useMapsLibrary} from "@vis.gl/react-google-maps";
import {useEffect, useState} from "react";

const  useDirectionService = () => {
    const map = useMap();
    const routesLibrary = useMapsLibrary('routes');
    const [directionsService, setDirectionsService] = useState<google.maps.DirectionsService | null>(null);

    useEffect(() => {
        if (!routesLibrary  || !map) return;
        setDirectionsService(new routesLibrary.DirectionsService());
    }, [routesLibrary, map]);

    return directionsService;
}

export default useDirectionService;
