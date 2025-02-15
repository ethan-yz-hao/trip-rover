import React, { useState } from "react";

interface StaySecondsEditorProps {
    placeId: string;
    currentStaySeconds: number;
    onSubmit: (placeId: string, newStaySeconds: number) => void;
}

const StaySecondsEditor: React.FC<StaySecondsEditorProps> = ({
    placeId,
    currentStaySeconds,
    onSubmit,
}) => {
    const [inputValue, setInputValue] = useState<number>(currentStaySeconds);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(e.target.value, 10);
        if (!isNaN(value)) {
            setInputValue(value);
        } else if (e.target.value === "") {
            setInputValue(1600); // Handle empty input as 0 or any default value
        }
    };

    const handleSubmit = () => {
        if (inputValue < 0) {
            alert("Please enter a valid number of seconds.");
            return;
        }
        onSubmit(placeId, inputValue);
    };

    return (
        <div style={{ display: "inline-flex", alignItems: "center" }}>
            <input
                type="number"
                value={inputValue}
                onChange={handleChange}
                min="1"
                style={{ width: "80px", marginRight: "8px" }}
            />
            <button onClick={handleSubmit}>Submit</button>
        </div>
    );
};

export default StaySecondsEditor;
