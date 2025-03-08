"use client";
import React, { useState, useEffect, useRef } from "react";
import { Map, Marker, InfoWindow, useMap } from "@vis.gl/react-google-maps";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";
import { Box, Typography, CircularProgress } from "@mui/material";
import { axiosInstance } from "@/lib/axios";

interface PlaceDetails {
    placeId: string;
    name: string;
    location: google.maps.LatLngLiteral;
    address: string;
}

const MapContent: React.FC<{
    placeDetails: Record<string, PlaceDetails>;
    selectedPlace: PlaceDetails | null;
    setSelectedPlace: (place: PlaceDetails | null) => void;
}> = ({ placeDetails, selectedPlace, setSelectedPlace }) => {
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
        </>
    );
};

const MapView: React.FC = () => {
    const { planPlaces, loading } = useCanvasContext();
    const [placeDetails, setPlaceDetails] = useState<
        Record<string, PlaceDetails>
    >({});
    const [selectedPlace, setSelectedPlace] = useState<PlaceDetails | null>(
        null
    );
    const [initialLoading, setInitialLoading] = useState(true);
    const [mapCenter, setMapCenter] = useState<google.maps.LatLngLiteral>({
        lat: 40.7128,
        lng: -74.006,
    });
    const [zoom, setZoom] = useState(12);
    const mapRef = useRef<google.maps.Map | null>(null);

    // Fetch place details for all places in the plan
    useEffect(() => {
        if (!planPlaces || planPlaces.places.length === 0) {
            setInitialLoading(false);
            return;
        }

        const fetchPlaceDetails = async () => {
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
                        const detail = {
                            placeId: place.placeId,
                            name: placeData.displayName.text,
                            location: {
                                lat: placeData.location.latitude,
                                lng: placeData.location.longitude,
                            },
                            address: placeData.formattedAddress,
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

    // Calculate map center and update it smoothly
    useEffect(() => {
        const places = Object.values(placeDetails);
        if (places.length === 0) return;

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

        // Update center state
        setMapCenter(newCenter);

        // If we have a map reference, pan smoothly
        if (mapRef.current) {
            mapRef.current.panTo(newCenter);
        }
    }, [placeDetails]);

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
                center={mapCenter}
                zoom={zoom}
                onZoomChanged={(e) => setZoom(e.detail.zoom)}
                mapId="trip-planner-map"
                fullscreenControl={false}
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
