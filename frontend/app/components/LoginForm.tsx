'use client';
import React, { useState } from 'react';
import log from "@/app/log";

const LoginForm: React.FC = () => {
    // State to hold form data
    const [userName, setUserName] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [message, setMessage] = useState<string>('');

    // Handle form submission
    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault(); // Prevent form from submitting the traditional way

        try {
            // Send POST request to the API
            const response = await fetch('http://localhost:8080/api/user/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userName,
                    password,
                }),
                credentials: 'include', // Include cookies in the request
            });

            // Parse the JSON response
            const data = await response.json();

            // Handle success or failure
            if (response.ok) {
                setMessage(data.msg);
                log.log('Success:', data);
            } else {
                setMessage(`Login failed: ${data.msg || 'Unknown error'}`);
            }
        } catch (error) {
            log.error('Error:', error);
            setMessage('Login failed due to network or server error.');
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">Username:</label>
                    <input
                        type="text"
                        id="username"
                        value={userName}
                        onChange={(e) => setUserName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="password">Password:</label>
                    <input
                        type="password"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Login</button>
            </form>
            {/* Show message */}
            {message && <p>{message}</p>}
        </div>
    );
};

export default LoginForm;
