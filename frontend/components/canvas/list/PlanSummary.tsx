"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary as PlanSummaryType } from "@/types/model";
import {
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Typography,
    Box,
    Chip,
    Stack,
    Divider,
    Tooltip,
    TextField,
    IconButton,
    Button,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import PublicIcon from "@mui/icons-material/Public";
import LockIcon from "@mui/icons-material/Lock";
import EditIcon from "@mui/icons-material/Edit";

const PlanSummary = ({
    planId,
    onRoleChange,
}: {
    planId: number;
    onRoleChange: (role: "OWNER" | "EDITOR" | "VIEWER") => void;
}) => {
    const [planSummary, setPlanSummary] = useState<PlanSummaryType | null>(
        null
    );
    const [error, setError] = useState<string>("");
    const [isEditing, setIsEditing] = useState(false);
    const [editedDescription, setEditedDescription] = useState("");
    const [updateError, setUpdateError] = useState("");

    const canEdit =
        planSummary?.role === "OWNER" || planSummary?.role === "EDITOR";

    useEffect(() => {
        const fetchPlanSummary = async () => {
            try {
                const response = await fetch(
                    `http://localhost:8080/api/plan/${planId}`,
                    {
                        credentials: "include",
                    }
                );

                if (!response.ok) {
                    throw new Error("Please log in to view plans");
                }

                const data = await response.json();
                // Convert string dates to Date objects
                const planSummary = {
                    ...data.data,
                    createTime: new Date(data.data.createTime),
                    updateTime: new Date(data.data.updateTime),
                };
                setPlanSummary(planSummary);
                onRoleChange(planSummary.role);
            } catch (error) {
                log.error("Error fetching plans:", error);
                setError(
                    error instanceof Error
                        ? error.message
                        : "Failed to load plans"
                );
            }
        };

        fetchPlanSummary();
    }, [planId, onRoleChange]);

    const handleEditClick = () => {
        setEditedDescription(planSummary?.description || "");
        setIsEditing(true);
    };

    const handleSubmit = async () => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/plan/${planId}`,
                {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    credentials: "include",
                    body: JSON.stringify({
                        description: editedDescription,
                    }),
                }
            );

            if (!response.ok) {
                throw new Error("Failed to update description");
            }

            const data = await response.json();
            setPlanSummary({
                ...planSummary!,
                description: editedDescription,
                updateTime: new Date(data.data.updateTime),
            });
            setIsEditing(false);
            setUpdateError("");
        } catch (error) {
            log.error("Error updating description:", error);
            setUpdateError(
                error instanceof Error
                    ? error.message
                    : "Failed to update description"
            );
        }
    };

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <Accordion
            sx={{
                backgroundColor: "background.paper",
                boxShadow: 1,
                "&:before": { display: "none" },
                borderRadius: 2,
                mb: 2,
            }}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                sx={{
                    minHeight: 64,
                    borderRadius: 2,
                }}
            >
                {planSummary && (
                    <Box
                        sx={{
                            display: "flex",
                            width: "100%",
                            alignItems: "center",
                            justifyContent: "space-between",
                        }}
                    >
                        <Typography variant="h6" component="div">
                            {planSummary.planName}
                        </Typography>
                        <Tooltip
                            title={
                                <Box sx={{ p: 1 }}>
                                    <Typography variant="body2">
                                        Created on{" "}
                                        {planSummary.createTime.toLocaleDateString(
                                            "en-US",
                                            {
                                                weekday: "long",
                                                year: "numeric",
                                                month: "long",
                                                day: "numeric",
                                            }
                                        )}
                                    </Typography>
                                </Box>
                            }
                            arrow
                            placement="top"
                            sx={{
                                backgroundColor: "background.paper",
                                boxShadow: 2,
                                borderRadius: 1,
                            }}
                        >
                            <Box
                                sx={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: 0.5,
                                    color: "text.secondary",
                                }}
                            >
                                <AccessTimeIcon fontSize="small" />
                                <Typography variant="body2">
                                    {planSummary.createTime.toLocaleTimeString(
                                        "en-US",
                                        {
                                            hour: "numeric",
                                            minute: "2-digit",
                                            hour12: true,
                                        }
                                    )}
                                </Typography>
                            </Box>
                        </Tooltip>
                    </Box>
                )}
            </AccordionSummary>
            <AccordionDetails>
                {planSummary && (
                    <Stack spacing={2}>
                        <Box sx={{ position: "relative" }}>
                            {isEditing ? (
                                <Stack spacing={2}>
                                    <TextField
                                        multiline
                                        minRows={2}
                                        maxRows={4}
                                        value={editedDescription}
                                        onChange={(e) =>
                                            setEditedDescription(e.target.value)
                                        }
                                        error={!!updateError}
                                        helperText={updateError}
                                        fullWidth
                                        sx={{ mt: 1 }}
                                    />
                                    <Stack
                                        direction="row"
                                        spacing={1}
                                        justifyContent="flex-end"
                                    >
                                        <Button
                                            variant="outlined"
                                            onClick={() => setIsEditing(false)}
                                            size="small"
                                        >
                                            Cancel
                                        </Button>
                                        <Button
                                            variant="contained"
                                            onClick={handleSubmit}
                                            size="small"
                                        >
                                            Save
                                        </Button>
                                    </Stack>
                                </Stack>
                            ) : (
                                <Box
                                    sx={{
                                        display: "flex",
                                        alignItems: "flex-start",
                                        gap: 1,
                                    }}
                                >
                                    <Typography
                                        color="text.secondary"
                                        sx={{ flex: 1 }}
                                    >
                                        {planSummary.description}
                                    </Typography>
                                    {canEdit && (
                                        <IconButton
                                            onClick={handleEditClick}
                                            size="small"
                                            sx={{
                                                ml: 1,
                                                "&:hover": {
                                                    backgroundColor:
                                                        "action.hover",
                                                },
                                            }}
                                        >
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                    )}
                                </Box>
                            )}
                        </Box>

                        <Stack direction="row" spacing={1} alignItems="center">
                            <Chip
                                icon={
                                    planSummary.isPublic ? (
                                        <PublicIcon />
                                    ) : (
                                        <LockIcon />
                                    )
                                }
                                label={
                                    planSummary.isPublic ? "Public" : "Private"
                                }
                                size="small"
                                color={
                                    planSummary.isPublic ? "success" : "default"
                                }
                            />
                            <Chip
                                icon={<EditIcon />}
                                label={planSummary.role}
                                size="small"
                                color="primary"
                            />
                        </Stack>
                    </Stack>
                )}
            </AccordionDetails>
        </Accordion>
    );
};

export default PlanSummary;
