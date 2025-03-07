"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary as PlanSummaryType, ResponseResult } from "@/types/model";
import {
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Typography,
    Box,
    Chip,
    Stack,
    Tooltip,
    TextField,
    IconButton,
    Button,
    Switch,
    FormControlLabel,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import PublicIcon from "@mui/icons-material/Public";
import LockIcon from "@mui/icons-material/Lock";
import EditIcon from "@mui/icons-material/Edit";
import { axiosInstance, AppError } from "@/lib/axios";
import PeopleIcon from "@mui/icons-material/People";
import PlanAccessDialog from "./PlanAccessDialog";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";

const PlanSummary = () => {
    const { planId, setUserRole } = useCanvasContext();
    const [planSummary, setPlanSummary] = useState<PlanSummaryType | null>(
        null
    );
    const [error, setError] = useState<string>("");
    const [isEditing, setIsEditing] = useState(false);
    const [editedDescription, setEditedDescription] = useState("");
    const [editedIsPublic, setEditedIsPublic] = useState(false);
    const [updateError, setUpdateError] = useState("");
    const [isAccessDialogOpen, setIsAccessDialogOpen] = useState(false);

    const canEdit =
        planSummary?.role === "OWNER" || planSummary?.role === "EDITOR";

    useEffect(() => {
        const fetchPlanSummary = async () => {
            try {
                const response = await axiosInstance.get<
                    ResponseResult<PlanSummaryType>
                >(`/plan/${planId}`);

                // Convert string dates to Date objects
                const planSummary = {
                    ...response.data.data,
                    createTime: new Date(response.data.data.createTime),
                    updateTime: new Date(response.data.data.updateTime),
                };
                setPlanSummary(planSummary);
                setUserRole(planSummary.role);
            } catch (err) {
                log.error("Error fetching plans:", err);
                if (err instanceof AppError) {
                    setError(err.message);
                } else {
                    setError("Failed to load plans");
                }
            }
        };

        fetchPlanSummary();
    }, [planId, setUserRole]);

    const handleEditClick = () => {
        setEditedDescription(planSummary?.description || "");
        setEditedIsPublic(planSummary?.isPublic || false);
        setIsEditing(true);
    };

    const handleSubmit = async () => {
        try {
            const response = await axiosInstance.patch<
                ResponseResult<PlanSummaryType>
            >(`/plan/${planId}`, {
                description: editedDescription,
                isPublic: editedIsPublic,
            });

            // Use the backend response to update the state
            const updatedPlan = {
                ...response.data.data,
                createTime: new Date(response.data.data.createTime),
                updateTime: new Date(response.data.data.updateTime),
            };

            setPlanSummary(updatedPlan);
            setIsEditing(false);
            setUpdateError("");
        } catch (err) {
            log.error("Error updating plan:", err);
            if (err instanceof AppError) {
                setUpdateError(err.message);
            } else {
                setUpdateError("Failed to update plan");
            }
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
            }}
        >
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                sx={{
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
                        <Stack direction="row" spacing={2} alignItems="center">
                            {canEdit && (
                                <IconButton
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setIsAccessDialogOpen(true);
                                    }}
                                    size="small"
                                >
                                    <PeopleIcon />
                                </IconButton>
                            )}
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
                        </Stack>
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
                                    <FormControlLabel
                                        control={
                                            <Switch
                                                checked={editedIsPublic}
                                                onChange={(e) =>
                                                    setEditedIsPublic(
                                                        e.target.checked
                                                    )
                                                }
                                                color="success"
                                            />
                                        }
                                        label={
                                            <Stack
                                                direction="row"
                                                spacing={1}
                                                alignItems="center"
                                            >
                                                {editedIsPublic ? (
                                                    <PublicIcon />
                                                ) : (
                                                    <LockIcon />
                                                )}
                                                <Typography>
                                                    {editedIsPublic
                                                        ? "Public"
                                                        : "Private"}
                                                </Typography>
                                            </Stack>
                                        }
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

                        {!isEditing && (
                            <Stack
                                direction="row"
                                spacing={1}
                                alignItems="center"
                            >
                                <Chip
                                    icon={
                                        planSummary.isPublic ? (
                                            <PublicIcon />
                                        ) : (
                                            <LockIcon />
                                        )
                                    }
                                    label={
                                        planSummary.isPublic
                                            ? "Public"
                                            : "Private"
                                    }
                                    size="small"
                                    color={
                                        planSummary.isPublic
                                            ? "success"
                                            : "default"
                                    }
                                    sx={{
                                        fontSize: "12px",
                                        px: 0.5,
                                    }}
                                />
                                <Chip
                                    icon={<EditIcon />}
                                    label={planSummary.role}
                                    size="small"
                                    color="primary"
                                    sx={{
                                        fontSize: "12px",
                                        px: 0.5,
                                    }}
                                />
                            </Stack>
                        )}
                    </Stack>
                )}
            </AccordionDetails>

            {planSummary && (
                <PlanAccessDialog
                    open={isAccessDialogOpen}
                    onClose={() => setIsAccessDialogOpen(false)}
                />
            )}
        </Accordion>
    );
};

export default PlanSummary;
