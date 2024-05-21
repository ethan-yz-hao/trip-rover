import React, {useEffect, useRef, useState} from "react";
import {ControlPosition, Map, useMap, useMapsLibrary} from '@vis.gl/react-google-maps';
import usePlacesService from "@/hooks/usePlacesService.ts";
import CustomMarker from "./CustomMarker.tsx";
import PlaceInfoCard from "./PlaceInfoCard.tsx";
import PlaceSideBar from "./PlaceSideBar.tsx";
import {Box, Button, HStack, Input} from "@chakra-ui/react";
import useResizeObserver from "@/hooks/useResizeObserver.ts";
import useAutoComplete from "@/hooks/useAutoComplete.ts";
import {selectPlacesArray, setPlacesArray} from "@/features/placesArray/placesArraySlice.ts";
import {useAppDispatch, useAppSelector} from '@/hooks/reduxHooks';
import {v4 as uuidv4} from "uuid";
import useDirectionService from "@/hooks/useDirectionService.ts";
import useDirectionsRenderers from "@/hooks/useDirectionsRenderers.ts";
import {selectStartTime} from "@/features/startTime/startTimeSlice.ts";
import {
    addArrivalTime,
    selectArrivalTimeArray,
    setArrivalTimeArray
} from "@/features/arrivalTimeArray/arrivalTimeArraySlice.ts";


const Canvas = () => {
    const map = useMap();

    const dispatch = useAppDispatch();
    const placesArray = useAppSelector(selectPlacesArray);

    const placesService = usePlacesService();
    const coreLibrary = useMapsLibrary('core');

    const hoverTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const [searchText, setSearchText] = useState('');
    const [places, setPlaces] = useState<Array<google.maps.places.PlaceResult>>([]);
    const [hoveredPlace, setHoveredPlace] = useState<google.maps.places.PlaceResult | null>(null);
    const [selectedPlace, setSelectedPlace] = useState<google.maps.places.PlaceResult | null>(null);
    const [placeSideBarElement, setPlaceSideBarElement] = useState<HTMLDivElement | null>(null);
    const dimensions = useResizeObserver(placeSideBarElement);
    const searchInputRef = useRef<HTMLInputElement>(null);
    const placeAutocomplete = useAutoComplete({
        inputRef: searchInputRef,
        options: {
            fields: ['geometry', 'name', 'formatted_address'],
        },
    });

    const directionsService = useDirectionService();
    const {createDirectionsRenderer, clearDirectionsRenderers} = useDirectionsRenderers();

    const startTime = useAppSelector(selectStartTime);
    const arrivalTimeArray = useAppSelector(selectArrivalTimeArray);


    const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!placesService) return; // Ensure the places service is initialized
        const request = {
            query: searchText,
        };
        placesService.textSearch(request, (results, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && results) {
                setPlaces(results);
                setSelectedPlace(results[0])
            } else {
                console.error("Places search was not successful for the following reason:", status);
            }
        });
    };

    const handleMouseEnter = (place: google.maps.places.PlaceResult) => {
        // Clear any existing timeout to prevent duplicate calls
        if (hoverTimeoutRef.current) {
            clearTimeout(hoverTimeoutRef.current);
        }

        // Set a new timeout
        hoverTimeoutRef.current = setTimeout(() => {
            setHoveredPlace(place);
        }, 1000); // 1000 milliseconds = 1 second
    };

    const handleMouseLeave = () => {
        // If the mouse leaves before 1 second, clear the timeout
        if (hoverTimeoutRef.current) {
            clearTimeout(hoverTimeoutRef.current);
            hoverTimeoutRef.current = null; // Reset the ref
        }

        // Optionally, immediately remove any hovered place upon mouse leave
        setHoveredPlace(null);
    };

    const handleMarkerClick = (place: google.maps.places.PlaceResult) => {
        setSelectedPlace(place);
    };

    const handleAddPlace = () => {
        const newPlace = {
            id: uuidv4(),
            name: selectedPlace?.name || '',
            place_id: selectedPlace?.place_id || '',
            lat: selectedPlace?.geometry?.location?.lat() || 0,
            lng: selectedPlace?.geometry?.location?.lng() || 0,
            stayTime: 0,
        }

        if (selectedPlace && newPlace.place_id) {
            dispatch(setPlacesArray([...placesArray, newPlace]));
        }
    }

    const handleClearPlaces = () => {
        setPlaces([]);
        setSelectedPlace(null);
        setSearchText('');
    }

    // Update the search text when a marker is clicked
    useEffect(() => {
        setSearchText(selectedPlace?.name || searchText)
    }, [selectedPlace]);

    // handle auto complete
    useEffect(() => {
        if (!placeAutocomplete) return;

        placeAutocomplete.addListener('place_changed', () => {
            setSelectedPlace(placeAutocomplete.getPlace());
        });
    }, [setSelectedPlace, placeAutocomplete]);

    // handle directions
    useEffect(() => {
        clearDirectionsRenderers();
        if (!directionsService) return;
        if (placesArray.length < 2) return;

        let departureTime = new Date(startTime);
        // round the departure time to present if the time is in the past
        if (departureTime < new Date()) {
            departureTime = new Date();
        }
        dispatch(setArrivalTimeArray([departureTime.toISOString()]));
        placesArray.forEach((place, index) => {
            if (index < placesArray.length - 1) {
                const renderer = createDirectionsRenderer();
                if (!renderer) return;

                const request = {
                    origin: {placeId: place.place_id},
                    destination: {placeId: placesArray[index + 1].place_id},
                    travelMode: google.maps.TravelMode.DRIVING,
                    drivingOptions: {
                        departureTime: departureTime,
                    },
                };

                directionsService.route(request, (result, status) => {
                    if (status === google.maps.DirectionsStatus.OK && result) {
                        renderer.setDirections(result);
                        if (result.routes[0] && result.routes[0].legs[0].duration) {
                            const travelTime = result.routes[0].legs[0].duration.value;
                            departureTime = new Date(departureTime.getTime() + (travelTime + place.stayTime) * 1000);
                            dispatch(addArrivalTime(departureTime.toISOString()));
                        }
                    } else {
                        console.error("Directions request failed due to " + status);
                    }
                });
            }
        });
        console.log('arrivalTimeArray', arrivalTimeArray);
    }, [placesArray, directionsService, startTime]);

    // handle zoom, bounds based on search result and routes
    useEffect(() => {
        if (!map || !coreLibrary) return;
        if (!places.length && !placesArray.length) return;

        const extendBounds = [];
        for (const place of places) {
            if (!place.geometry?.location) continue;
            extendBounds.push(place.geometry.location);
        }
        for (const place of placesArray) {
            extendBounds.push(new coreLibrary.LatLng(place.lat, place.lng));
        }
        if (!extendBounds.length) return;

        const bounds = new coreLibrary.LatLngBounds();
        extendBounds.forEach((location) => {
            bounds.extend(location);
        });

        // account for the width of the place sidebar
        const convertPxToLongitude = (pxDistance: number | undefined) => {
            if (!pxDistance) return 0;
            var center = map.getCenter();
            var zoom = map.getZoom();
            if (!center || !zoom) return 0;
            var latitude = center.lat();
            var latRadians = latitude * (Math.PI / 180);
            var degPerPixel = Math.cos(latRadians) * 360 / (256 * Math.pow(2, zoom));
            return degPerPixel * pxDistance;
        }
        const sw = bounds.getSouthWest();
        const newSw = new google.maps.LatLng(sw.lat(), sw.lng() - convertPxToLongitude(dimensions?.width));
        bounds.extend(newSw);

        map.fitBounds(bounds);

    }, [places, placesArray, map, dimensions]);

    return (
        <Box height="100%" position="relative" overflow="hidden">
            <Box position="absolute" zIndex={4} w="25%" pl={2} pr={6} mt={2}>
                <form onSubmit={handleSearch}>
                    <HStack>
                        <Input
                            type="text"
                            backgroundColor="white"
                            placeholder="Search places..."
                            value={searchText}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchText(e.target.value)}
                            ref={searchInputRef}
                        />
                        <Button type="submit">Search</Button>
                    </HStack>
                </form>
            </Box>
            <Button
                position="absolute"
                zIndex={4}
                left="25%"
                m={2}
                onClick={handleAddPlace}>Add Place
            </Button>
            <Button
                position="absolute"
                zIndex={4}
                left="35%"
                m={2}
                onClick={handleClearPlaces}>X
            </Button>
            {hoveredPlace && (
                <PlaceInfoCard place={hoveredPlace} placeSideBarWidth={dimensions ? dimensions.width : 0}/>
            )}
            {selectedPlace?.place_id && (
                <PlaceSideBar placeId={selectedPlace.place_id} ref={el => setPlaceSideBarElement(el)}/>
            )}
            <Map
                mapId="map"
                defaultCenter={{lat: 37.0902, lng: -95.7129}}
                defaultZoom={5}
                gestureHandling={'greedy'}
                minZoom={3}
                restriction={{latLngBounds: {north: 85, south: -85, east: 180, west: -180}}}
                mapTypeControlOptions={{
                    position: ControlPosition.TOP_RIGHT,
                    }}
                streetViewControl={false}
            >
                {places.map((place) => {
                    if (!place.geometry?.location) return null;
                    return <CustomMarker key={place.place_id}
                                         place={place}
                                         handleMouseEnter={handleMouseEnter}
                                         handleMouseLeave={handleMouseLeave}
                                         handleMarkerClick={handleMarkerClick}/>;
                })}
            </Map>
        </Box>
    );
};

export default Canvas;
