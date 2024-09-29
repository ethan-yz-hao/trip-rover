import usePlacesService from "@/hooks/usePlacesService.ts";
import {useEffect, useState} from "react";
import {PlaceOverview} from '@googlemaps/extended-component-library/react';
import {Box, Card, CardBody, Image} from "@chakra-ui/react";
import {useMap} from "@vis.gl/react-google-maps";

interface Props {
    place: google.maps.places.PlaceResult;
    placeSideBarWidth: number;
}

const PlaceInfoCard = ({place, placeSideBarWidth}: Props) => {
    const map = useMap();
    const placesService = usePlacesService();
    const [placeDetails, setPlaceDetails] = useState<google.maps.places.PlaceResult | null>(null);
    const [cardPosition, setCardPosition] = useState({left: 0, top: 0});
    const [cardTranslate, setCardTranslate] = useState({x: '', y: ''});

    useEffect(() => {
        if (!map || !place) return;
        if (!place.geometry?.location) return;
        const geometry = {
            lat: place.geometry.location.lat(),
            lng: place.geometry.location.lng(),
        }

        const topRight = map.getProjection()?.fromLatLngToPoint(map.getBounds()!.getNorthEast());
        const bottomLeft = map.getProjection()?.fromLatLngToPoint(map.getBounds()!.getSouthWest());
        const scale = Math.pow(2, map.getZoom()!);
        const worldPoint = map.getProjection()?.fromLatLngToPoint(geometry);

        const x = worldPoint!.x - bottomLeft!.x < 0 ? (worldPoint!.x - bottomLeft!.x + 256) * scale : (worldPoint!.x - bottomLeft!.x) * scale;
        const y = (worldPoint!.y - topRight!.y) * scale;

        setCardPosition({left: x, top: y});

        const mapWidth = Math.abs(topRight!.x - bottomLeft!.x) * scale;
        const mapHeight = Math.abs(topRight!.y - bottomLeft!.y) * scale;

        setCardTranslate(prevState => {
            const newState = {...prevState};
            if (y > mapHeight / 2) {
                newState.y = "calc(-100% - 45px)";
            } else {
                newState.y = "calc(0% + 10px)";
            }
            console.log('x', x, 'mapWidth', mapWidth, 'placeSideBarWidth', placeSideBarWidth);

            if (x < 150 + placeSideBarWidth) {
                newState.x = "10px";
            } else if (x > mapWidth - 150) {
                newState.x = "calc(-100% - 10px)";
            } else {
                newState.x = "-50%";
            }

            return newState;
        });

    }, [placeDetails, map]);

    useEffect(() => {
        if (!placesService || !place.place_id) return;
        const request = {
            placeId: place.place_id,
        };
        placesService.getDetails(request, (place, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place) {
                console.log(place);
                setPlaceDetails(place);
            } else {
                console.error("Place details request was not successful for the following reason:", status);
            }
        });
    }, [place.place_id, placesService]);

    const renderPhotos = () => {
        if (!placeDetails?.photos) return null;
        const photoUrl = placeDetails.photos[0].getUrl({maxWidth: 400, maxHeight: 400});
        return <Image objectFit='cover' src={photoUrl} alt={`photo of ${placeDetails.name}`} maxHeight={100}/>;
    };

    return (
        <Card
            overflow='hidden'
            width='300px'
            position='absolute'
            left={cardPosition.left}
            top={cardPosition.top}
            transform={`translate(${cardTranslate.x}, ${cardTranslate.y})`}
            zIndex={2}
        >
            {renderPhotos()}
            <CardBody p={0}>
                <Box textAlign="left">
                    <PlaceOverview size="small"
                                   place={placeDetails ? placeDetails : undefined}
                                   googleLogoAlreadyDisplayed/>
                </Box>
            </CardBody>
        </Card>
    );
};

export default PlaceInfoCard;