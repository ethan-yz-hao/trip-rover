"use client";
import React, { useState, useEffect } from "react";
import { Map, ControlPosition } from "@vis.gl/react-google-maps";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";
import { Box, CircularProgress } from "@mui/material";
import { axiosInstance } from "@/lib/axios";
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
    const { planPlaces, loading } = useCanvasContext();
    const [placeDetails, setPlaceDetails] = useState<
        Record<string, PlaceDetails>
    >({});
    const [selectedPlace, setSelectedPlace] = useState<PlaceDetails | null>(
        null
    );
    const [initialLoading, setInitialLoading] = useState(true);

    // Fetch place details for all places in the plan
    useEffect(() => {
        if (!planPlaces || planPlaces.places.length === 0) {
            setPlaceDetails({});
            setInitialLoading(false);
            return;
        }

        const fetchPlaceDetails = async () => {
            const details: Record<string, PlaceDetails> = {};

            // Process each place in parallel
            await Promise.all(
                planPlaces.places.map(async (place, index) => {
                    try {
                        // Skip if we already have details for this place
                        if (placeDetails[place.placeId]) {
                            details[place.placeId] =
                                placeDetails[place.placeId];
                            details[place.placeId].index = index;
                            return;
                        }

                        console.log(
                            "fetching place details for place",
                            place.placeId,
                            place.googlePlaceId
                        );

                        const response = await axiosInstance.get(
                            `/place/details/${place.googlePlaceId}`
                        );

                        const placeData = response.data.data;
                        const detail = {
                            placeId: place.placeId,
                            googlePlaceId: place.googlePlaceId,
                            name: placeData.displayName.text,
                            location: {
                                lat: placeData.location.latitude,
                                lng: placeData.location.longitude,
                            },
                            address: placeData.formattedAddress,
                            index: index,
                        };
                        details[place.placeId] = detail;
                    } catch (error) {
                        console.error(
                            `Failed to fetch details for place ${place.googlePlaceId}:`,
                            error
                        );
                    }
                })
            );

            setPlaceDetails(details);
            setInitialLoading(false);
        };

        fetchPlaceDetails();
    }, [planPlaces]);

    if (loading || initialLoading) {
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
