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
    Alert,
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
import { useRouter } from "next/navigation";
import { useAppSelector } from "@/lib/hooks";

const PlanSummary = () => {
    const {
        planId,
        planSummary,
        loading,
        error,
        setPlanSummary,
        setPlanPlaces,
        planPlaces,
    } = useCanvasContext();
    const { isAuthenticated } = useAppSelector((state) => state.auth);
    const [isEditing, setIsEditing] = useState(false);
    const [editedDescription, setEditedDescription] = useState("");
    const [editedIsPublic, setEditedIsPublic] = useState(false);
    const [editedPlanName, setEditedPlanName] = useState("");
    const [updateError, setUpdateError] = useState("");
    const [isAccessDialogOpen, setIsAccessDialogOpen] = useState(false);
    const router = useRouter();

    // Determine if this is a new plan (no planId) or if user can edit existing plan
    const isNewPlan = !planId;
    const canEdit =
        isNewPlan ||
        planSummary?.role === "OWNER" ||
        planSummary?.role === "EDITOR";

    // Initialize edit form when entering edit mode or for new plans
    useEffect(() => {
        if ((isEditing && planSummary) || isNewPlan) {
            setEditedDescription(planSummary?.description || "");
            setEditedIsPublic(planSummary?.isPublic || false);
            setEditedPlanName(planSummary?.planName || "");
        }
    }, [isEditing, planSummary, isNewPlan]);

    // For new plans, start in edit mode
    useEffect(() => {
        if (isNewPlan) {
            setIsEditing(true);
            setPlanSummary({
                planId: -1,
                planName: editedPlanName,
                isPublic: editedIsPublic,
                description: editedDescription,
                role: "OWNER",
                createTime: new Date(),
                updateTime: new Date(),
            });
            setPlanPlaces({
                planId: -1,
                planName: editedPlanName,
                places: [],
                version: 0,
            });
        }
    }, [isNewPlan]);

    const handleEditClick = () => {
        setIsEditing(true);
    };

    const handleSubmit = async () => {
        try {
            if (!isAuthenticated) {
                // Prompt user to sign in
                window.localStorage.setItem(
                    "redirectAfterLogin",
                    window.location.pathname
                );
                window.localStorage.setItem("pendingPlanName", editedPlanName);
                window.localStorage.setItem(
                    "pendingPlanDescription",
                    editedDescription
                );
                window.localStorage.setItem(
                    "pendingPlanIsPublic",
                    String(editedIsPublic)
                );

                router.push("/login");
                return;
            }

            if (isNewPlan) {
                // Create a new plan with places
                const response = await axiosInstance.post<
                    ResponseResult<PlanSummaryType>
                >("/plan", {
                    planName: editedPlanName,
                    description: editedDescription,
                    isPublic: editedIsPublic,
                    places: planPlaces?.places || [],
                });

                // Navigate to the new plan
                const newPlanId = response.data.data.planId;
                router.push(`/plan/${newPlanId}`);
            } else {
                // Update existing plan
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
            }
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

    // For new plans, show a simplified form
    if (isNewPlan) {
        return (
            <Accordion
                defaultExpanded={false}
                sx={{
                    backgroundColor: "background.paper",
                    boxShadow: 1,
                    "&:before": { display: "none" },
                    borderRadius: 2,
                }}
            >
                <AccordionSummary
                    expandIcon={<ExpandMoreIcon />}
                    sx={{ borderRadius: 2 }}
                >
                    <Typography variant="h6" component="div">
                        Create New Plan
                    </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Stack spacing={2}>
                        <TextField
                            label="Plan Name"
                            value={editedPlanName}
                            onChange={(e) => setEditedPlanName(e.target.value)}
                            error={!!updateError}
                            required
                            fullWidth
                        />
                        <TextField
                            multiline
                            minRows={2}
                            maxRows={4}
                            label="Description"
                            value={editedDescription}
                            onChange={(e) =>
                                setEditedDescription(e.target.value)
                            }
                            error={!!updateError}
                            helperText={updateError}
                            fullWidth
                        />
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={editedIsPublic}
                                    onChange={(e) =>
                                        setEditedIsPublic(e.target.checked)
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
                                        {editedIsPublic ? "Public" : "Private"}
                                    </Typography>
                                </Stack>
                            }
                        />
                        {!isAuthenticated && (
                            <Alert severity="info">
                                You'll need to sign in to save your plan.
                            </Alert>
                        )}
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={!editedPlanName.trim()}
                            fullWidth
                        >
                            {isAuthenticated
                                ? "Create Plan"
                                : "Sign in to Create Plan"}
                        </Button>
                    </Stack>
                </AccordionDetails>
            </Accordion>
        );
    }

    if (error) {
        return <div>{error}</div>;
    }

    if (loading || !planSummary) {
        return <div>Loading plan summary...</div>;
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
