"use client";
import React, { useState, useEffect } from "react";
import { APIProvider } from "@vis.gl/react-google-maps";
import { useAppSelector } from "@/lib/hooks";
import { MapProvider } from "@/components/canvas/CanvasProvider";
import { Grid } from "@mui/material";
import PlanPlaceList from "@/components/canvas/list/PlanPlaceList";
import PlanSummary from "@/components/canvas/summary/PlanSummary";

interface MapContainerProps {
    planId?: number;
}

const MapContainer: React.FC<MapContainerProps> = ({ planId }) => {
    const { isAuthenticated } = useAppSelector((state) => state.auth);
    const [apiKey, setApiKey] = useState<string>("");
    const [userRole, setUserRole] = useState<"OWNER" | "EDITOR" | "VIEWER">(
        "VIEWER"
    );

    useEffect(() => {
        // Get API key from environment variable
        setApiKey(process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "");
    }, []);

    const handleRoleChange = (role: "OWNER" | "EDITOR" | "VIEWER") => {
        setUserRole(role);
    };

    if (!apiKey) {
        return <div>Loading Google Maps API...</div>;
    }

    return (
        <APIProvider apiKey={apiKey}>
            <MapProvider planId={planId}>
                <Grid container sx={{ height: "calc(100vh - 64px)" }}>
                    <Grid item xs={12}>
                        {planId && isAuthenticated && (
                            <PlanSummary
                                planId={planId}
                                onRoleChange={handleRoleChange}
                            />
                        )}
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <PlanPlaceList userRole={userRole} />
                    </Grid>
                    <Grid item xs={12} md={9}>
                        <h1>Map</h1>
                        {/* Map component will go here */}
                    </Grid>
                </Grid>
            </MapProvider>
        </APIProvider>
    );
};

export default MapContainer;
