import {useMapsLibrary} from "@vis.gl/react-google-maps";
import React, {useEffect, useState} from "react";

interface Props {
    inputRef: React.RefObject<HTMLInputElement>;
    options: google.maps.places.AutocompleteOptions;

}

const  useAutoComplete = ({inputRef, options}: Props) => {
    const placesLibrary = useMapsLibrary('places');
    const [autoComplete, setAutoComplete] = useState<google.maps.places.Autocomplete | null>(null);

    useEffect(() => {
        if (!placesLibrary || !inputRef.current) return;

        setAutoComplete(new placesLibrary.Autocomplete(inputRef.current, options));
    }, [placesLibrary]);

    return autoComplete;
}

export default useAutoComplete;
