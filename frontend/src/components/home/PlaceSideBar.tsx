import {PlaceOverview} from '@googlemaps/extended-component-library/react';
import {Box} from "@chakra-ui/react";
import React, {forwardRef} from "react";

const PlaceSideBar = forwardRef(({placeId}: {placeId: string}, ref: React.Ref<HTMLDivElement>) => {
    return (
        <Box w="25%" zIndex={3} position="absolute" maxH="100%" overflowY="auto" textAlign="left" ref={ref}>
            <Box height="3rem" bg="white"/>
            <PlaceOverview size="x-large" place={placeId} googleLogoAlreadyDisplayed />
        </Box>
    );
});

export default PlaceSideBar;