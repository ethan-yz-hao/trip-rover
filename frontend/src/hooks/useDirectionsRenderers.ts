import { useMap, useMapsLibrary } from "@vis.gl/react-google-maps";
import { useEffect, useRef } from "react";

const useDirectionsRenderers = () => {
    const map = useMap();
    const routesLibrary = useMapsLibrary('routes');
    const renderersRef = useRef<Array<google.maps.DirectionsRenderer>>([]);

    const clearDirectionsRenderers = () => {
        renderersRef.current.forEach(renderer => {
            renderer.setMap(null);
        });
        renderersRef.current = [];
    }

    // Function to create a new renderer
    const createDirectionsRenderer = () => {
        if (!routesLibrary || !map) return null;

        const renderer = new routesLibrary.DirectionsRenderer({preserveViewport: true});
        renderer.setMap(map);
        renderersRef.current.push(renderer);
        return renderer;
    };

    useEffect(() => {
        if (!routesLibrary || !map) return;

        // Clean up existing renderers when dependencies change
        return () => {
            clearDirectionsRenderers();
        };
    }, [routesLibrary, map]);

    return { createDirectionsRenderer, clearDirectionsRenderers, renderers: renderersRef.current };
}

export default useDirectionsRenderers;
