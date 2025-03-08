"use client";
import React, { useState, useEffect, useMemo } from "react";
import { Map, Marker, InfoWindow } from "@vis.gl/react-google-maps";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";
import { Box, Typography, CircularProgress } from "@mui/material";
import { axiosInstance } from "@/lib/axios";

interface PlaceDetails {
    placeId: string;
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
    const [mapLoading, setMapLoading] = useState(true);

    // Fetch place details for all places in the plan
    useEffect(() => {
        if (!planPlaces || planPlaces.places.length === 0) {
            setMapLoading(false);
            return;
        }

        const fetchPlaceDetails = async () => {
            setMapLoading(true);
            const details: Record<string, PlaceDetails> = {};

            // Process each place in parallel
            await Promise.all(
                planPlaces.places.map(async (place) => {
                    try {
                        // Skip if we already have details for this place
                        if (details[place.placeId]) return;

                        const response = await axiosInstance.get(
                            `/place/details/${place.googlePlaceId}`
                        );

                        const placeData = response.data.data;
                        details[place.placeId] = {
                            placeId: place.placeId,
                            name: placeData.name,
                            location: {
                                lat: placeData.geometry.location.lat,
                                lng: placeData.geometry.location.lng,
                            },
                            address:
                                placeData.formatted_address ||
                                placeData.vicinity ||
                                "",
                        };
                    } catch (error) {
                        console.error(
                            `Failed to fetch details for place ${place.googlePlaceId}:`,
                            error
                        );
                    }
                })
            );

            setPlaceDetails(details);
            setMapLoading(false);
        };

        fetchPlaceDetails();
    }, [planPlaces]);

    // Calculate map center and bounds
    const mapCenter = useMemo(() => {
        const places = Object.values(placeDetails);
        if (places.length === 0) {
            // Default to a central location if no places
            return { lat: 40.7128, lng: -74.006 }; // New York City
        }

        // Calculate the average of all coordinates
        const sum = places.reduce(
            (acc, place) => ({
                lat: acc.lat + place.location.lat,
                lng: acc.lng + place.location.lng,
            }),
            { lat: 0, lng: 0 }
        );

        return {
            lat: sum.lat / places.length,
            lng: sum.lng / places.length,
        };
    }, [placeDetails]);

    if (loading || mapLoading) {
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
                defaultCenter={mapCenter}
                defaultZoom={12}
                mapId="trip-planner-map"
                fullscreenControl={false}
                style={{ width: "100%", height: "100%" }}
            >
                {Object.values(placeDetails).map((place, index) => (
                    <Marker
                        key={place.placeId}
                        position={place.location}
                        onClick={() => setSelectedPlace(place)}
                        label={{
                            text: (index + 1).toString(),
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
            </Map>
        </Box>
    );
};

export default MapView;
