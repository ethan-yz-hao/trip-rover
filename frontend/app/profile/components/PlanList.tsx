"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary, ResponseResult } from "@/types/model";
import Link from "next/link";
import { axiosInstance, AppError } from "@/lib/axios";
import {
    Alert,
    CircularProgress,
    Box,
    Typography,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";

const PlanList = () => {
    const [planSummaries, setPlanSummaries] = useState<PlanSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [planToDelete, setPlanToDelete] = useState<PlanSummary | null>(null);

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

    const handleDeleteClick = (plan: PlanSummary, e: React.MouseEvent) => {
        e.preventDefault(); // Prevent navigation when clicking delete
        setPlanToDelete(plan);
        setDeleteDialogOpen(true);
    };

    const handleDeleteCancel = () => {
        setDeleteDialogOpen(false);
        setPlanToDelete(null);
    };

    const handleDeleteConfirm = async () => {
        if (!planToDelete) return;

        try {
            setLoading(true);
            await axiosInstance.delete(`/plan/${planToDelete.planId}`);

            // Remove the deleted plan from the state
            setPlanSummaries(
                planSummaries.filter(
                    (plan) => plan.planId !== planToDelete.planId
                )
            );

            setDeleteDialogOpen(false);
            setPlanToDelete(null);
        } catch (err) {
            log.error("Error deleting plan:", err);

            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError(
                    "An unexpected error occurred while deleting the plan"
                );
            }
        } finally {
            setLoading(false);
        }
    };

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
                    <Box
                        key={planSummary.planId}
                        className="plan-item"
                        mb={2}
                        sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "flex-start",
                            p: 2,
                            border: "1px solid #eee",
                            borderRadius: 1,
                        }}
                    >
                        <Box>
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
                                Created:{" "}
                                {planSummary.createTime.toLocaleString()}
                            </Typography>
                            <Typography>
                                Last Modified:{" "}
                                {planSummary.updateTime.toLocaleString()}
                            </Typography>
                        </Box>
                        <Button
                            color="error"
                            startIcon={<DeleteIcon />}
                            onClick={(e) => handleDeleteClick(planSummary, e)}
                            disabled={planSummary.role === "VIEWER"}
                        >
                            Delete
                        </Button>
                    </Box>
                ))
            )}

            {/* Confirmation Dialog */}
            <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
                <DialogTitle>Confirm Deletion</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Are you sure you want to delete the plan "
                        {planToDelete?.planName}"? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleDeleteCancel}>Cancel</Button>
                    <Button
                        onClick={handleDeleteConfirm}
                        color="error"
                        autoFocus
                    >
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
};

export default PlanList;
