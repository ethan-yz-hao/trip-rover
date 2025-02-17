"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary as PlanSummaryType } from "@/types/model";

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

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            {planSummary && (
                <div key={planSummary.planId} className="plan-item">
                    <h3>{planSummary.planName}</h3>
                    <p>Role: {planSummary.role}</p>
                    <p>Created: {planSummary.createTime.toLocaleString()}</p>
                    <p>
                        Last Modified: {planSummary.updateTime.toLocaleString()}
                    </p>
                </div>
            )}
        </div>
    );
};

export default PlanSummary;
