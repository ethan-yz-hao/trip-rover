"use client";
import React, { useState, useEffect } from "react";
import { Map, ControlPosition } from "@vis.gl/react-google-maps";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";
import { Box, CircularProgress } from "@mui/material";
import MapContent from "./MapContent";

interface PlaceDetails {
    index: number;
    placeId: string;
    googlePlaceId: string;
    name: string;
    location: google.maps.LatLngLiteral;
    address: string;
}

const MapView: React.FC = () => {
    const { placeDetails, loadingPlaceDetails } = useCanvasContext();
    const [selectedPlace, setSelectedPlace] = useState<PlaceDetails | null>(
        null
    );

    if (loadingPlaceDetails) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                height="100%"
                width="100%"
            >
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box sx={{ height: "100%", width: "100%" }}>
            <Map
                defaultCenter={{
                    lat: 40.7128,
                    lng: -74.006,
                }}
                defaultZoom={12}
                minZoom={3}
                restriction={{
                    latLngBounds: {
                        north: 85,
                        south: -85,
                        east: 180,
                        west: -180,
                    },
                }}
                mapId="trip-rover-map"
                fullscreenControl={false}
                mapTypeControlOptions={{
                    position: ControlPosition.TOP_RIGHT,
                }}
                style={{ width: "100%", height: "100%" }}
            >
                <MapContent
                    placeDetails={placeDetails}
                    selectedPlace={selectedPlace}
                    setSelectedPlace={setSelectedPlace}
                />
            </Map>
        </Box>
    );
};

export default MapView;
