"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary, ResponseResult } from "@/types/model";
import Link from "next/link";
import { axiosInstance, AppError } from "@/lib/axios";
import { Alert, CircularProgress, Box, Typography } from "@mui/material";

const PlanList = () => {
    const [planSummaries, setPlanSummaries] = useState<PlanSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPlanSummaries = async () => {
            try {
                setLoading(true);
                setError(null);

                const response = await axiosInstance.get<
                    ResponseResult<PlanSummary[]>
                >("/plan");

                const plansWithDates = response.data.data.map(
                    (planSummary: PlanSummary) => ({
                        ...planSummary,
                        createTime: new Date(planSummary.createTime),
                        updateTime: new Date(planSummary.updateTime),
                    })
                );

                setPlanSummaries(plansWithDates);
            } catch (err) {
                log.error("Error fetching plans:", err);

                // Handle specific error types
                if (err instanceof AppError) {
                    setError(err.message);
                } else {
                    setError(
                        "An unexpected error occurred while loading plans"
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchPlanSummaries();
    }, []);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Box p={2}>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    return (
        <Box p={2}>
            <Typography variant="h4" component="h2" gutterBottom>
                Your Plans
            </Typography>

            {planSummaries.length === 0 ? (
                <Typography>
                    You don't have any plans yet. Create your first plan!
                </Typography>
            ) : (
                planSummaries.map((planSummary) => (
                    <Box key={planSummary.planId} className="plan-item" mb={2}>
                        <Link href={`/plan/${planSummary.planId}`}>
                            <Typography
                                variant="h6"
                                className="hover:underline cursor-pointer"
                            >
                                {planSummary.planName}
                            </Typography>
                        </Link>
                        <Typography>Role: {planSummary.role}</Typography>
                        <Typography>
                            Created: {planSummary.createTime.toLocaleString()}
                        </Typography>
                        <Typography>
                            Last Modified:{" "}
                            {planSummary.updateTime.toLocaleString()}
                        </Typography>
                    </Box>
                ))
            )}
        </Box>
    );
};

export default PlanList;
