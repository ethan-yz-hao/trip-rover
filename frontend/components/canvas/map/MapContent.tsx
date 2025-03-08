import React, { useEffect } from "react";
import { Marker, InfoWindow, useMap } from "@vis.gl/react-google-maps";
import { Box, Typography } from "@mui/material";

interface PlaceDetails {
    index: number;
    placeId: string;
    googlePlaceId: string;
    name: string;
    location: google.maps.LatLngLiteral;
    address: string;
}

interface MapContentProps {
    placeDetails: Record<string, PlaceDetails>;
    selectedPlace: PlaceDetails | null;
    setSelectedPlace: (place: PlaceDetails | null) => void;
}

const MapContent: React.FC<MapContentProps> = ({
    placeDetails,
    selectedPlace,
    setSelectedPlace,
}) => {
    const map = useMap();

    // Calculate map center and update it smoothly
    useEffect(() => {
        const places = Object.values(placeDetails);
        if (places.length === 0 || !map) return;

        // Calculate the average of all coordinates
        const sum = places.reduce(
            (acc, place) => ({
                lat: acc.lat + place.location.lat,
                lng: acc.lng + place.location.lng,
            }),
            { lat: 0, lng: 0 }
        );

        const newCenter = {
            lat: sum.lat / places.length,
            lng: sum.lng / places.length,
        };

        // Pan smoothly to new center
        map.panTo(newCenter);
    }, [placeDetails, map]);

    return (
        <>
            {Object.values(placeDetails).map((place) => (
                <Marker
                    key={place.placeId}
                    position={place.location}
                    onClick={() => setSelectedPlace(place)}
                    label={{
                        text: (place.index + 1).toString(),
                        color: "white",
                        fontWeight: "bold",
                    }}
                />
            ))}

            {selectedPlace && (
                <InfoWindow
                    position={selectedPlace.location}
                    onCloseClick={() => setSelectedPlace(null)}
                >
                    <Box sx={{ p: 1, maxWidth: 200 }}>
                        <Typography variant="subtitle1" fontWeight="bold">
                            {selectedPlace.name}
                        </Typography>
                        <Typography variant="body2">
                            {selectedPlace.address}
                        </Typography>
                    </Box>
                </InfoWindow>
            )}
        </>
    );
};

export default MapContent;
