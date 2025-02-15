"use client";
import React, { useState, useCallback, useEffect } from "react";
import log from "@/lib/log";
import { PlanIndexResponseDTO, ResponseResult } from "@/types/model";
import debounce from "lodash/debounce";
import { axiosInstance } from "@/lib/axios";

const SearchPlan = () => {
    const [query, setQuery] = useState<string>("");
    const [plans, setPlans] = useState<PlanIndexResponseDTO[]>([]);
    const [error, setError] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const searchPlans = async (searchQuery: string) => {
        if (!searchQuery.trim()) {
            setPlans([]);
            return;
        }

        setIsLoading(true);
        try {
            const response = await axiosInstance.get(
                `/plan/search?query=${encodeURIComponent(searchQuery)}`
            );

            if (response.status !== 200) {
                throw new Error("Failed to search plans");
            }

            const data: ResponseResult<PlanIndexResponseDTO[]> = response.data;
            setPlans(data.data);
            setError("");
        } catch (error) {
            log.error("Error searching plans:", error);
            setError(
                error instanceof Error
                    ? error.message
                    : "Failed to search plans"
            );
            setPlans([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Debounce the search function to avoid too many API calls
    const debouncedSearch = useCallback(
        debounce((query: string) => searchPlans(query), 300),
        []
    );

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newQuery = e.target.value;
        setQuery(newQuery);
        debouncedSearch(newQuery);
    };

    // Cleanup debounce on component unmount
    useEffect(() => {
        return () => {
            debouncedSearch.cancel();
        };
    }, [debouncedSearch]);

    return (
        <div className="p-4">
            <div className="mb-4">
                <input
                    type="text"
                    value={query}
                    onChange={handleInputChange}
                    placeholder="Search public plans..."
                    className="w-full p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>

            {isLoading && <div className="text-gray-500">Loading...</div>}

            {error && <div className="text-red-500">{error}</div>}

            <div className="space-y-2">
                {plans.map((plan) => (
                    <div
                        key={plan.planId}
                        className="p-3 border rounded-lg hover:bg-gray-50 cursor-pointer"
                    >
                        <div className="font-medium">{plan.planName}</div>
                    </div>
                ))}
                {query && plans.length === 0 && !isLoading && !error && (
                    <div className="text-gray-500">No plans found</div>
                )}
            </div>
        </div>
    );
};

export default SearchPlan;
