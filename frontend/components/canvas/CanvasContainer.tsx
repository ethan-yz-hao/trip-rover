"use client";
import React, { useState, useEffect } from "react";
import { APIProvider } from "@vis.gl/react-google-maps";
import { useAppSelector } from "@/lib/hooks";
import { CanvasProvider } from "@/components/canvas/CanvasProvider";
import { Grid } from "@mui/material";
import PlanPlaceList from "@/components/canvas/list/PlanPlaceList";
import PlanSummary from "@/components/canvas/summary/PlanSummary";
import { useRouter } from "next/navigation";
import { axiosInstance } from "@/lib/axios";

interface CanvasContainerProps {
    planId?: number;
}

const CanvasContainer: React.FC<CanvasContainerProps> = ({ planId }) => {
    const { isAuthenticated } = useAppSelector((state) => state.auth);
    const [apiKey, setApiKey] = useState<string>("");
    const router = useRouter();

    // Check for pending plan data after login
    useEffect(() => {
        if (isAuthenticated && !planId) {
            const pendingPlanName = localStorage.getItem("pendingPlanName");
            const pendingPlanDescription = localStorage.getItem(
                "pendingPlanDescription"
            );
            const pendingPlanIsPublic =
                localStorage.getItem("pendingPlanIsPublic") === "true";

            if (pendingPlanName) {
                // Clear the pending plan data
                localStorage.removeItem("pendingPlanName");
                localStorage.removeItem("pendingPlanDescription");
                localStorage.removeItem("pendingPlanIsPublic");

                // Create the plan
                const createPlan = async () => {
                    try {
                        const response = await axiosInstance.post("/plan", {
                            planName: pendingPlanName,
                            description: pendingPlanDescription || "",
                            isPublic: pendingPlanIsPublic,
                        });

                        // Navigate to the new plan
                        router.push(`/plan/${response.data.data.planId}`);
                    } catch (err) {
                        console.error("Failed to create pending plan:", err);
                    }
                };

                createPlan();
            }
        }
    }, [isAuthenticated, planId, router]);

    useEffect(() => {
        // Get API key from environment variable
        setApiKey(process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || "");
    }, []);

    if (!apiKey) {
        return <div>Loading Google Maps API...</div>;
    }

    return (
        <APIProvider apiKey={apiKey}>
            <CanvasProvider planId={planId}>
                <Grid container sx={{ height: "calc(100vh - 64px)" }}>
                    <Grid item xs={12} md={3}>
                        <PlanSummary />
                        <PlanPlaceList />
                    </Grid>
                    <Grid item xs={12} md={9}>
                        <h1>Map</h1>
                        {/* Map component will go here */}
                    </Grid>
                </Grid>
            </CanvasProvider>
        </APIProvider>
    );
};

export default CanvasContainer;
