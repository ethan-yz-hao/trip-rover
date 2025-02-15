"use client";
import React, { useState, useEffect } from "react";
import log from "@/lib/log";
import { PlanSummary, ResponseResult } from "@/types/model";
import Link from "next/link";
import { axiosInstance } from "@/lib/axios";

const PlanList = () => {
    const [planSummaries, setPlanSummaries] = useState<PlanSummary[]>([]);
    const [error, setError] = useState<string>("");

    useEffect(() => {
        const fetchPlanSummaries = async () => {
            try {
                const response = await axiosInstance.get<
                    ResponseResult<PlanSummary[]>
                >("/plan", {
                    withCredentials: true,
                });

                if (response.status !== 200) {
                    throw new Error(response.data.msg);
                }

                const plansWithDates = response.data.data.map(
                    (planSummary: PlanSummary) => ({
                        ...planSummary,
                        createTime: new Date(planSummary.createTime),
                        updateTime: new Date(planSummary.updateTime),
                    })
                );
                setPlanSummaries(plansWithDates);
            } catch (error) {
                log.error("Error fetching plans:", error);
                setError(
                    error instanceof Error
                        ? error.message
                        : "Failed to load plans"
                );
            }
        };

        fetchPlanSummaries();
    }, []);

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            <h2>Your Plans</h2>
            {planSummaries.map((planSummary) => (
                <div key={planSummary.planId} className="plan-item">
                    <Link href={`/plan/${planSummary.planId}`}>
                        <h3 className="hover:underline cursor-pointer">
                            {planSummary.planName}
                        </h3>
                    </Link>
                    <p>Role: {planSummary.role}</p>
                    <p>Created: {planSummary.createTime.toLocaleString()}</p>
                    <p>
                        Last Modified: {planSummary.updateTime.toLocaleString()}
                    </p>
                </div>
            ))}
        </div>
    );
};

export default PlanList;
