"use client";
import React, { useState, useEffect } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    List,
    ListItem,
    ListItemText,
    ListItemSecondaryAction,
    Select,
    MenuItem,
    Chip,
    Stack,
    TextField,
    Divider,
    Typography,
    Box,
    FormControl,
} from "@mui/material";
import { axiosInstance, AppError } from "@/lib/axios";
import {
    PlanMember,
    UserIndexResponseDTO,
    ResponseResult,
    BatchPlanMemberAdditionResponse,
} from "@/types/model";
import { useSelector } from "react-redux";
import { RootState } from "@/lib/store";
import log from "@/lib/log";
import Autocomplete from "@mui/material/Autocomplete";
import debounce from "lodash/debounce";
import { useMapContext } from "../CanvasProvider";

interface PlanAccessDialogProps {
    open: boolean;
    onClose: () => void;
}

export default function PlanAccessDialog({
    open,
    onClose,
}: PlanAccessDialogProps) {
    const { planId } = useMapContext();
    const [members, setMembers] = useState<PlanMember[]>([]);
    const [searchResults, setSearchResults] = useState<UserIndexResponseDTO[]>(
        []
    );
    const [selectedUsers, setSelectedUsers] = useState<UserIndexResponseDTO[]>(
        []
    );
    const [newMemberRole, setNewMemberRole] = useState<"VIEWER" | "EDITOR">(
        "VIEWER"
    );
    const [searchQuery, setSearchQuery] = useState("");
    const [error, setError] = useState("");
    const currentUser = useSelector((state: RootState) => state.auth.user);

    // Fetch current members
    useEffect(() => {
        if (open) {
            fetchMembers();
        }
    }, [open, planId]);

    const fetchMembers = async () => {
        try {
            const response = await axiosInstance.get<
                ResponseResult<PlanMember[]>
            >(`/plan/${planId}/member`);
            setMembers(response.data.data);
        } catch (err) {
            log.error("Error fetching members:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to load members");
            }
        }
    };

    const searchUsers = debounce(async (query: string) => {
        if (!query.trim()) {
            setSearchResults([]);
            return;
        }

        try {
            const response = await axiosInstance.get<
                ResponseResult<UserIndexResponseDTO[]>
            >(`/user/search?query=${encodeURIComponent(query)}`);
            // Filter out current user and already selected users
            const filteredResults = response.data.data.filter(
                (user) =>
                    user.id !== currentUser?.id &&
                    !selectedUsers.some(
                        (selected) => selected.id === user.id
                    ) &&
                    !members.some((member) => member.id === user.id)
            );
            setSearchResults(filteredResults);
        } catch (err) {
            log.error("Error searching users:", err);
        }
    }, 300);

    const handleRoleChange = async (
        userId: number,
        newRole: "VIEWER" | "EDITOR" | "REMOVE"
    ) => {
        try {
            if (newRole === "REMOVE") {
                await axiosInstance.delete(`/plan/${planId}/member`, {
                    data: { id: userId },
                });
                setMembers(members.filter((m) => m.id !== userId));
            } else {
                const response = await axiosInstance.patch<
                    ResponseResult<PlanMember>
                >(`/plan/${planId}/member/role`, { id: userId, role: newRole });
                setMembers(
                    members.map((m) =>
                        m.id === userId ? response.data.data : m
                    )
                );
            }
        } catch (err) {
            log.error("Error updating member role:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to update member role");
            }
        }
    };

    const handleAddMembers = async () => {
        if (!selectedUsers.length) return;

        try {
            const response = await axiosInstance.post<
                ResponseResult<BatchPlanMemberAdditionResponse>
            >(
                `/plan/${planId}/member`,
                selectedUsers.map((user) => ({
                    id: user.id,
                    role: newMemberRole,
                }))
            );

            // Update members list with successful additions
            setMembers([...members, ...response.data.data.successful]);

            // Handle failures if any
            if (response.data.data.failed.length > 0) {
                setError(
                    `Failed to add some members: ${response.data.data.failed
                        .map((f) => f.error)
                        .join(", ")}`
                );
            }

            // Clear selection
            setSelectedUsers([]);
            setSearchQuery("");
        } catch (err) {
            log.error("Error adding members:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to add members");
            }
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Manage Access</DialogTitle>
            <DialogContent>
                <Stack spacing={2}>
                    {error && (
                        <Typography color="error" variant="body2">
                            {error}
                        </Typography>
                    )}

                    <Box
                        sx={{
                            display: "flex",
                            gap: 1,
                            alignItems: "flex-start",
                        }}
                    >
                        <Autocomplete
                            multiple
                            value={selectedUsers}
                            onChange={(_, newValue) =>
                                setSelectedUsers(newValue)
                            }
                            options={searchResults}
                            getOptionLabel={(option) => option.userName}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    variant="outlined"
                                    placeholder="Add people"
                                    onChange={(e) => {
                                        setSearchQuery(e.target.value);
                                        searchUsers(e.target.value);
                                    }}
                                />
                            )}
                            renderTags={(value, getTagProps) =>
                                value.map((option, index) => (
                                    <Chip
                                        label={option.userName}
                                        {...getTagProps({ index })}
                                        size="small"
                                    />
                                ))
                            }
                            sx={{ flex: 1 }}
                        />
                        <FormControl sx={{ minWidth: 120 }}>
                            <Select
                                value={newMemberRole}
                                onChange={(e) =>
                                    setNewMemberRole(
                                        e.target.value as "VIEWER" | "EDITOR"
                                    )
                                }
                                size="small"
                            >
                                <MenuItem value="VIEWER">Viewer</MenuItem>
                                <MenuItem value="EDITOR">Editor</MenuItem>
                            </Select>
                        </FormControl>
                        <Button
                            variant="contained"
                            onClick={handleAddMembers}
                            disabled={selectedUsers.length === 0}
                        >
                            Add
                        </Button>
                    </Box>

                    <List>
                        {members
                            .sort((a, b) =>
                                a.role === "OWNER"
                                    ? -1
                                    : b.role === "OWNER"
                                    ? 1
                                    : 0
                            )
                            .map((member) => (
                                <ListItem key={member.id}>
                                    <ListItemText
                                        primary={member.userName}
                                        secondary={member.nickName}
                                    />
                                    <ListItemSecondaryAction>
                                        {member.role === "OWNER" ? (
                                            <Typography
                                                variant="body2"
                                                color="textSecondary"
                                            >
                                                Owner
                                            </Typography>
                                        ) : (
                                            <Select
                                                value={member.role}
                                                onChange={(e) =>
                                                    handleRoleChange(
                                                        member.id,
                                                        e.target.value as any
                                                    )
                                                }
                                                size="small"
                                            >
                                                <MenuItem value="EDITOR">
                                                    Editor
                                                </MenuItem>
                                                <MenuItem value="VIEWER">
                                                    Viewer
                                                </MenuItem>
                                                <Divider />
                                                <MenuItem value="REMOVE">
                                                    Remove access
                                                </MenuItem>
                                            </Select>
                                        )}
                                    </ListItemSecondaryAction>
                                </ListItem>
                            ))}
                    </List>
                </Stack>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Done</Button>
            </DialogActions>
        </Dialog>
    );
}
