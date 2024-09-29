import {useEffect} from 'react';
import {AdvancedMarker, Pin, useAdvancedMarkerRef} from '@vis.gl/react-google-maps';

interface Props {
    place: google.maps.places.PlaceResult;
    handleMouseEnter: (place: google.maps.places.PlaceResult) => void;
    handleMouseLeave: () => void;
    handleMarkerClick: (place: google.maps.places.PlaceResult) => void;
}

const CustomMarker = ({place, handleMouseEnter, handleMouseLeave, handleMarkerClick}: Props) => {
    const [markerRef, marker] = useAdvancedMarkerRef();

    useEffect(() => {
        if (!marker) return;
        marker.addEventListener('mouseenter', () => {
            handleMouseEnter(place)
        });
        marker.addEventListener('mouseleave', () => {
            handleMouseLeave();
        });

        return () => {
            marker.removeEventListener('mouseenter', () => handleMouseEnter(place));
            marker.removeEventListener('mouseleave', () => handleMouseLeave());
        }
    }, [marker, place.name]);

    if (!place.geometry?.location) return null;

    return (
        <AdvancedMarker
            key={place.place_id}
            position={{
                lat: place.geometry.location.lat(),
                lng: place.geometry.location.lng(),
            }}
            ref={markerRef}
            onClick={() => handleMarkerClick(place)}
        >
            <div>
            <Pin>
                {place.icon && (
                    <img
                        src={place.icon}
                        alt={place.name}
                        style={{filter: 'brightness(0) invert(1) saturate(0)', maxWidth: '12px', maxHeight: '12px'}}
                    />
                )}
            </Pin>
            </div>
        </AdvancedMarker>
    );
};

export default CustomMarker;
