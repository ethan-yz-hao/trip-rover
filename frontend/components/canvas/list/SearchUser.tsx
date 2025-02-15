"use client";
import React, { useState, useCallback, useEffect } from "react";
import log from "@/lib/log";
import { UserIndexResponseDTO, ResponseResult } from "@/types/model";
import debounce from "lodash/debounce";

const SearchUser = () => {
    const [query, setQuery] = useState<string>("");
    const [users, setUsers] = useState<UserIndexResponseDTO[]>([]);
    const [error, setError] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const searchUsers = async (searchQuery: string) => {
        if (!searchQuery.trim()) {
            setUsers([]);
            return;
        }

        setIsLoading(true);
        try {
            const response = await fetch(
                `http://localhost:8080/api/user/search?query=${encodeURIComponent(
                    searchQuery
                )}`,
                {
                    credentials: "include",
                }
            );

            if (!response.ok) {
                throw new Error("Failed to search users");
            }

            const data: ResponseResult<UserIndexResponseDTO[]> =
                await response.json();
            setUsers(data.data);
            setError("");
        } catch (error) {
            log.error("Error searching users:", error);
            setError(
                error instanceof Error
                    ? error.message
                    : "Failed to search users"
            );
            setUsers([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Debounce the search function to avoid too many API calls
    const debouncedSearch = useCallback(
        debounce((query: string) => searchUsers(query), 300),
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
                    placeholder="Search users..."
                    className="w-full p-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>

            {isLoading && <div className="text-gray-500">Loading...</div>}

            {error && <div className="text-red-500">{error}</div>}

            <div className="space-y-2">
                {users.map((user) => (
                    <div
                        key={user.id}
                        className="p-3 border rounded-lg hover:bg-gray-50"
                    >
                        <div className="font-medium">{user.userName}</div>
                        <div className="text-sm text-gray-500">
                            {user.nickName}
                        </div>
                    </div>
                ))}
                {query && users.length === 0 && !isLoading && !error && (
                    <div className="text-gray-500">No users found</div>
                )}
            </div>
        </div>
    );
};

export default SearchUser;
